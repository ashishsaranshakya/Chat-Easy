package com.ashishsaranshakya.chateasy.models.http;

import java.io.Serializable;

public class CreateChatResponse implements Serializable {
    private boolean success;
    private String message;
    private String chatId;
    private String chatName;

    @Override
    public String toString() {
        return "CreateChatResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", chatId='" + chatId + '\'' +
                ", chatName='" + chatName + '\'' +
                '}';
    }

    public CreateChatResponse(boolean success, String message, String chatId, String chatName) {
        this.success = success;
        this.message = message;
        this.chatId = chatId;
        this.chatName = chatName;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }
}
