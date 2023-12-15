package com.ashishsaranshakya.chateasy.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashishsaranshakya.chateasy.R;
import com.ashishsaranshakya.chateasy.adapters.AddUserGroupAdapter;
import com.ashishsaranshakya.chateasy.models.socket.SearchUserResponse;
import com.ashishsaranshakya.chateasy.services.SocketClientHandler;

import java.util.List;

public class AddUserToGroupActivity extends AppCompatActivity implements TextWatcher {
    RecyclerView searchUserList;
    EditText searchUser;
    private AddUserGroupAdapter userAdapter;
    private String chatId;

    private final BroadcastReceiver searchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Object object = intent.getSerializableExtra("users");
            assert object instanceof List;
            List<SearchUserResponse.User> response = (List<SearchUserResponse.User>) object;
            userAdapter.setUsers(response);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_to_group);
        chatId = getIntent().getStringExtra("chatId");

        searchUserList = findViewById(R.id.usersRecyclerView);
        searchUser = findViewById(R.id.searchUser);

        searchUser.addTextChangedListener(this);

        userAdapter = new AddUserGroupAdapter(getLayoutInflater(), chatId);
        searchUserList.setAdapter(userAdapter);
        searchUserList.setLayoutManager(new LinearLayoutManager(this));

        registerReceiver(searchReceiver, new IntentFilter(SocketClientHandler.SOCKET_EVENT_SEARCH_GROUP), Context.RECEIVER_NOT_EXPORTED);

        findViewById(R.id.btnFinish).setOnClickListener(v -> finish());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.toString().length()==0){
            userAdapter.clear();
            return;
        }
        SocketClientHandler handler = SocketClientHandler.getInstance(this);
        handler.searchUserForGroup(chatId, s.toString());
    }
}