package com.azurelight.capstone_2.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Utility {
    public static String getCurrentDateTimeAsString() {
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        LocalDateTime seoulTime = LocalDateTime.now(seoulZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedSeoulTime = seoulTime.format(formatter);
        return formattedSeoulTime;
    }
}
