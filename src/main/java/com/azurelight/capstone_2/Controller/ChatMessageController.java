package com.azurelight.capstone_2.Controller;

import java.util.UUID;
import java.util.Date;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.Comparator;

import com.azurelight.capstone_2.Repository.ChatMessageRepository;
import com.azurelight.capstone_2.Repository.UserRepository;
import com.azurelight.capstone_2.Service.FCMService;
import com.azurelight.capstone_2.Service.Noti.NotificationRequest;
import com.azurelight.capstone_2.db.ChatMessage;
import com.azurelight.capstone_2.db.User;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/chat")
public class ChatMessageController {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private UserRepository ur;

    @Autowired
    private ChatMessageRepository cr;

    @Autowired
    private FCMService fs;

    @PostMapping("/send-msg")
    public String sendMessage(@RequestParam(value = "sender") String sender,
            @RequestParam(value = "receiver") String receiver, @RequestParam(value = "detail") String detail) {

        final Map<String, String> idEmailTable = new HashMap<>(
                Map.of(sender, ur.findByEmail(sender).get(0).getId(),
                        receiver, ur.findByEmail(receiver).get(0).getId()));
        final String chatid = UUID.randomUUID() + "";

        final ChatMessage insertedEntity = cr
                .save(new ChatMessage(chatid, idEmailTable.get(sender), idEmailTable.get(receiver), detail,
                        null, false));
        final User u = ur.findByEmail(receiver).get(0);

        ZoneId seoulZone = ZoneId.of("Asia/Seoul");

        LocalDateTime seoulTime = LocalDateTime.now(seoulZone);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedSeoulTime = seoulTime.format(formatter);
        log.info("in con : " + formattedSeoulTime);

        try {
            fs.sendNotification(new NotificationRequest(u.getFcmtoken(), sender, detail),
                    Map.of("notitype", "receive", "chatid", insertedEntity.getId(), "detail",
                            insertedEntity.getChatDetail(),
                            "timestamp", formattedSeoulTime));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
        return chatid + "/" + formattedSeoulTime;
    }

    @PostMapping("/readmsg")
    public int reagMessage(@RequestParam(value = "chatid") String chatid) {
        Optional<ChatMessage> m = cr.findById(chatid);
        String fromid = m.get().getFromId();
        final User senderInfo = ur.findById(fromid).get();
        try {
            fs.sendNotification(new NotificationRequest(senderInfo.getFcmtoken(), "hi~!!", "i am fxxking bug!"),
                    Map.of("notitype", "reply", "chatid", chatid));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
        return cr.updateIsreadMsg(chatid);
        // 여기다가 메시지 보낸놈한테 읽었다고 노티 줘야함
    }

    @GetMapping("/get-recentmsg")
    public List<HashMap<String, String>> getRecentMessages(@RequestParam(value = "me") String me) {
        final String me_id = ur.findByEmail(me).get(0).getId();
        List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();

        @AllArgsConstructor
        @Getter
        class Msg {
            String id;
            Date date;
            String text;
        }

        List<ChatMessage> logs = cr.findAllLogs(me_id);
        Map<String, Msg> hm = new HashMap<>();
        Map<String, Integer> unreadMessageCountMap = new HashMap<>();

        for (ChatMessage cm : logs) {
            String opneid = cm.getFromId().equals(me_id) ? cm.getToId() : cm.getFromId();
            final User opne = ur.findByuserid(opneid).get(0);

            if (unreadMessageCountMap.containsKey(opne.getEmail())) {
                if (cm.getIsreadmsg() == false && cm.getToId().equals(me_id))
                    unreadMessageCountMap.put(opne.getEmail(), unreadMessageCountMap.get(opne.getEmail()) + 1);
            } else {
                if (cm.getIsreadmsg() == false && cm.getToId().equals(me_id))
                    unreadMessageCountMap.put(opne.getEmail(), 1);
                else
                    unreadMessageCountMap.put(opne.getEmail(), 0);
            }

            if (hm.containsKey(opne.getEmail())) {
                if (hm.get(opne.getEmail()).date.compareTo(cm.getTimestamp()) < 0)
                    hm.put(opne.getEmail(), new Msg(opneid, cm.getTimestamp(), cm.getChatDetail()));
            } else
                hm.put(opne.getEmail(), new Msg(opneid, cm.getTimestamp(), cm.getChatDetail()));
        }
        System.out.println(unreadMessageCountMap);

        for (Map.Entry<String, Msg> elem : hm.entrySet()) {
            result.add(new HashMap<String, String>(Map.of("fromEmail", elem.getKey(),
                    "timeStamp", elem.getValue().date + "",
                    "text", elem.getValue().text,
                    "recentMessageId", elem.getValue().id,
                    "profileImagePath", "http://52.78.99.139:8080/rest/get-profile/" + elem.getKey(),
                    "unreadmsgcount", unreadMessageCountMap.get(elem.getKey()) + "")));
        }

        Collections.sort(result, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                return o1.get("timestamp").compareTo(o2.get("timestamp"));
            }
        });

        return result;
    }

    @GetMapping("/get-chatlogs")
    public List<Map<String, String>> getChatlogs(@RequestParam(value = "me") String me,
            @RequestParam(value = "audience") String audience) {

        final Map<String, String> idEmailTable = new HashMap<>(
                Map.of(audience, ur.findByEmail(audience).get(0).getId(),
                        me, ur.findByEmail(me).get(0).getId()));

        List<ChatMessage> lll = new ArrayList<>();
        lll.addAll(cr.findByfromIdAndtoId(idEmailTable.get(me), idEmailTable.get(audience)));
        lll.addAll(cr.findByfromIdAndtoId(idEmailTable.get(audience), idEmailTable.get(me)));
        Collections.sort(lll);
        Collections.reverse(lll);

        List<Map<String, String>> result = new ArrayList<>();

        for (ChatMessage cm : lll) {
            if (cm.getIsreadmsg() == false && cm.getToId().equals(idEmailTable.get(me)))
                cr.updateIsreadMsg(cm.getId());

            if (cm.getFromId().equals(idEmailTable.get(me))) {
                result.add(Map.of("chatId", cm.getId(),
                        "fromEmail", me,
                        "toEmail", audience,
                        "text", cm.getChatDetail(),
                        "timeStamp", cm.getTimestamp().toString(),
                        "isread", cm.getIsreadmsg() + ""));
            } else {
                result.add(Map.of("chatId", cm.getId(),
                        "fromEmail", audience,
                        "toEmail", me,
                        "text", cm.getChatDetail(),
                        "timeStamp", cm.getTimestamp().toString(),
                        "isread", "none"));
            }
        }
        return result;
    }
}
