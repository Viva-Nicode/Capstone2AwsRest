package com.azurelight.capstone_2.Service;

import com.google.firebase.messaging.Notification;

import lombok.Builder;

public class Noti {
    public record NotificationRequest(String deviceToken, String title, String body) {

        @Builder
        public NotificationRequest {
        }

        public Notification toNotification() {
            return Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();
        }
    }

}
