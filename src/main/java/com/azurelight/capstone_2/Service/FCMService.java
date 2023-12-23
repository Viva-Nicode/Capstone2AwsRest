package com.azurelight.capstone_2.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.azurelight.capstone_2.Service.Noti.NotificationRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FCMService {

    @Autowired
    private final FirebaseMessaging firebaseMessaging;

    public void sendNotification(NotificationRequest notificationRequest, Map<String, String> data)
            throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(notificationRequest.deviceToken())
                .setNotification(notificationRequest.toNotification())
                .setApnsConfig(ApnsConfig.builder().setAps(Aps.builder().setSound("default").build()).build())
                .putAllData(data)
                .build();

        firebaseMessaging.send(message);
    }

    public void sendNotificationAll(List<NotificationRequest> notificationRequestList, Map<String, String> data)
            throws FirebaseMessagingException {
        List<Message> messageList = new ArrayList<>();
        for (NotificationRequest Notification : notificationRequestList) {
            messageList.add(Message.builder()
                .setToken(Notification.deviceToken())
                .setNotification(Notification.toNotification())
                .setApnsConfig(ApnsConfig.builder().setAps(Aps.builder().setSound("default").build()).build())
                .putAllData(data)
                .build());
        }
        firebaseMessaging.sendAll(messageList);
    }
}
