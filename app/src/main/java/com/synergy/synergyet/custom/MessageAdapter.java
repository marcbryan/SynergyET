package com.synergy.synergyet.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.synergy.synergyet.R;
import com.synergy.synergyet.model.Message;
import com.synergy.synergyet.strings.FirebaseStrings;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Message> messages;
    private String imageURL;
    private FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Message> messages, String imageURL) {
        this.context = context;
        this.messages = messages;
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        // Obtenemos el mensaje y su información
        Message msg = messages.get(position);
        // Mostramos el mensaje
        holder.show_message.setText(msg.getMessage());
        // Quitar los segundos de la hora
        String spHour[] = msg.getHour().split(":");
        String hour = spHour[0]+":"+spHour[1];
        // Mostramos la hora del mensaje
        holder.show_msg_hour.setText(hour);
        if (imageURL.equals(FirebaseStrings.DEFAULT_IMAGE_VALUE)) {
            holder.profile_image.setImageResource(R.drawable.google_user_icon);
        } else {
            Glide.with(context).load(imageURL).into(holder.profile_image);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        public TextView show_msg_hour;
        public ImageView profile_image;

        public ViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            show_msg_hour = itemView.findViewById(R.id.show_msg_hour);
            profile_image = itemView.findViewById(R.id.profile_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Si el usuario que envía el mensaje es el actual, añadiremos el mensaje a su columna de mensajes
        if (messages.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        return MSG_TYPE_LEFT;
    }

}
