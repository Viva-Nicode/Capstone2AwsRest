package com.azurelight.capstone_2.Controller;

import java.util.UUID;
import java.util.Date;

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
                        null));

        final User u = ur.findByEmail(receiver).get(0);

        try {
            fs.sendNotification(new NotificationRequest(u.getFcmtoken(), sender, detail));
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }

        return chatid + "/" + insertedEntity.getTimestamp();
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

        // 키 : 상대 이메일
        // 값 : 시간, 내용, 아이디

        List<ChatMessage> logs = cr.findAllLogs(me_id);
        Map<String, Msg> hm = new HashMap<>();

        for (ChatMessage cm : logs) {
            String opneid = cm.getFromId().equals(me_id) ? cm.getToId() : cm.getFromId();
            final User opne = ur.findByuserid(opneid).get(0);
            if (hm.containsKey(opne.getEmail())) {
                // 키 이미 있다면
                if (hm.get(opne.getEmail()).date.compareTo(cm.getTimestamp()) < 0) {
                    // 기존 맵에 있던거보다 최신이라면
                    hm.put(opne.getEmail(), new Msg(opneid, cm.getTimestamp(), cm.getChatDetail()));
                }
            } else {
                hm.put(opne.getEmail(), new Msg(opneid, cm.getTimestamp(), cm.getChatDetail()));
            }
        }

        for (Map.Entry<String, Msg> elem : hm.entrySet()) {
            result.add(new HashMap<String, String>(Map.of("fromEmail", elem.getKey(),
                    "timeStamp", elem.getValue().date + "",
                    "text", elem.getValue().text,
                    "recentMessageId", elem.getValue().id,
                    "profileImagePath", "http://52.78.99.139:8080/rest/get-profile/" + elem.getKey())));
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
            if (cm.getFromId().equals(idEmailTable.get(me))) {
                result.add(Map.of("chatId", cm.getId(),
                        "fromEmail", me,
                        "toEmail", audience,
                        "text", cm.getChatDetail(),
                        "timeStamp", cm.getTimestamp().toString()));
            } else {
                result.add(Map.of("chatId", cm.getId(),
                        "fromEmail", audience,
                        "toEmail", me,
                        "text", cm.getChatDetail(),
                        "timeStamp", cm.getTimestamp().toString()));
            }
        }
        return result;
    }
}
