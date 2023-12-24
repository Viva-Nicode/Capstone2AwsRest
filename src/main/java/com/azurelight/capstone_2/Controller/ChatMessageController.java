package com.azurelight.capstone_2.Controller;

import java.util.Arrays;
import java.util.UUID;
import static java.util.stream.Collectors.toCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import com.azurelight.capstone_2.Repository.ChatMessageRepository;
import com.azurelight.capstone_2.Repository.ChatroomRepository;
import com.azurelight.capstone_2.Repository.ChatroomuserRepository;
import com.azurelight.capstone_2.Repository.UserRepository;
import com.azurelight.capstone_2.Service.FCMService;
import com.azurelight.capstone_2.Service.Utility;
import com.azurelight.capstone_2.Service.Noti.NotificationRequest;
import com.azurelight.capstone_2.db.ChatMessage;
import com.azurelight.capstone_2.db.Chatroom;
import com.azurelight.capstone_2.db.Chatroomuser;
import com.azurelight.capstone_2.db.User;
import com.google.firebase.messaging.FirebaseMessagingException;

import java.util.Collections;

@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private FCMService fs;

    @Autowired
    private ChatroomuserRepository chatroomuserRepository;

    @Autowired
    private ChatroomRepository chatroomRepository;

    @PostMapping("/send-msg")
    public Map<String, Object> sendMessage(@RequestParam(value = "chatroomid") String chatroomid,
            @RequestParam(value = "me") String me, @RequestParam(value = "detail") String detail) {

        final String chatid = UUID.randomUUID() + "";

        String identifier = chatroomuserRepository.findIdentifierByRoomidAndEmail(chatroomid, me).get(0);
        chatMessageRepository.save(new ChatMessage(chatid, identifier, detail, 0));
        String currentTime = Utility.getCurrentDateTimeAsString();

        List<Chatroomuser> userlistinroomAll = chatroomuserRepository.findByRoomid(chatroomid);
        List<Chatroomuser> userlistinroom = userlistinroomAll.stream().filter(u -> u.isState()).toList();
        System.out.println(userlistinroomAll);

        String audienceList = String.join(" ", userlistinroomAll.stream().map(Chatroomuser::getEmail).toList());
        System.out.println(audienceList);

        List<NotificationRequest> notificationRequestList = new ArrayList<>(); // 이게 비어있음

        for (Chatroomuser u : userlistinroom) {
            if (!u.getEmail().equals(me)) {
                String fcmtoken = userRepository.findById(u.getEmail()).get().getFcmtoken();
                notificationRequestList.add(new NotificationRequest(fcmtoken, me, detail));
            }
        }

        try {
            fs.sendNotificationAll(notificationRequestList,
                    Map.of("notitype", "receive",
                            "roomid", chatroomid,
                            "chatid", chatid,
                            "detail", detail,
                            "timestamp", currentTime,
                            "audiencelist", audienceList));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
        chatroomRepository.updateRecentInfo(chatroomid, detail);

        return Map.of("chatId", chatid, "writer", me, "text", detail, "timestamp", currentTime);
    }

    // @PostMapping("/readmsg")
    // public int reagMessage(@RequestParam(value = "chatid") String chatid) {
    // Optional<ChatMessage> m = cr.findById(chatid);
    // String fromid = m.get().getFromId();
    // final User senderInfo = ur.findById(fromid).get();
    // try {
    // fs.sendNotification(new NotificationRequest(senderInfo.getFcmtoken(),
    // "hi~!!", "i am fxxking bug!"),
    // Map.of("notitype", "reply", "chatid", chatid));
    // } catch (FirebaseMessagingException e) {
    // e.printStackTrace();
    // }
    // return cr.updateIsreadMsg(chatid);
    // }

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
                    "audienceList", audienceList,
                    "unreadmsgcount", 0)));
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

        // List<ChatMessage> lll = new ArrayList<>();
        // lll.addAll(cr.findByfromIdAndtoId(idEmailTable.get(me),
        // idEmailTable.get(audience)));
        // lll.addAll(cr.findByfromIdAndtoId(idEmailTable.get(audience),
        // idEmailTable.get(me)));
        // Collections.sort(lll);
        // Collections.reverse(lll);
        // boolean isIamRead = false;

        // List<Map<String, String>> result = new ArrayList<>();

        // for (ChatMessage cm : lll) {
        // if (cm.getIsreadmsg() == false && cm.getToId().equals(idEmailTable.get(me)))
        // {
        // isIamRead = true;
        // cr.updateIsreadMsg(cm.getId());
        // }
        // String timeString = cm.getTimestamp().toString();

        // if (cm.getFromId().equals(idEmailTable.get(me))) {
        // result.add(Map.of("chatId", cm.getId(),
        // "fromEmail", me,
        // "toEmail", audience,
        // "text", cm.getChatDetail(),
        // "timeStamp", timeString.substring(0, timeString.length() - 2),
        // "isread", cm.getIsreadmsg() + ""));
        // } else {
        // result.add(Map.of("chatId", cm.getId(),
        // "fromEmail", audience,
        // "toEmail", me,
        // "text", cm.getChatDetail(),
        // "timeStamp", timeString.substring(0, timeString.length() - 2),
        // "isread", "none"));
        // }
        // }
        // final User opne = ur.findByuserid(idEmailTable.get(audience)).get(0);

        // if (isIamRead) {
        // try {
        // fs.sendNotification(new NotificationRequest(opne.getFcmtoken(), "hi~!!", "iam
        // fxxking bug!"),
        // Map.of("notitype", "reply", "chatid", "allMessage"));
        // } catch (FirebaseMessagingException e) {
        // e.printStackTrace();
        // }
        // }

        return response;
    }

    @GetMapping("/get-newusers")
    public List<Map<String, String>> getNewUsers(@RequestParam(value = "me") String me) {

        System.out.println("in get-newusers me : " + me);
        // 처음부터 방이 존재하지 않았거나
        // 내가 나갔거나
        // 한명이 추가되서 단톡방이 되어버렸거나
        // 내가 나갔는데 한명이 추가되서 단톡이 되어버렸거나

        List<Map<String, String>> response = new ArrayList<Map<String, String>>();

        List<User> allUsers = userRepository.findAll();
        Set<String> s = new HashSet<>();
        s.add(me);

        for (String roomid : chatroomuserRepository.findRoomidByEmail(me)) {
            // 해당 톡방에 state 고려하지 않고 참여한 모든 유저목록을 가져온다.
            List<Chatroomuser> affiliatedUsersInChatroom = chatroomuserRepository.findByRoomid(roomid);

            if (affiliatedUsersInChatroom.size() <= 2) {
                affiliatedUsersInChatroom.stream().forEach(u -> s.add(u.getEmail()));
                // 갠톡방에 존재하므로 추가가능친구목록에서 제외된다. 즉, 새로운 유저 목록에 보이지 않게 된다.
            }
        }

        for (User u : allUsers) {
            if (!s.contains(u.getEmail()))
                response.add(Map.of("userEmail", u.getEmail()));
        }

        return response;
    }

    @PostMapping("/newchat")
    public Map<String, Object> startNewChat(@RequestParam(value = "me") String me,
            @RequestParam(value = "audience") String audience) {

        // 너랑나 단둘이만 있는 갠톡을 찾는다.
        // 없다면 새로 생성한다. chatroom.save()
        // 있다면 true로 바꿔준다. 이미 true인경우는 정상적이라면 없어야 함(있어도 문제는 안됨)

        boolean isFindWeAreAlone = true;
        Chatroom recentChatroom = null;

        for (Chatroom chatroom : chatroomRepository.findAll()) {
            Set<String> s = new HashSet<>(
                    chatroomuserRepository.findByRoomid(chatroom.getRoomid()).stream().map(Chatroomuser::getEmail)
                            .collect(toCollection(HashSet::new)));
            boolean pred = Arrays.asList(me, audience).stream().allMatch(u -> s.contains(u)) && s.size() == 2;

            if (pred) {
                System.out.println("if로 들어왔다고??");
                isFindWeAreAlone = false;
                recentChatroom = chatroom;
                chatroomuserRepository.updateChatroomuserState(chatroom.getRoomid(), me);
                break;
            }
        }

        if (isFindWeAreAlone) {
            // 새로운 톡방 개설
            final String roomid = UUID.randomUUID() + "";
            chatroomRepository.save(new Chatroom(roomid, 2, ""));
            chatroomuserRepository.save(new Chatroomuser(UUID.randomUUID() + "", roomid, me, me, true));
            chatroomuserRepository.save(new Chatroomuser(UUID.randomUUID() + "", roomid, audience, audience, true));
            return Map.of("chatroomid", roomid,
                    "text", "",
                    "timestamp", Utility.getCurrentDateTimeAsString(),
                    "audienceList", Arrays.asList(audience),
                    "unreadmsgcount", 0);
        } else {
            return Map.of("chatroomid", recentChatroom.getRoomid(),
                    "text", recentChatroom.getRecent_detail(),
                    "timestamp", recentChatroom.getRecentTimestamAsString(),
                    "audienceList", Arrays.asList(audience),
                    "unreadmsgcount", 0);
        }
    }

    @PostMapping("/exit-chat")
    public String exitChattingRoom(@RequestParam(value = "me") String me,
            @RequestParam(value = "chatroomid") String chatroomid) {
        int result = chatroomuserRepository.updateChatroomuserStateToFalse(chatroomid, me);
        return result + "";
    }
}
