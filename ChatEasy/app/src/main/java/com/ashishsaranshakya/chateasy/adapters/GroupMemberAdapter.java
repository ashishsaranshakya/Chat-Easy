package com.ashishsaranshakya.chateasy.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class GroupMemberAdapter extends RecyclerView.Adapter {
    private final LayoutInflater inflater;
    private List<SearchUserResponse.User> users = new ArrayList<>();
    private String chatId;
    private boolean isAdmin;
    public GroupMemberAdapter(LayoutInflater inflater, String chatId, boolean isAdmin) {
        this.inflater = inflater;
        this.chatId = chatId;
        this.isAdmin = isAdmin;
    }

    private class UserHolder extends RecyclerView.ViewHolder {
        TextView nameTxt;
        ImageView image, removeBtn;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            nameTxt = itemView.findViewById(R.id.nameTxt);
            image = itemView.findViewById(R.id.image);
            removeBtn = itemView.findViewById(R.id.removeUser);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_group_member, parent, false);
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

        userHolder.removeBtn.setOnClickListener(v -> {
            HttpService httpService = Util.getHttpService(v.getContext());
            String token = Util.getEncryptedSharedPreferences(v.getContext()).getString("session", "");
            httpService.removeUserFromGroup(token, chatId, new AddUserToGroupRequest(user.get_id()))
                    .enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                            if(response.body().getSuccess()){
                                Toast.makeText(v.getContext(), "User removed successfully", Toast.LENGTH_SHORT).show();
                                users.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                            }
                            else{
                                Toast.makeText(v.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                            Toast.makeText(v.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        if(!isAdmin){
            userHolder.removeBtn.setVisibility(View.GONE);
        }
        else if(user.get_id().equals(Util.getEncryptedSharedPreferences(holder.itemView.getContext()).getString("userId", ""))){
            userHolder.removeBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<SearchUserResponse.User> users) {
        this.users = users;
        notifyDataSetChanged();
    }
}
