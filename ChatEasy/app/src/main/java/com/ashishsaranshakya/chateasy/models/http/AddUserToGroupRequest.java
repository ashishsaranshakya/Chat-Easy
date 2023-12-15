package com.ashishsaranshakya.chateasy.models.http;

import com.google.gson.annotations.SerializedName;

public class AddUserToGroupRequest {
    @SerializedName("userId")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public AddUserToGroupRequest(String userId) {
        this.userId = userId;
    }
}