package com.ashishsaranshakya.chateasy.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashishsaranshakya.chateasy.R;
import com.ashishsaranshakya.chateasy.Util;
import com.ashishsaranshakya.chateasy.models.adapter.Chat;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter {

    private LayoutInflater inflater;
    private List<Chat> chats = new ArrayList<>();
    ActivityResultLauncher<String[]> chatDeletionLauncher;
    public ChatAdapter(LayoutInflater inflater, ActivityResultLauncher<String[]> chatDeletionLauncher) {
        this.inflater = inflater;
        this.chatDeletionLauncher = chatDeletionLauncher;
    }

    private class ChatHolder extends RecyclerView.ViewHolder {

        TextView nameTxt, messageTxt;
        ImageView image, bell;
        RelativeLayout parent;


        public ChatHolder(@NonNull View itemView) {
            super(itemView);

            nameTxt = itemView.findViewById(R.id.nameTxt);
            messageTxt = itemView.findViewById(R.id.lastTxt);
            image = itemView.findViewById(R.id.image);
            parent = itemView.findViewById(R.id.chat_container);
            bell = itemView.findViewById(R.id.bell);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_chat, parent, false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chat chat = chats.get(position);
        ChatHolder messageHolder = (ChatHolder) holder;
        messageHolder.nameTxt.setText(chat.getChatName());
        messageHolder.messageTxt.setText(chat.getLastMessage());
        if (chat.isNotify()) {
            messageHolder.bell.setVisibility(View.VISIBLE);
        }
        else {
            messageHolder.bell.setVisibility(View.GONE);
        }
        String chatName = chat.getChatName();
        if (chatName != null && !chatName.isEmpty()) {
            char firstLetter = chatName.charAt(0);
            if (firstLetter >= 'a' && firstLetter <= 'z') {
                firstLetter -= 32;
            }
            Bitmap iconBitmap = Util.generateIconForLetter(firstLetter, holder.itemView.getContext());
            messageHolder.image.setImageBitmap(iconBitmap);
        }
        messageHolder.parent.setOnClickListener(v -> {
            messageChecked(position);
            chatDeletionLauncher.launch(new String[]{chat.getChatId(), chat.getChatName(), String.valueOf(chat.isGroup()), String.valueOf(chat.isAdmin())});
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public void addItem (Chat chat) {
        chats.add(chat);
        notifyItemInserted(chats.size() - 1);
    }

    public void addChat(Chat chat) {
        chats.add(0, chat);
        notifyItemInserted(0);
    }

    public void deleteChat(String chatId) {
        for (int i = 0; i < chats.size(); i++) {
            if (chats.get(i).getChatId().equals(chatId)) {
                chats.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void newMessage(String chatId){
        for (int i = 0; i < chats.size(); i++) {
            if (chats.get(i).getChatId().equals(chatId)) {
                Chat chat = chats.get(i);
                chats.remove(i);
                notifyItemRemoved(i);
                chat.setNotify(true);
                chats.add(0, chat);
                notifyItemInserted(0);
                break;
            }
        }
    }

    private void messageChecked(int pos){
        chats.get(pos).setNotify(false);
        notifyItemChanged(pos);
    }
}
