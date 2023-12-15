package com.ashishsaranshakya.chateasy.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.ashishsaranshakya.chateasy.Util;
import com.ashishsaranshakya.chateasy.models.adapter.Chat;
import com.ashishsaranshakya.chateasy.models.adapter.Message;
import com.ashishsaranshakya.chateasy.models.socket.SearchUserResponse;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketClientHandler {
    private static final String TAG = "SocketClientHandler";
    private static final String SOCKET_SERVER_URL = "http://10.0.2.2:3000/";
////////////////

    public static final String SOCKET_EVENT_CHATS = "chats";
    public static final String SOCKET_EVENT_MESSAGE = "message";
    public static final String SOCKET_EVENT_SINGLE_CHAT = "single chat";
    public static final String SOCKET_EVENT_JOIN_CHAT = "join chat";
    public static final String SOCKET_EVENT_LEAVE_CHAT = "leave chat";
    public static final String SOCKET_EVENT_SEARCH = "search";
    public static final String SOCKET_EVENT_SEARCH_GROUP = "search group";

////////////////
    public static final String SOCKET_OBJECT_CHAT = "chat";
    public static final String SOCKET_OBJECT_MESSAGE = "message";
    private Socket socket;
    private Context context;

    private static SocketClientHandler instance;

    private SocketClientHandler(Context context) {
        try {
            this.context = context;
            SharedPreferences sharedPreferences = Util.getEncryptedSharedPreferences(context);
            IO.Options options = new IO.Options();
            options.auth = new HashMap<>();
            options.auth.put("session", sharedPreferences.getString("session", ""));

            socket = IO.socket(SOCKET_SERVER_URL, options);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static SocketClientHandler getInstance(Context context) {
        if (instance == null) {
            instance = new SocketClientHandler(context);
            instance.connectToSocket();
            instance.receiveMessage();
        }
        return instance;
    }

    private void connectToSocket() {
        socket.connect();
    }

    public static void deleteConnection() {
        if (instance != null && instance.socket.connected()) {
            instance.socket.disconnect();
            instance = null;
        }
    }

    public void getChats() {
        socket.emit(SOCKET_EVENT_CHATS);
    }

    public void sendMessage(String chatId, String content) {
        JSONObject data = new JSONObject();
        try {
            data.put("chatId", chatId);
            data.put("content", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.w(TAG, "Sending message " + data);
        socket.emit(SOCKET_EVENT_MESSAGE, data);
    }

    public void getChat(String chatId){
        JSONObject data = new JSONObject();
        try {
            data.put("chatId", chatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit(SOCKET_EVENT_SINGLE_CHAT, data);
    }

    public void searchUser(String query){
        JSONObject data = new JSONObject();
        try{
            data.put("query", query);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        socket.emit(SOCKET_EVENT_SEARCH, data);
    }

    public void searchUserForGroup(String chatId, String query){
        JSONObject data = new JSONObject();
        try{
            data.put("id", chatId);
            data.put("search", query);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        socket.emit(SOCKET_EVENT_SEARCH_GROUP, data);
    }

    public void joinChat(String chatId){
        JSONObject data = new JSONObject();
        try{
            data.put("chatId", chatId);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        socket.emit(SOCKET_EVENT_JOIN_CHAT, data);
    }

    public void leaveChat(String chatId){
        JSONObject data = new JSONObject();
        try{
            data.put("chatId", chatId);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        socket.emit(SOCKET_EVENT_LEAVE_CHAT, data);
    }

    private void receiveMessage() {
        socket.on(SOCKET_EVENT_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args[0] instanceof JSONObject) {
                    JSONObject data = (JSONObject) args[0];
                    Intent intent = new Intent(SOCKET_EVENT_MESSAGE);
                    try {
                        Message msg = new Message(
                                data.getString("chat"),
                                data.getString("sender"),
                                data.getString("content"),
                                data.getString("sender").equals(
                                        Util.getEncryptedSharedPreferences(context).getString("username", "")
                                )
                        );
                        intent.putExtra(SOCKET_OBJECT_MESSAGE, msg);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    SocketClientHandler.this.context.sendBroadcast(intent);
                }
            }
        });

        socket.on(SOCKET_EVENT_CHATS, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.w(TAG, "Chats received "+ args[0].toString());
                if (args[0] instanceof JSONObject) {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String _id = data.getString("chatId");
                        String content = data.getString("lastMessage");
                        String name = data.getString("name");
                        boolean isGroup = data.getBoolean("isGroup");
                        boolean isAdmin = data.getBoolean("isAdmin");
                        Chat chat = new Chat(_id, name, content,isGroup, isAdmin);
                        Intent intent = new Intent(SOCKET_EVENT_CHATS);
                        intent.putExtra(SOCKET_OBJECT_CHAT, chat);
                        SocketClientHandler.this.context.sendBroadcast(intent);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        socket.on(SOCKET_EVENT_SINGLE_CHAT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args[0] instanceof JSONObject) {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String chatId = data.getString("chatId");
                        JSONArray messages = data.getJSONArray("messages");
                        for (int i = 0; i < messages.length(); i++) {
                            JSONObject message = messages.getJSONObject(i);
                            Intent intent = new Intent(SOCKET_EVENT_MESSAGE);
                            Message msg = new Message(
                                    chatId,
                                    message.getString("sender"),
                                    message.getString("content"),
                                    message.getString("sender").equals(
                                            Util.getEncryptedSharedPreferences(context).getString("username", "")
                                    )
                            );
                            intent.putExtra(SOCKET_OBJECT_MESSAGE, msg);
                            SocketClientHandler.this.context.sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        socket.on(SOCKET_EVENT_SEARCH, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args[0] instanceof JSONObject) {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        Gson gson = new Gson();
                        SearchUserResponse response = gson.fromJson(data.toString(), SearchUserResponse.class);
                        Intent intent = new Intent(SOCKET_EVENT_SEARCH);
                        Log.w(TAG, "Search received "+response.toString());
                        intent.putExtra("users", (Serializable) response.getUsers());
                        SocketClientHandler.this.context.sendBroadcast(intent);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        socket.on(SOCKET_EVENT_SEARCH_GROUP, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args[0] instanceof JSONObject) {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        Gson gson = new Gson();
                        SearchUserResponse response = gson.fromJson(data.toString(), SearchUserResponse.class);
                        Intent intent = new Intent(SOCKET_EVENT_SEARCH_GROUP);
                        intent.putExtra("users", (Serializable) response.getUsers());
                        SocketClientHandler.this.context.sendBroadcast(intent);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}