package com.example.myapp;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        boolean isUser = message.sender.equals("user");

        holder.tvMessage.setText(message.content);
        holder.tvTime.setText(message.timestamp);

        // Set layout gravity based on sender
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.cardView.getLayoutParams();
        params.gravity = isUser ? Gravity.END : Gravity.START;
        holder.cardView.setLayoutParams(params);

        // Set colors based on sender
        int bgColor = isUser ? ContextCompat.getColor(holder.itemView.getContext(), R.color.message_user)
                : ContextCompat.getColor(holder.itemView.getContext(), R.color.message_bot);
        int textColor = isUser ? ContextCompat.getColor(holder.itemView.getContext(), R.color.message_user_text)
                : ContextCompat.getColor(holder.itemView.getContext(), R.color.message_bot_text);

        holder.cardView.setCardBackgroundColor(bgColor);
        holder.tvMessage.setTextColor(textColor);

        // Set time text gravity
        LinearLayout.LayoutParams timeParams = (LinearLayout.LayoutParams) holder.tvTime.getLayoutParams();
        timeParams.gravity = isUser ? Gravity.END : Gravity.START;
        holder.tvTime.setLayoutParams(timeParams);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvMessage, tvTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_message);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}