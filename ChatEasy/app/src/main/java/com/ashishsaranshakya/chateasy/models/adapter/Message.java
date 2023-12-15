package com.ashishsaranshakya.chateasy.models.adapter;

public class Message implements java.io.Serializable{
    private String content;
    private String sender;
    private String chatId;
    private boolean isSent;

    public Message(String chatId, String sender, String content, boolean isSent) {
        this.content = content;
        this.sender = sender;
        this.chatId = chatId;
        this.isSent = isSent;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public String getContent() {
        return content;
    }

    public String getSender() {
        return sender;
    }

    public String getChatId() {
        return chatId;
    }
}
