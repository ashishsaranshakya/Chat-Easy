package com.ashishsaranshakya.chateasy.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashishsaranshakya.chateasy.MainActivity;
import com.ashishsaranshakya.chateasy.R;
import com.ashishsaranshakya.chateasy.Util;
import com.ashishsaranshakya.chateasy.adapters.ChatAdapter;
import com.ashishsaranshakya.chateasy.models.adapter.Chat;
import com.ashishsaranshakya.chateasy.models.adapter.Message;
import com.ashishsaranshakya.chateasy.models.http.CreateChatResponse;
import com.ashishsaranshakya.chateasy.models.http.CreateGroupRequest;
import com.ashishsaranshakya.chateasy.models.http.DeleteChatResponse;
import com.ashishsaranshakya.chateasy.services.HttpService;
import com.ashishsaranshakya.chateasy.services.SocketClientHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatsActivity extends AppCompatActivity {
    public static final String CONTRACT_OBJECT_CHAT = "chat";
    public static final String CONTRACT_OBJECT_DELETION = "deletion";
    RecyclerView chats;
    private ChatAdapter chatAdapter;
    FloatingActionButton btnNewChat;
    FloatingActionButton btnCreateGroup;
    FloatingActionButton btnCreateChat;
    ActivityResultLauncher<String> searchResultLauncher;
    ActivityResultLauncher<String[]> chatDeletionLauncher;

    BroadcastReceiver allChatsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Chat chat = (Chat) intent.getSerializableExtra(SocketClientHandler.SOCKET_OBJECT_CHAT);
            chatAdapter.addItem(chat);
        }
    };

    BroadcastReceiver newChatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Chat chat = (Chat) intent.getSerializableExtra(SocketClientHandler.SOCKET_OBJECT_CHAT);
            chatAdapter.addChat(chat);
        }
    };

    BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message message = (Message) intent.getSerializableExtra(SocketClientHandler.SOCKET_OBJECT_MESSAGE);
            chatAdapter.newMessage(message.getChatId());
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        chats = findViewById(R.id.chats);
        btnNewChat = findViewById(R.id.btnNewChat);
        btnCreateGroup = findViewById(R.id.btnNewGroup);
        btnCreateChat = findViewById(R.id.btnNewUser);

        setContracts();

        chatAdapter = new ChatAdapter(getLayoutInflater(), chatDeletionLauncher);
        chats.setAdapter(chatAdapter);
        chats.setLayoutManager(new LinearLayoutManager(this));

        registerReceiver(allChatsReceiver, new IntentFilter(SocketClientHandler.SOCKET_EVENT_CHATS), Context.RECEIVER_NOT_EXPORTED);
        registerReceiver(newChatReceiver, new IntentFilter(SocketClientHandler.SOCKET_EVENT_NEW_CHAT), Context.RECEIVER_NOT_EXPORTED);

        SocketClientHandler.getInstance(this).getChats();

        btnNewChat.setOnClickListener(v -> {
            if(btnCreateChat.getVisibility() == FloatingActionButton.GONE){
                btnNewChat.setRotation(45);
                btnCreateGroup.setVisibility(FloatingActionButton.VISIBLE);
                btnCreateChat.setVisibility(FloatingActionButton.VISIBLE);
            }
            else {
                btnNewChat.setRotation(0);
                btnCreateGroup.setVisibility(FloatingActionButton.GONE);
                btnCreateChat.setVisibility(FloatingActionButton.GONE);
            }
        });

        btnCreateChat.setOnClickListener(v -> {
            searchResultLauncher.launch("");
            btnNewChat.setVisibility(FloatingActionButton.VISIBLE);
            btnCreateGroup.setVisibility(FloatingActionButton.GONE);
            btnCreateChat.setVisibility(FloatingActionButton.GONE);
        });

        btnCreateGroup.setOnClickListener(v -> {
            createGroupDialog();
            btnNewChat.setVisibility(FloatingActionButton.VISIBLE);
            btnCreateGroup.setVisibility(FloatingActionButton.GONE);
            btnCreateChat.setVisibility(FloatingActionButton.GONE);
        });
    }

    @Override
    protected void onResume() {
        registerReceiver(newMessageReceiver, new IntentFilter(SocketClientHandler.SOCKET_EVENT_MESSAGE), Context.RECEIVER_NOT_EXPORTED);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(newMessageReceiver);
        super.onPause();
    }

    private void setContracts(){
        searchResultLauncher = registerForActivityResult(
                new ActivityResultContract<String, CreateChatResponse>() {
                    @NonNull @Override
                    public Intent createIntent(@NonNull Context context, String s) {
                        return new Intent(context, SearchUserActivity.class);
                    }

                    @Override
                    public CreateChatResponse parseResult(int i, @Nullable Intent intent) {
                        if(intent == null) return null;
                        return (CreateChatResponse) intent.getSerializableExtra(CONTRACT_OBJECT_CHAT);
                    }
                },
                response -> {
                    if(response == null) return;
                    Chat chat = new Chat(
                            response.getChatId(),
                            response.getChatName(),
                            "",
                            false,
                            false
                    );
                    chatAdapter.addChat(chat);
                    SocketClientHandler.getInstance(ChatsActivity.this).joinChat(chat.getChatId());
                    chatDeletionLauncher.launch(new String[]{chat.getChatId(), chat.getChatName(), "false", "false"});
                });

        chatDeletionLauncher = registerForActivityResult(
                new ActivityResultContract<String[], DeleteChatResponse>() {
                    @Override
                    public DeleteChatResponse parseResult(int i, @Nullable Intent intent) {
                        if(intent == null) return null;
                        if(i!=RESULT_OK) return null;
                        Log.w("ChatDeletionLauncher", "parseResult: " + intent.getSerializableExtra(CONTRACT_OBJECT_DELETION));
                        return (DeleteChatResponse) intent.getSerializableExtra(CONTRACT_OBJECT_DELETION);
                    }

                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, String s[]) {
                        String chatId = s[0];
                        String chatName = s[1];
                        boolean isGroup = Boolean.parseBoolean(s[2]);
                        boolean isAdmin = Boolean.parseBoolean(s[3]);
                        Intent intent = new Intent(ChatsActivity.this, ChatActivity.class);
                        intent.putExtra("chatId", chatId);
                        intent.putExtra("chatName", chatName);
                        intent.putExtra("isGroup", isGroup);
                        intent.putExtra("isAdmin", isAdmin);
                        return intent;
                    }
                },
                new ActivityResultCallback<DeleteChatResponse>() {
                    @Override
                    public void onActivityResult(DeleteChatResponse o) {
                        if(o == null) return;
                        chatAdapter.deleteChat(o.getChatId());
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            SharedPreferences sharedPreferences = Util.getEncryptedSharedPreferences(this);
            assert sharedPreferences != null;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("session");
            editor.apply();
            SocketClientHandler.deleteConnection();
            startActivity(new Intent(this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void createGroupDialog(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.create_group_dialog, null);

        EditText input = dialogView.findViewById(R.id.inputField);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(dialogView);

        alertDialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredText = input.getText().toString();
                if(enteredText.isEmpty()) return;
                HttpService httpService = Util.getHttpService(ChatsActivity.this);
                String token = Util.getEncryptedSharedPreferences(ChatsActivity.this).getString("session", "");
                httpService.createGroup(token, new CreateGroupRequest(enteredText))
                    .enqueue(new Callback<CreateChatResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<CreateChatResponse> call, @NonNull Response<CreateChatResponse> response) {
                            if (response.isSuccessful()) {
                                CreateChatResponse chatResponse = response.body();
                                assert chatResponse != null;
                                Chat chat = new Chat(
                                        chatResponse.getChatId(),
                                        chatResponse.getChatName(),
                                        "",
                                        true,
                                        true
                                );
                                chatAdapter.addChat(chat);
                                SocketClientHandler.getInstance(ChatsActivity.this).joinChat(chat.getChatId());
                                chatDeletionLauncher.launch(new String[]{chat.getChatId(), chat.getChatName(), "true", "true"});
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<CreateChatResponse> call, @NonNull Throwable t) {
                            Toast.makeText(ChatsActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}