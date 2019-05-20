package com.synergy.synergyet;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.synergy.synergyet.custom.MessageAdapter;
import com.synergy.synergyet.model.ChatUser;
import com.synergy.synergyet.model.Message;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    private CircleImageView profile_picture;
    private TextView display_name;

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    private ImageButton btn_send;
    private EditText text_send;
    private RecyclerView recyclerView;

    private MessageAdapter adapter;
    private List<Message> messages;
    private String conversation_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        // Flecha para volver atrás
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(MessageActivity.this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        profile_picture = findViewById(R.id.profile_image);
        display_name = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_toSend);

        // UID del usuario al que enviaremos un mensaje
        final String receiver_uid = getIntent().getStringExtra(IntentExtras.EXTRA_UID);
        // ID de la conversación actual
        conversation_id = getIntent().getStringExtra(IntentExtras.EXTRA_CONVERSATION_ID);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //TODO: Revisar esta parte
        reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_1).child(receiver_uid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatUser user = dataSnapshot.getValue(ChatUser.class);
                display_name.setText(user.getDisplayName());
                if (user.getImageURL().equals(FirebaseStrings.DEFAULT_IMAGE_VALUE)) {
                    // Si el usuario tiene como ImageURL el valor 'default', le pondremos la imagen de usuario por defecto
                    profile_picture.setImageResource(R.drawable.google_user_icon);
                } else {
                    // Pone la imagen del usuario en el CircleImageView
                    Glide.with(MessageActivity.this).load(user.getImageURL()).into(profile_picture);
                }
                getMessages(user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //TODO: Mostrar AlertDialog de error
                System.err.println(databaseError.getMessage());
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    // Ejemplo fecha -> 18/05/2019 13:30:45
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String date = sdf.format(Calendar.getInstance().getTime());
                    // Dividimos la fecha y la hora del mensaje enviado
                    String spDate[] = date.split(" ");
                    // La posición 0 de spDate[] es la fecha y la posición 1 la hora
                    Message message = new Message(msg, spDate[0], spDate[1], firebaseUser.getUid());
                    // Enviamos el mensaje
                    sendMessage(message);
                } else {
                    // Mostramos un toast diciendo que no se puede enviar mensajes vacíos
                    Toast.makeText(MessageActivity.this, getString(R.string.toast_no_msg), Toast.LENGTH_SHORT).show();
                }
                // Borramos el texto del EditText
                text_send.setText("");
            }
        });

    }

    /**
     * Envía un mensaje y actualiza la información del último mensaje
     * @param msg - El Objeto Message con la información del mensaje a enviar
     */
    private void sendMessage (Message msg) {
        reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_2).child(conversation_id);
        // Guardamos el mensaje en Realtime Database
        reference.child(FirebaseStrings.KEY3_R2).push().setValue(msg);

        // Actualizamos la información del último mensaje
        HashMap<String, Object> lastMsgInfo = new HashMap<>();
        lastMsgInfo.put(FirebaseStrings.K1R2_CHILD1, msg.getMessage());
        lastMsgInfo.put(FirebaseStrings.K1R2_CHILD2, msg.getDate()+" "+msg.getHour());
        lastMsgInfo.put(FirebaseStrings.K1R2_CHILD3, msg.getSender());
        reference.child(FirebaseStrings.KEY1_R2).updateChildren(lastMsgInfo);

        //TODO: Actualizar el número de mensajes no leidos
    }

    /**
     * Obtiene todos los mensajes de la conversación
     * @param imageURL - La URL de la foto del usuario actual
     */
    private void getMessages(final String imageURL) {
        messages = new ArrayList<>();
        // Vamos al nodo messages
        reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_2).child(conversation_id).child(FirebaseStrings.KEY3_R2);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message msg = snapshot.getValue(Message.class);
                    messages.add(msg);
                    // Creamos la vista de cada mensaje
                    adapter = new MessageAdapter(MessageActivity.this, messages, imageURL);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //TODO: Mostrar AlertDialog de error
                System.err.println(databaseError.getMessage());
            }
        });
    }

}
