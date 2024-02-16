package com.azurelight.capstone_2.Controller;

import java.util.Arrays;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.azurelight.capstone_2.Repository.ChatMessageRepository;
import com.azurelight.capstone_2.Repository.ChatroomRepository;
import com.azurelight.capstone_2.Repository.ChatroomuserRepository;
import com.azurelight.capstone_2.Repository.SystemMessageRepository;
import com.azurelight.capstone_2.Repository.UserRepository;
import com.azurelight.capstone_2.Service.AppStateEnum;
import com.azurelight.capstone_2.Service.FCMService;
import com.azurelight.capstone_2.Service.UserCurrentView;
import com.azurelight.capstone_2.Service.Utility;
import com.azurelight.capstone_2.Service.Noti.NotificationRequest;
import com.azurelight.capstone_2.db.ChatMessage;
import com.azurelight.capstone_2.db.Chatroom;
import com.azurelight.capstone_2.db.Chatroomuser;
import com.azurelight.capstone_2.db.SystemMessage;
import com.google.firebase.messaging.FirebaseMessagingException;

@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    @Autowired
    private FCMService fs;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatroomuserRepository chatroomuserRepository;

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private SystemMessageRepository systemMessageRepository;

    @PostMapping("/send-msg")
    public Map<String, Object> sendMessage(@RequestParam(value = "chatroomid") String chatroomid,
            @RequestParam(value = "me") String me, @RequestParam(value = "detail") String detail) {

        final String chatid = UUID.randomUUID() + "";

        String identifier = chatroomuserRepository.findIdentifierByRoomidAndEmail(chatroomid, me).get(0);
        chatMessageRepository.save(new ChatMessage(chatid, identifier, detail, me));
        String currentTime = Utility.getCurrentDateTimeAsString();

        List<Chatroomuser> userlistinroomAll = chatroomuserRepository.findByRoomid(chatroomid);
        List<Chatroomuser> userlistinroom = userlistinroomAll.stream().filter(u -> u.isState()).toList();

        String audienceList = String.join(" ", userlistinroomAll.stream().map(Chatroomuser::getEmail).toList());

        List<NotificationRequest> notificationRequestList = new ArrayList<>();

        for (Chatroomuser u : userlistinroom) {
            if (!u.getEmail().equals(me)) {
                String fcmtoken = userRepository.findById(u.getEmail()).get().getFcmtoken();
                notificationRequestList.add(new NotificationRequest(fcmtoken, me, detail));
            }
        }

        if (!notificationRequestList.isEmpty()) {
            try {
                fs.sendNotificationAll(notificationRequestList,
                        Map.of("notitype", "receive",
                                "roomid", chatroomid,
                                "chatid", chatid,
                                "detail", detail,
                                "timestamp", currentTime,
                                "audiencelist", audienceList,
                                "readusers", me));
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
        }

        chatroomRepository.updateRecentInfo(chatroomid, detail);
        return Map.of("chatid", chatid, "writer", me, "detail", detail, "timestamp", currentTime, "readusers", me);
    }

    @PostMapping("/readmsg")
    public int readMessage(@RequestParam(value = "chatroomid") String roomid, @RequestParam(value = "me") String me,
            @RequestParam(value = "chatidlist") String chatid) {
        System.out.println(me + "가 읽음");

        List<String> chatidlist = Arrays.asList(chatid.split(" "));
        List<String> updatedChatid = new ArrayList<>();

        for (String id : chatidlist) {
            ChatMessage cm = chatMessageRepository.findById(id).get();
            List<String> readuserlist = new ArrayList<>(Arrays.asList(cm.getReadusers().split(" ")));
            if (!readuserlist.contains(me)) {
                readuserlist.add(me);
                chatMessageRepository.updateReadusersByChatid(id, String.join(" ", readuserlist));
                updatedChatid.add(id);
            }
        }

        List<Chatroomuser> userlist = chatroomuserRepository.findByRoomidOnlyTrue(roomid);
        List<NotificationRequest> notificationRequestList = new ArrayList<>();
        for (Chatroomuser u : userlist) {
            if (!u.getEmail().equals(me)) {
                AppStateEnum s = UserCurrentView.getInstance().get(u.getEmail());

                if (s == AppStateEnum.FOREGROUND) {
                    System.out.println(u.getEmail() + " state is " + AppStateEnum.FOREGROUND);
                    String fcmtoken = userRepository.findById(u.getEmail()).get().getFcmtoken();
                    notificationRequestList.add(new NotificationRequest(fcmtoken, "reply", "i am bug."));
                } else {
                    System.out.println(u.getEmail() + " must to send but state is " + s);
                }
            }
        }

        if (!(notificationRequestList.isEmpty())) {
            try {
                fs.sendNotificationAll(notificationRequestList,
                        Map.of("notitype", "reply",
                                "chatroomid", roomid,
                                "who", me,
                                "idlist", String.join(" ", updatedChatid)));
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @PostMapping("/newchat")
    public Map<String, Object> startNewChat(@RequestParam(value = "me") String me,
            @RequestParam(value = "audiences") String audiences) {

        List<String> roomMembers = Arrays.asList(audiences.split(" "));
        final String chatroomId = UUID.randomUUID() + "";
        chatroomRepository.save(new Chatroom(chatroomId, roomMembers.size() + 1, ""));
        for (String member : roomMembers) {
            chatroomuserRepository.save(new Chatroomuser(UUID.randomUUID() + "", chatroomId, member, member, true));
        }
        final String chatroomStarterIdentifier = UUID.randomUUID() + "";
        chatroomuserRepository.save(new Chatroomuser(chatroomStarterIdentifier, chatroomId, me, me, true));
        SystemMessage chatStartSystemMessage = new SystemMessage(UUID.randomUUID() + "", chatroomId,
                audiences + " " + me, chatroomStarterIdentifier, "STARTCHAT");
        systemMessageRepository.save(chatStartSystemMessage);
        List<HashMap<String, Object>> syslogs = new ArrayList<HashMap<String, Object>>();
        syslogs.add(new HashMap<String, Object>(Map.of("sysid", chatStartSystemMessage.getSysid(),
                "type", chatStartSystemMessage.getType(),
                "timestamp", Utility.getCurrentDateTimeAsString(),
                "detail", chatStartSystemMessage.getDetail())));

        return Map.of("chatroomid", chatroomId,
                "unreadcount", 0,
                "audiencelist", audiences + " " + me,
                "logs", new ArrayList<HashMap<String, Object>>(),
                "syslogs", syslogs);
    }

    @PostMapping("/exit-chat")
    public String exitChattingRoom(@RequestParam(value = "me") String me,
            @RequestParam(value = "chatroomid") String chatroomid) {

        int result = chatroomuserRepository.updateChatroomuserStateToFalse(chatroomid, me);
        List<Chatroomuser> cru = chatroomuserRepository.findByRoomidOnlyTrue(chatroomid);
        if (cru.isEmpty()) {
            chatroomRepository.deleteById(chatroomid);
            System.out.println("chatroom destroyed");
        } else {
            final String identifier = chatroomuserRepository.findIdentifierByRoomidAndEmail(chatroomid, me).get(0);
            final String sysid = UUID.randomUUID() + "";
            systemMessageRepository.save(new SystemMessage(sysid, chatroomid, me, identifier, "EXIT"));
            List<NotificationRequest> notificationRequestList = new ArrayList<>();

            for (Chatroomuser user : cru) {
                AppStateEnum s = UserCurrentView.getInstance().get(user.getEmail());
                if (s == AppStateEnum.FOREGROUND) {
                    notificationRequestList.add(new NotificationRequest(
                            userRepository.findById(user.getEmail()).get().getFcmtoken(), "system log", "EXIT"));
                }
            }
            if (!(notificationRequestList.isEmpty())) {
                try {
                    fs.sendNotificationAll(notificationRequestList,
                            Map.of("notitype", "syslog",
                                    "sysid", sysid,
                                    "type", "EXIT",
                                    "roomid", chatroomid,
                                    "timestamp", Utility.getCurrentDateTimeAsString(),
                                    "detail", me));
                } catch (FirebaseMessagingException e) {
                    e.printStackTrace();
                }
            }
        }
        return result + "";
    }

    @PostMapping("enter-chat")
    public Map<String, String> enterChattingRoom(@RequestParam(value = "me") String me,
            @RequestParam(value = "target") String target,
            @RequestParam(value = "chatroomid") String chatroomid) {

        List<Chatroomuser> cru = chatroomuserRepository.findByRoomid(chatroomid);
        Boolean isExistAlready = true;
        String chatuserIdentifier = "";
        final String sysid = UUID.randomUUID() + "";

        for (Chatroomuser user : cru) {
            if (user.getEmail().equals(target) && !user.isState()) {
                isExistAlready = false;
                chatuserIdentifier = user.getIdentifier();
                chatroomuserRepository.updateChatroomuserState(chatroomid, target);
                break;
            }
        }

        if (isExistAlready) {
            chatuserIdentifier = UUID.randomUUID() + "";
            chatroomuserRepository.save(new Chatroomuser(chatuserIdentifier, chatroomid, target, target, true));
        }

        systemMessageRepository
                .save(new SystemMessage(sysid, chatroomid, me + " " + target, chatuserIdentifier, "ENTER"));

        List<NotificationRequest> notificationRequestList = new ArrayList<>();

        for (Chatroomuser user : cru.stream().filter(u -> u.isState()).filter(u -> !u.getEmail().equals(me)).toList()) {
            AppStateEnum s = UserCurrentView.getInstance().get(user.getEmail());
            if (s == AppStateEnum.FOREGROUND) {
                notificationRequestList.add(new NotificationRequest(
                        userRepository.findById(user.getEmail()).get().getFcmtoken(), "system log", "ENTER"));
            }
        }

        if (!(notificationRequestList.isEmpty())) {
            try {
                fs.sendNotificationAll(notificationRequestList,
                        Map.of("notitype", "syslog",
                                "sysid", sysid,
                                "type", "ENTER",
                                "roomid", chatroomid,
                                "timestamp", Utility.getCurrentDateTimeAsString(),
                                "detail", me + " " + target));
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
        }

        return Map.of("sysid", sysid,
                "type", "ENTER",
                "timestamp", Utility.getCurrentDateTimeAsString(),
                "detail", me + " " + target);
    }
}