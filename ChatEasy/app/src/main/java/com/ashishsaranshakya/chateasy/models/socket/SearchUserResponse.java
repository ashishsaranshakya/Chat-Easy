package com.ashishsaranshakya.chateasy.models.socket;

import java.io.Serializable;
import java.util.List;

public class SearchUserResponse implements java.io.Serializable{
    private boolean success;
    private List<User> users;

    public List<User> getUsers() {
        return users;
    }

    @Override
    public String toString() {
        return "SearchUserResponse{" +
                "success=" + success +
                ", users=" + users +
                '}';
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public User getUser(int index){
        return users.get(index);
    }

    public String getUserId(int index){
        return users.get(index).get_id();
    }

    public String getUsername(int index){
        return users.get(index).getUsername();
    }


    public class User implements Serializable {
        String _id;
        String username;

        public String get_id() {
            return _id;
        }

        @Override
        public String toString() {
            return "User{" +
                    "_id='" + _id + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
