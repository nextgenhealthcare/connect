package com.webreach.mirth.model;

public class UsageData {
    private int key;
    private String value;
    private String groupId;

    public UsageData(String groupId, int key, String value) {
        this.groupId = groupId;
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
