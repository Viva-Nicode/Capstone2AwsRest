package com.azurelight.capstone_2.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azurelight.capstone_2.Service.Noti.NotificationRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FCMService {

    @Autowired
    private final FirebaseMessaging firebaseMessaging;

    public void sendNotification(NotificationRequest notificationRequest) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(notificationRequest.deviceToken())
                .setNotification(notificationRequest.toNotification())
                .build();

        firebaseMessaging.send(message);
    }
}