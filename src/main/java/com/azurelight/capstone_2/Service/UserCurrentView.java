package com.azurelight.capstone_2.Service;

import java.util.Map;

import lombok.NoArgsConstructor;

import java.util.HashMap;


@NoArgsConstructor
public class UserCurrentView {

    private volatile static Map<String, AppStateEnum> userCrurentViewTable;

    public static Map<String, AppStateEnum> getInstance() {
        if (userCrurentViewTable == null) {
            synchronized (UserCurrentView.class){
                userCrurentViewTable = new HashMap<String, AppStateEnum>();
            }
        }
        return userCrurentViewTable;
    }
}
