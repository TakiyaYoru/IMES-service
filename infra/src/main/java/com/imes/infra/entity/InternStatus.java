package com.imes.infra.entity;

public enum InternStatus {
    NEW("Mới"),
    ACTIVE("Đang thực tập"),
    ON_LEAVE("Đang nghỉ phép"),
    COMPLETED("Hoàn thành"),
    TERMINATED("Chấm dứt");

    private final String displayName;

    InternStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
