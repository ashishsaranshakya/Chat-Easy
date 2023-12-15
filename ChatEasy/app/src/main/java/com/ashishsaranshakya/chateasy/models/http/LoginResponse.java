package com.ashishsaranshakya.chateasy.models.http;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("user")
    private User user;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "success=" + success +
                ", user=" + user +
                '}';
    }

    public boolean getSuccess() {
        return this.success;
    }

    public class User {
        @SerializedName("username")
        private String username;

        @SerializedName("_id")
        private String userId;

        @SerializedName("token")
        private String token;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getToken() {
            return token;
        }

        @Override
        public String toString() {
            return "User{" +
                    "username='" + username + '\'' +
                    ", userId='" + userId + '\'' +
                    ", token='" + token + '\'' +
                    '}';
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}



