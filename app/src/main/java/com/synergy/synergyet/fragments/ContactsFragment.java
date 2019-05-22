package com.synergy.synergyet.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.synergy.synergyet.R;
import com.synergy.synergyet.custom.ChatUserAdapter;
import com.synergy.synergyet.model.ChatUser;
import com.synergy.synergyet.notifications.Token;
import com.synergy.synergyet.strings.FirebaseStrings;

import java.util.ArrayList;
import java.util.List;


public class ContactsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatUserAdapter adapter;
    private List<ChatUser> users;
    private FirebaseUser firebaseUser;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                if (firebaseUser != null) {
                    String refreshToken = instanceIdResult.getToken();
                    updateToken(refreshToken);
                }
            }
        });

        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        users = new ArrayList<>();
        getContacts(view.getContext());
        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_3);
        Token refreshToken = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(refreshToken);
    }

    private void getContacts(final Context context) {
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
                adapter = new ChatUserAdapter(context, users, imageURL);
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
