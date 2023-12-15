package com.ashishsaranshakya.chateasy.activities;

import static com.ashishsaranshakya.chateasy.activities.ChatsActivity.CONTRACT_OBJECT_DELETION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashishsaranshakya.chateasy.R;
import com.ashishsaranshakya.chateasy.Util;
import com.ashishsaranshakya.chateasy.adapters.MessageAdapter;
import com.ashishsaranshakya.chateasy.models.adapter.Message;
import com.ashishsaranshakya.chateasy.models.http.DeleteChatResponse;
import com.ashishsaranshakya.chateasy.services.SocketClientHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private static final int MENU_ADD = 1;
    private static final int MENU_LEAVE = 2;
    private static final int MENU_DELETE = 3;
    private static final int MENU_DETAILS = 4;
    RecyclerView messageRecyclerView;
    MessageAdapter messageAdapter;
    EditText messageTxt;
    FloatingActionButton sendBtn;
    String chatId;
    boolean isGroup;
    boolean isAdmin;

    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message message = (Message) intent.getSerializableExtra(SocketClientHandler.SOCKET_OBJECT_MESSAGE);
            assert message != null;
            if(!message.getChatId().equals(chatId))
                return;
            messageAdapter.addItem(message);
            int itemCount = messageAdapter.getItemCount();
            if (itemCount > 0) {
                messageRecyclerView.smoothScrollToPosition(itemCount - 1);
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        chatId = intent.getStringExtra("chatId");
        String chatName = intent.getStringExtra("chatName");
        isGroup = intent.getBooleanExtra("isGroup", false);
        isAdmin = intent.getBooleanExtra("isAdmin", false);
        setTitle(chatName);
        SocketClientHandler.getInstance(this).getChat(chatId);

        messageTxt = findViewById(R.id.edtMessage);
        sendBtn = findViewById(R.id.btnSend);

        sendBtn.setOnClickListener(v -> {
            if(messageTxt.getText().toString().isEmpty())
                return;
            SocketClientHandler.getInstance(this).sendMessage(
                    chatId,
                    messageTxt.getText().toString()
            );

            Message message = new Message(
                    "",
                    "",
                    messageTxt.getText().toString(),
                    true
            );
            messageTxt.setText("");
            messageAdapter.addItem(message);
            int itemCount = messageAdapter.getItemCount();
            if (itemCount > 0) {
                messageRecyclerView.smoothScrollToPosition(itemCount - 1);
            }
        });

        messageRecyclerView = findViewById(R.id.chats);
        messageAdapter = new MessageAdapter(getLayoutInflater());
        messageRecyclerView.setAdapter(messageAdapter);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        registerReceiver(messageReceiver, new IntentFilter(SocketClientHandler.SOCKET_EVENT_MESSAGE), Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if(isGroup)
            menu.add(0, MENU_DETAILS, Menu.NONE, "Group details").setIcon(null);
        menu.add(0, MENU_LEAVE, Menu.NONE, "Leave Chat").setIcon(R.drawable.ic_leave);
        if(isAdmin) {
            menu.add(0, MENU_DELETE, Menu.NONE, "Delete group").setIcon(R.drawable.ic_delete);
            menu.add(0, MENU_ADD, Menu.NONE, "Add members").setIcon(R.drawable.ic_add_group);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == MENU_LEAVE) {
            leaveChat();
        }
        else if (item.getItemId() == MENU_DELETE) {
            deleteGroup();
        }
        else if (item.getItemId() == MENU_ADD) {
            Intent intent = new Intent(this, AddUserToGroupActivity.class);
            intent.putExtra("chatId", chatId);
            startActivity(intent);
        }
        else if (item.getItemId() == MENU_DETAILS) {
            Intent intent = new Intent(this, GroupMembersActivity.class);
            intent.putExtra("chatId", chatId);
            intent.putExtra("chatName", getTitle());
            intent.putExtra("isAdmin", isAdmin);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void leaveChat(){
        SharedPreferences sharedPreferences = Util.getEncryptedSharedPreferences(this);
        assert sharedPreferences != null;
        String token = sharedPreferences.getString("session", "");
        Util.getHttpService(this).deleteChat(token, chatId).enqueue(new Callback<DeleteChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeleteChatResponse> call, @NonNull Response<DeleteChatResponse> response) {
                Log.w("ChatActivity", "onResponse: " + response);
                if (response.isSuccessful()) {
                    DeleteChatResponse res = response.body();
                    if (res != null && res.getSuccess()) {
                        Toast.makeText(ChatActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra(CONTRACT_OBJECT_DELETION, res);
                        setResult(RESULT_OK, intent);
                        SocketClientHandler.getInstance(ChatActivity.this).leaveChat(chatId);
                        ChatActivity.this.finish();
                    } else {
                        Toast.makeText(ChatActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteChatResponse> call, @NonNull Throwable t) {
                Toast.makeText(ChatActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup(){
        SharedPreferences sharedPreferences = Util.getEncryptedSharedPreferences(this);
        assert sharedPreferences != null;
        String token = sharedPreferences.getString("session", "");
        Util.getHttpService(this).deleteGroup(token, chatId).enqueue(new Callback<DeleteChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeleteChatResponse> call, @NonNull Response<DeleteChatResponse> response) {
                if (response.isSuccessful()) {
                    DeleteChatResponse res = response.body();
                    if (res != null && res.getSuccess()) {
                        Toast.makeText(ChatActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra(CONTRACT_OBJECT_DELETION, res);
                        setResult(RESULT_OK, intent);
                        ChatActivity.this.finish();
                    } else {
                        Toast.makeText(ChatActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteChatResponse> call, @NonNull Throwable t) {
                Toast.makeText(ChatActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}