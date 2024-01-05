package com.azurelight.capstone_2.Controller;

import java.util.Date;
import java.util.Arrays;
import java.util.UUID;
import static java.util.stream.Collectors.toCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import com.azurelight.capstone_2.Repository.ChatMessageRepository;
import com.azurelight.capstone_2.Repository.ChatroomRepository;
import com.azurelight.capstone_2.Repository.ChatroomuserRepository;
import com.azurelight.capstone_2.Repository.SystemMessageRepository;
import com.azurelight.capstone_2.Repository.UserRepository;
import com.azurelight.capstone_2.Service.FCMService;
import com.azurelight.capstone_2.Service.Utility;
import com.azurelight.capstone_2.Service.Noti.NotificationRequest;
import com.azurelight.capstone_2.db.ChatMessage;
import com.azurelight.capstone_2.db.Chatroom;
import com.azurelight.capstone_2.db.Chatroomuser;
import com.azurelight.capstone_2.db.SystemMessage;
import com.azurelight.capstone_2.db.User;
import com.google.firebase.messaging.FirebaseMessagingException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.util.Collections;

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

        // if (chatroomuserRepository.findByRoomidOnlyTrue(chatroomid).size() == 1) {}

        final String chatid = UUID.randomUUID() + "";

        String identifier = chatroomuserRepository.findIdentifierByRoomidAndEmail(chatroomid, me).get(0);
        chatMessageRepository.save(new ChatMessage(chatid, identifier, detail, me));
        String currentTime = Utility.getCurrentDateTimeAsString();

        List<Chatroomuser> userlistinroomAll = chatroomuserRepository.findByRoomid(chatroomid);
        List<Chatroomuser> userlistinroom = userlistinroomAll.stream().filter(u -> u.isState()).toList();

        String audienceList = String.join(" ", userlistinroomAll.stream().map(Chatroomuser::getEmail).toList());

        List<NotificationRequest> notificationRequestList = new ArrayList<>(); // 이게 비어있음

        for (Chatroomuser u : userlistinroom) {
            if (!u.getEmail().equals(me)) {
                String fcmtoken = userRepository.findById(u.getEmail()).get().getFcmtoken();
                notificationRequestList.add(new NotificationRequest(fcmtoken, me, detail));
            }
        }
        // audiencelist는 state를 고려하지 않음
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

        return Map.of("chatid", chatid, "writer", me, "text", detail, "timestamp", currentTime, "readusers", me);
    }

    @PostMapping("/readmsg")
    public int readMessage(@RequestParam(value = "chatroomid") String roomid, @RequestParam(value = "me") String me,
            @RequestParam(value = "chatidlist") String chatid) {
        System.out.println(me + "가 읽음");
        // 어떤놈이 보내놓고 나가서 아무도 없는데 한놈이 와서 읽으면 에러
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
                String fcmtoken = userRepository.findById(u.getEmail()).get().getFcmtoken();
                notificationRequestList.add(new NotificationRequest(fcmtoken, "reply", "i am bug."));
            }
        }

        if (!(notificationRequestList.isEmpty())) {
            try {
                fs.sendSilentNotificationAll(notificationRequestList,
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

    @GetMapping("/get-recentmsg")
    public List<HashMap<String, Object>> getRecentMessages(@RequestParam(value = "me") String me) {

        List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();

        // 본인이 소속된 톡방의 룸아이디 목록을 state를 고려하여 가져온다.
        List<String> roomids = chatroomuserRepository.findRoomidByEmail(me);

        for (String roomid : roomids) {
            // 룸아이디로 톡방 정보를 가져온다.
            Chatroom chatroom = chatroomRepository.findById(roomid).get();

            // 해당 톡방에 소속된 유저들의 목록을 가져온다. state는 고려하지 않는다.
            List<Chatroomuser> affiliatedUsersInChatroom = chatroomuserRepository.findByRoomid(roomid);

            List<String> audienceList = affiliatedUsersInChatroom.stream().filter(u -> !u.getEmail().equals(me))
                    .map(Chatroomuser::getEmail).toList();

            result.add(new HashMap<String, Object>(Map.of(
                    "timestamp", chatroom.getRecentTimestamAsString(),
                    "text", chatroom.getRecent_detail(),
                    "chatroomid", chatroom.getRoomid(),
                    "audienceList", audienceList)));
        }
        return result;
    }

    @GetMapping("/get-chatlogs")
    public Map<String, Object> getChatlogs(@RequestParam(value = "chatroomid") String roomid) {

        List<Chatroomuser> userListInRoom = chatroomuserRepository.findByRoomid(roomid);
        Map<String, String> identifierToEmailMap = new HashMap<>();
        List<ChatMessage> messageList = new ArrayList<>();
        List<HashMap<String, Object>> chatloglist = new ArrayList<HashMap<String, Object>>();

        for (Chatroomuser cru : userListInRoom) {
            messageList.addAll(chatMessageRepository.findByIdentifier(cru.getIdentifier()));
            identifierToEmailMap.put(cru.getIdentifier(), cru.getEmail());
        }

        Collections.sort(messageList);
        Collections.reverse(messageList);

        for (ChatMessage cm : messageList) {
            chatloglist.add(new HashMap<String, Object>(Map.of(
                    "chatId", cm.getChatid(),
                    "writer", identifierToEmailMap.get(cm.getIdentifier()),
                    "text", cm.getDetail(),
                    "timestamp", cm.getRecentTimestamAsString())));
        }

        List<Chatroomuser> joinedUserList = chatroomuserRepository.findByRoomid(roomid);
        List<HashMap<String, String>> joinedusermap = new ArrayList<HashMap<String, String>>();

        for (Chatroomuser u : joinedUserList) {
            joinedusermap.add(new HashMap<String, String>(Map.of(
                    "email", u.getEmail(), "nickname", u.getNickname())));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("chatloglist", chatloglist);
        response.put("joineduserlist", joinedusermap);

        // dmswns0147@gmail.com^2023-05-15 13:33:04#

        // try {
        // fs.sendNotification(new NotificationRequest(opne.getFcmtoken(), "hi~!!", "i
        // am fxxking bug!"),
        // Map.of("notitype", "reply", "chatid", "allMessage"));
        // } catch (FirebaseMessagingException e) {
        // e.printStackTrace();
        // }

        return response;
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
                notificationRequestList.add(new NotificationRequest(
                        userRepository.findById(user.getEmail()).get().getFcmtoken(), "system log", "EXIT"));
            }

            try {
                fs.sendNotificationAll(notificationRequestList,
                        Map.of("notitype", "syslog",
                                "sysid", sysid,
                                "type", "exit",
                                "roomid", chatroomid,
                                "timestamp", Utility.getCurrentDateTimeAsString(),
                                "who", me));
            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
            }
        }
        return result + "";
    }
}