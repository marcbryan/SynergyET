package com.synergy.synergyet.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.synergy.synergyet.MessageActivity;
import com.synergy.synergyet.R;
import com.synergy.synergyet.model.Chat;
import com.synergy.synergyet.model.ChatUser;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {
    private Context context;
    private List<ChatUser> users;

    private String last_msg_string;
    private Chat last_msg_sender;
    private Chat last_msg_receiver;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private String you;

    public ChatUserAdapter(Context context, List<ChatUser> users) {
        this.context = context;
        this.users = users;
        // Texto 'Tú:'
        you = context.getString(R.string.you);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ChatUserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtenemos el contacto
        final ChatUser user = users.get(position);
        // Mostramos el nombre del contacto
        holder.username.setText(user.getDisplayName());
        // Obtenemos nuestro usuario
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            // Mostraremos el último mensaje y la fecha (si hay último mensaje)
            getLastMessageAndTime(firebaseUser.getUid(), user.getUid(), holder.last_message, holder.last_msg_date);
        }
        if (user.getImageURL().equals(FirebaseStrings.DEFAULT_IMAGE_VALUE)) {
            holder.profile_image.setImageResource(R.drawable.google_user_icon);
        } else {
            Glide.with(context).load(user.getImageURL()).into(holder.profile_image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cuando haga click en un contacto, se abrirá el Activity para chatear
                Intent intent = new Intent(context, MessageActivity.class);
                // Le pasamos el UID del usuario con el que chateará al siguiente Activity
                intent.putExtra(IntentExtras.EXTRA_UID, user.getUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public ImageView profile_image;
        public TextView last_message;
        public TextView last_msg_date;

        public ViewHolder(View itemView) {
            super(itemView);
            // Buscamos los componentes de user_item.xml que utilizaremos
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            last_message = itemView.findViewById(R.id.lastMessage);
            last_msg_date = itemView.findViewById(R.id.lastMsg_date);
        }
    }

    /**
     * Pone en el TextView correspondiente el último mensaje enviado en un chat, además también pondrá la fecha o la hora de cuando fue enviado
     * @param sender - El UID del usuario actual
     * @param receiver - El UID del usuario con que el chateará el usuario actual
     * @param last_msg - El TextView donde se verá el último mensaje
     * @param last_msg_date - El TextView donde se verá la fecha o la hora del último mensaje
     */
    private void getLastMessageAndTime(final String sender, final String receiver, final TextView last_msg, final TextView last_msg_date) {
        last_msg_string = "none";
        last_msg_sender = null;
        last_msg_receiver = null;
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_2);
        // Query 1 -> Obtiene todos los mensajes que haya enviado el usuario A
        reference.orderByChild(FirebaseStrings.KEY1_R2)
                .equalTo(sender)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Chat chat = snapshot.getValue(Chat.class);
                            // Ahora filtramos por los mensajes que haya recibido el usuario B
                            if (chat.getReceiver().equals(receiver)) {
                                last_msg_sender = chat;
                            }
                        }
                        // Query 2 -> Obtiene todos los mensajes que haya recibido el usuario
                        reference.orderByChild(FirebaseStrings.KEY2_R2)
                                .equalTo(sender)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                            Chat chat = snapshot.getValue(Chat.class);
                                            // Ahora filtramos por los mensajes que haya enviado el usuario B
                                            if (chat.getSender().equals(receiver)) {
                                                last_msg_receiver = chat;
                                            }
                                        }
                                        SpannableString str = null;
                                        String last_date_string = "";
                                        if (last_msg_sender != null && last_msg_receiver == null){
                                            // Si el usuario A ha enviado mensajes pero el usuario B no, el último mensaje será el del usuario A
                                            last_msg_string = you+" "+last_msg_sender.getMessage();
                                            last_date_string = last_msg_sender.getDate();
                                            // Sirve para mostrar 'Tú:' en negrita el resto del texto normal
                                            str = new SpannableString(last_msg_string);
                                            str.setSpan(new StyleSpan(Typeface.BOLD), 0, you.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        } else if (last_msg_sender == null && last_msg_receiver != null) {
                                            // Si el usuario B ha enviado mensajes pero el usuario A no, el último mensaje será el del usuario B
                                            last_msg_string = last_msg_receiver.getMessage();
                                            last_date_string = last_msg_receiver.getDate();
                                        } else if (last_msg_sender != null && last_msg_receiver != null) {
                                            // Si usuario A y usuario B han enviado mensajes, compararemos las fechas para ver cual es el más reciente
                                            try {
                                                // Parseamos los strings de las fechas
                                                Date date1 = sdf.parse(last_msg_sender.getDate());
                                                Date date2 = sdf.parse(last_msg_receiver.getDate());
                                                if (date1.compareTo(date2) > 0) {
                                                    // La fecha 1 es más reciente que la fecha 2
                                                    last_msg_string = you+" "+last_msg_sender.getMessage();
                                                    last_date_string = last_msg_sender.getDate();
                                                    // Sirve para mostrar 'Tú:' en negrita el resto del texto normal
                                                    str = new SpannableString(last_msg_string);
                                                    str.setSpan(new StyleSpan(Typeface.BOLD), 0, you.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                } else {
                                                    // La fecha 2 es más reciente que la fecha 1
                                                    last_msg_string = last_msg_receiver.getMessage();
                                                    last_date_string = last_msg_receiver.getDate();
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        switch (last_msg_string) {
                                            case "none":
                                                // Si ningún usuario ha enviado mensajes, no habrá texto en los TextView
                                                last_msg.setText("");
                                                last_msg_date.setText("");
                                                break;
                                            default:
                                                if (str != null) {
                                                    // El usuario A ha mandado el último mensaje
                                                    last_msg.setText(str);
                                                } else {
                                                    // El usuario B ha mandado el último mensaje
                                                    last_msg.setText(last_msg_string);
                                                }
                                                // Ponemos la hora de último mensaje (si el último mensaje es de hoy se verá la hora, si es de otro dia se verá la fecha)
                                                last_msg_date.setText(textForDateTextView(last_date_string));
                                                break;
                                        }

                                        last_msg_string = "none";
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        //TODO: Mostrar AlertDialog de error
                                        System.err.println(databaseError.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //TODO: Mostrar AlertDialog de error
                        System.err.println(databaseError.getMessage());
                    }
                });
    }

    /**
     * Este método nos devolverá la hora de una fecha si esa fecha es hoy,
     * si la fecha es de otro dia devolverá la fecha
     * @param date - La fecha de la que queremos el resultado
     * @return Devuelve un string con la hora o la fecha
     */
    private String textForDateTextView(String date) {
        // La fecha de hoy
        String today = sdf.format(Calendar.getInstance().getTime());
        String spToday[] = today.split(" ");
        String spDate[] = date.split(" ");
        // Si la fecha que pasamos como parámetro es hoy
        if (spDate[0].equals(spToday[0])){
            // Le devolveremos la hora
            String spHour[] = spDate[1].split(":");
            return spHour[0]+":"+spHour[1];
        }
        // Si no le devolveremos la fecha
        return spDate[0];
    }
}
