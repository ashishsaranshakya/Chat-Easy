package com.ashishsaranshakya.chateasy.models.http;

import com.google.gson.annotations.SerializedName;

public class CreateGroupRequest {
    @SerializedName("name")
    private String name;

    public CreateGroupRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
