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
import com.synergy.synergyet.model.Chat;
import com.synergy.synergyet.model.ChatUser;
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
    private List<Chat> chatList;

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

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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
                getMessages(firebaseUser.getUid(), user.getUid(), user.getImageURL());
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
                    sendMessage(firebaseUser.getUid(), receiver_uid, msg, date);
                } else {
                    // Mostramos un toast diciendo que no se puede enviar mensajes vacíos
                    Toast.makeText(MessageActivity.this, getString(R.string.toast_no_msg), Toast.LENGTH_SHORT).show();
                }
                // Borramos el texto del EditText
                text_send.setText("");
            }
        });

    }

    //TODO: Comentar método
    private void sendMessage(String sender, String receiver, String msg, String date) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        // Creamos un HashMap que contendrá el nombre del usuario que envía el mensaje, el usuario que lo recibe y el mensaje
        HashMap <String, Object> hashMap = new HashMap<>();
        hashMap.put(FirebaseStrings.KEY1_R2, sender);
        hashMap.put(FirebaseStrings.KEY2_R2, receiver);
        hashMap.put(FirebaseStrings.KEY3_R2, msg);
        hashMap.put(FirebaseStrings.KEY4_R2, date);
        // Lo guardamos en la base de datos
        reference.child(FirebaseStrings.REFERENCE_2).push().setValue(hashMap);
    }

    //TODO: Comentar método
    private void getMessages(final String sender, final String receiver, final String imageURL) {
        chatList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_2);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(sender) && chat.getSender().equals(receiver)
                            || chat.getReceiver().equals(receiver) && chat.getSender().equals(sender)) {
                        chatList.add(chat);
                    }
                    adapter = new MessageAdapter(MessageActivity.this, chatList, imageURL);
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
