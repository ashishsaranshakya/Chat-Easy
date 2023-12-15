package com.ashishsaranshakya.chateasy.activities;

import static com.ashishsaranshakya.chateasy.activities.ChatsActivity.CONTRACT_OBJECT_CHAT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashishsaranshakya.chateasy.R;
import com.ashishsaranshakya.chateasy.adapters.UserAdapter;
import com.ashishsaranshakya.chateasy.models.http.CreateChatResponse;
import com.ashishsaranshakya.chateasy.models.socket.SearchUserResponse;
import com.ashishsaranshakya.chateasy.services.SocketClientHandler;

import java.util.List;

public class SearchUserActivity extends AppCompatActivity implements TextWatcher {
    RecyclerView searchUserList;
    EditText searchUser;
    private UserAdapter userAdapter;

    private final BroadcastReceiver searchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Object object = intent.getSerializableExtra("users");
            // assert class List<SearchUserResponse.User>
            assert object instanceof List;
            List<SearchUserResponse.User> response = (List<SearchUserResponse.User>) object;
            //List<SearchUserResponse.User> response = (List<SearchUserResponse.User>) intent.getSerializableExtra("users");
            Log.w("SearchUserActivity", "onReceive: "+response);
            userAdapter.setUsers(response);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        searchUserList = findViewById(R.id.usersRecyclerView);
        searchUser = findViewById(R.id.searchUser);

        searchUser.addTextChangedListener(this);

        userAdapter = new UserAdapter(getLayoutInflater(), this);
        searchUserList.setAdapter(userAdapter);
        searchUserList.setLayoutManager(new LinearLayoutManager(this));

        registerReceiver(searchReceiver, new IntentFilter(SocketClientHandler.SOCKET_EVENT_SEARCH), Context.RECEIVER_NOT_EXPORTED);
    }

    public void searchFinished(CreateChatResponse response){
        unregisterReceiver(searchReceiver);
        Log.w("SearchUserActivity", "searchFinished: "+response);
        Intent intent = new Intent();
        intent.putExtra(CONTRACT_OBJECT_CHAT, response);
        setResult(RESULT_OK, intent);
        finish();
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
        handler.searchUser(s.toString());
    }
}