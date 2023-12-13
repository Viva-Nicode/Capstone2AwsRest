package com.azurelight.capstone_2.Controller;

import java.util.UUID;

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

import com.azurelight.capstone_2.Repository.ChatMessageRepository;
import com.azurelight.capstone_2.Repository.UserRepository;
import com.azurelight.capstone_2.Service.FCMService;
import com.azurelight.capstone_2.Service.Noti.NotificationRequest;
import com.azurelight.capstone_2.db.ChatMessage;
import com.azurelight.capstone_2.db.User;
import com.google.firebase.messaging.FirebaseMessagingException;

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
        List<Map<String, String>> result = new ArrayList<>();

        for (ChatMessage cm : lll) {
            result.add(Map.of("chatId", cm.getId(),
                    "fromEmail", me,
                    "toEmail", audience,
                    "text", cm.getChatDetail(),
                    "timeStamp", cm.getTimestamp().toString()));
        }
        return result;
    }
}
