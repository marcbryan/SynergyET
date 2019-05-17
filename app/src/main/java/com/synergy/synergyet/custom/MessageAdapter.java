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
import com.synergy.synergyet.model.Chat;
import com.synergy.synergyet.strings.FirebaseStrings;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Chat> chatList;
    private String imageURL;
    private FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Chat> chatList, String imageURL) {
        this.context = context;
        this.chatList = chatList;
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
        Chat chat = chatList.get(position);
        holder.show_message.setText(chat.getMessage());
        holder.show_msg_hour.setText(getHourFromDateString(chat.getDate()));
        if (imageURL.equals(FirebaseStrings.DEFAULT_IMAGE_VALUE)) {
            holder.profile_image.setImageResource(R.drawable.google_user_icon);
        } else {
            Glide.with(context).load(imageURL).into(holder.profile_image);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
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
        if (chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        return MSG_TYPE_LEFT;
    }

    //TODO: Comentar método
    // Formato de fecha -> ej. 17/05/2019 23:06:15
    private String getHourFromDateString(String date){
        // Primero hacemos un split del espacio para obtener la hora
        String sp[] = date.split(" ");
        // Después hacemos otro split de los dos puntos para obtener hora y minutos
        String hrSp[] = sp[1].split(":");
        // Devolvemos hora y minutos
        return hrSp[0]+":"+hrSp[1];
    }
}
