package com.ashishsaranshakya.chateasy.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashishsaranshakya.chateasy.R;
import com.ashishsaranshakya.chateasy.Util;
import com.ashishsaranshakya.chateasy.models.http.AddUserToGroupRequest;
import com.ashishsaranshakya.chateasy.models.http.GenericResponse;
import com.ashishsaranshakya.chateasy.models.socket.SearchUserResponse;
import com.ashishsaranshakya.chateasy.services.HttpService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddUserGroupAdapter extends RecyclerView.Adapter {

    private final LayoutInflater inflater;
    private List<SearchUserResponse.User> users = new ArrayList<>();
    private String chatId;

    public AddUserGroupAdapter(LayoutInflater inflater, String chatId) {
        this.inflater = inflater;
        this.chatId = chatId;
    }

    private class UserHolder extends RecyclerView.ViewHolder {

        TextView nameTxt;
        ImageView image;
        RelativeLayout parent;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            nameTxt = itemView.findViewById(R.id.nameTxt);
            image = itemView.findViewById(R.id.image);
            parent = itemView.findViewById(R.id.chat_container);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_user, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SearchUserResponse.User user = users.get(position);
        UserHolder userHolder = (UserHolder) holder;
        userHolder.nameTxt.setText(user.getUsername());
        String username = user.getUsername();
        if (username != null && !username.isEmpty()) {
            char firstLetter = username.charAt(0);
            if (firstLetter >= 'a' && firstLetter <= 'z') {
                firstLetter -= 32;
            }
            Bitmap iconBitmap = Util.generateIconForLetter(firstLetter, holder.itemView.getContext());
            userHolder.image.setImageBitmap(iconBitmap);
        }
        userHolder.parent.setOnClickListener(v -> {
            HttpService httpService = Util.getHttpService();
            String token = Util.getEncryptedSharedPreferences(v.getContext()).getString("session", "");
            AddUserToGroupRequest addUserToGroupRequest = new AddUserToGroupRequest(user.get_id());
            httpService.addUserToGroup(token, chatId, addUserToGroupRequest)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful()) {
                            GenericResponse genericResponse = response.body();
                            if (genericResponse != null && genericResponse.getSuccess()) {
                                userAdded(holder.getAdapterPosition());
                                Toast.makeText(v.getContext(), "User added successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(v.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(v.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        Toast.makeText(v.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void addItem (SearchUserResponse.User user) {
        users.add(user);
        notifyItemInserted(users.size() - 1);
    }

    public void clear() {
        users.clear();
        notifyItemRangeRemoved(0, users.size()-1);
    }

    public void setUsers(List<SearchUserResponse.User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public void userAdded(int position){
        this.users.remove(position);
        notifyItemRemoved(position);
    }
}
