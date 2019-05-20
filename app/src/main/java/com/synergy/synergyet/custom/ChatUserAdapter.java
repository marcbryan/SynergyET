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
import com.synergy.synergyet.model.ChatUser;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {
    private Context context;
    private List<ChatUser> users;
    private String imageURL;

    private String conversation_id;
    private String last_msg_string;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private String you;

    public ChatUserAdapter(Context context, List<ChatUser> users, String imageURL) {
        this.context = context;
        this.users = users;
        this.imageURL = imageURL;
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
            //TODO: Comprobar si el ID de la conversación ya está en la base de datos
            // De momento lo obtendremos así
            conversation_id = generateConversationID(firebaseUser.getUid(), user.getUid());
            // Mostraremos el último mensaje y la fecha (si hay último mensaje)
            getLastMessageAndTime(firebaseUser.getUid(), user.getUid(), holder.last_message, holder.last_msg_date, imageURL);
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
                // También le pasamos el ID de la conversación
                intent.putExtra(IntentExtras.EXTRA_CONVERSATION_ID, conversation_id);
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
     * @param senderImageURL - La URL de la foto del usuario actual
     */
    private void getLastMessageAndTime(final String sender, final String receiver, final TextView last_msg, final TextView last_msg_date, final String senderImageURL) {
        last_msg_string = "none";
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_2).child(conversation_id).child(FirebaseStrings.KEY1_R2);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                last_msg_string = ds.child(FirebaseStrings.K1R2_CHILD1).getValue(String.class);
                String lmDate = ds.child(FirebaseStrings.K1R2_CHILD2).getValue(String.class);
                String lmSender = ds.child(FirebaseStrings.K1R2_CHILD3).getValue(String.class);
                if (last_msg_string == null && lmDate == null && lmSender == null) {
                    // Si los tres valores son nulos significa que nunca ha habido una conversación entre los usuarios, por lo tanto, la creamos
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_2).child(conversation_id);
                    // HashMap de estructura de los datos de la conversación
                    HashMap <String, Object> hashMap = new HashMap<>();
                    // HashMap de la información del último mensaje
                    HashMap <String, String> lastMsgInfo = new HashMap<>();
                    lastMsgInfo.put(FirebaseStrings.K1R2_CHILD1, "none");
                    // Los nodos de fecha mensaje y el del que envía el mensaje se crearán cuando se envie o se reciba el primer mensaje

                    // Añadimos al HashMap de la conversación el HashMap de la información del último mensaje
                    hashMap.put(FirebaseStrings.KEY1_R2, lastMsgInfo);
                    // HashMap de los miembros de la conversación
                    HashMap <String, Boolean> members = new HashMap<>();
                    // Añadimos los usuarios
                    members.put(sender, true);
                    members.put(receiver, true);
                    // Añadimos al HashMap de la conversación el HashMap de los miembros
                    hashMap.put(FirebaseStrings.KEY2_R2, members);
                    // El nodo de los mensajes se creará cuando se envie o se reciba el primer mensaje

                    // Añadimos los datos a Realtime Database
                    ref.setValue(hashMap);

                    // Creamos otro nodo en usuarios
                    DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_1).child(sender).child(FirebaseStrings.KEY4_R1);
                    HashMap <String, Object> userConvInfo = new HashMap<>();
                    userConvInfo.put(FirebaseStrings.K4_R1_CHILD1, 0);
                    userConvInfo.put(FirebaseStrings.K4_R1_CHILD2, senderImageURL);
                    // Añadimos los datos a Realtime Database
                    ref2.child(conversation_id).setValue(userConvInfo);
                } else {
                    switch (last_msg_string) {
                        case "none":
                            // Si ningún usuario ha enviado mensajes, no habrá texto en los TextView
                            last_msg.setText("");
                            last_msg_date.setText("");
                            break;
                        default:
                            SpannableString str = null;
                            if (lmSender.equals(sender)) {
                                // Sirve para mostrar 'Tú:' en negrita el resto del texto normal
                                str = new SpannableString(you + " " + last_msg_string);
                                str.setSpan(new StyleSpan(Typeface.BOLD), 0, you.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                // El usuario A ha mandado el último mensaje
                                last_msg.setText(str);
                            } else {
                                // El usuario B ha mandado el último mensaje
                                last_msg.setText(last_msg_string);
                            }
                            // Ponemos la hora de último mensaje (si el último mensaje es de hoy se verá la hora, si es de otro dia se verá la fecha)
                            last_msg_date.setText(textForDateTextView(lmDate));
                            break;
                    }
                    last_msg_string = "none";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // TODO: Mostrar AlertDialog o Toast de error
                System.out.println(databaseError.getMessage());
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

    /**
     * Genera el ID de la conversación, puede devolver UID1+UID2 o UID2+UID1 dependiendo
     * de cual va antes alfabeticamente
     * @param uid1 - El UID del usuario actual
     * @param uid2 - El ID del otro usuario
     * @return Devuelve ID de la conversación
     */
    private String generateConversationID(String uid1, String uid2) {
        // Orden alfabético
        if (uid1.compareTo(uid2) < 0) {
            // Ej de dos letras -> a+b
            return uid1+uid2;
        }
        // Si no se cumple la condición: Ej de dos letras -> b+a
        return uid2+uid1;
    }
}
