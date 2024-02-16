package com.azurelight.capstone_2.Service;

public enum AppStateEnum {
    BACKGROUND("background"),
    INACTIVE("inactive"),
    FOREGROUND("foreground"),
    TERMINATED("terminated");

    private final String label;

    AppStateEnum(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
