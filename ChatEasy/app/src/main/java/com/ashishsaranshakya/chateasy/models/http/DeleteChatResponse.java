package com.ashishsaranshakya.chateasy.models.http;

import com.google.gson.annotations.SerializedName;

public class DeleteChatResponse implements java.io.Serializable{
    @SerializedName("chatId")
    private String chatId;

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @Override
    public String toString() {
        return "DeleteChatResponse{" +
                "chatId='" + chatId + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public DeleteChatResponse(Boolean success, String message, String chatId) {
        this.chatId = chatId;
        this.success = success;
        this.message = message;
    }
}



