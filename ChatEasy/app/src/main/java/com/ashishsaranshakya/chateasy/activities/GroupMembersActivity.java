package com.ashishsaranshakya.chateasy.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashishsaranshakya.chateasy.R;
import com.ashishsaranshakya.chateasy.Util;
import com.ashishsaranshakya.chateasy.adapters.GroupMemberAdapter;
import com.ashishsaranshakya.chateasy.models.socket.SearchUserResponse;
import com.ashishsaranshakya.chateasy.services.HttpService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupMembersActivity extends AppCompatActivity {

    private String chatId;
    RecyclerView usersRecyclerView;
    GroupMemberAdapter membersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);
        setTitle(getIntent().getStringExtra("chatName"));
        chatId = getIntent().getStringExtra("chatId");

        usersRecyclerView = findViewById(R.id.group_members_recycler_view);

        membersAdapter = new GroupMemberAdapter(getLayoutInflater(), chatId, getIntent().getBooleanExtra("isAdmin", false));
        usersRecyclerView.setAdapter(membersAdapter);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        HttpService httpService = Util.getHttpService(this);
        String token = Util.getEncryptedSharedPreferences(this).getString("session", "");
        httpService.getGroupMembers(token, chatId)
                .enqueue(new Callback<SearchUserResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SearchUserResponse> call, @NonNull Response<SearchUserResponse> response) {
                        if(!response.body().getSuccess()) return;
                        membersAdapter.setUsers(response.body().getUsers());
                    }

                    @Override
                    public void onFailure(@NonNull Call<SearchUserResponse> call, @NonNull Throwable t) {
                        Toast.makeText(GroupMembersActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}