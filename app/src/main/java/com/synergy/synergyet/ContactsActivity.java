package com.synergy.synergyet;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.synergy.synergyet.custom.ChatUserAdapter;
import com.synergy.synergyet.model.ChatUser;
import com.synergy.synergyet.strings.FirebaseStrings;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatUserAdapter adapter;
    private List<ChatUser> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));
        users = new ArrayList<>();
        getContacts();
    }

    private void getContacts() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_1);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                String imageURL = FirebaseStrings.DEFAULT_IMAGE_VALUE;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatUser user = snapshot.getValue(ChatUser.class);
                    // Nos aseguramos que el usuario de la base datos y el de Firebase no son nulos
                    assert user != null;
                    assert firebaseUser != null;
                    //TODO: Reemplazar por comprobación final (de momento añade todos los usuarios disponibles excepto el que está utilizando la aplicación)
                    if (!user.getUid().equals(firebaseUser.getUid())) {
                        users.add(user);
                    } else {
                        // Obtenemos la URL de la foto del usuario actual
                        imageURL = user.getImageURL();
                    }
                }
                adapter = new ChatUserAdapter(ContactsActivity.this, users, imageURL);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //TODO: Mostrar AlertDialog de error
                System.out.println(databaseError.getMessage());
            }
        });

    }
}
