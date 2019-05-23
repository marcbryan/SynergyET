package com.synergy.synergyet.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.synergy.synergyet.R;
import com.synergy.synergyet.model.ChatUser;
import com.synergy.synergyet.strings.FirebaseStrings;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private CircleImageView imageProfile;
    private TextView displayName;

    private DatabaseReference reference;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    private Dialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        imageProfile = view.findViewById(R.id.profile_image);
        displayName = view.findViewById(R.id.displayName);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference(FirebaseStrings.CLOUD_REFERENCE_1).child(firebaseUser.getUid());

        reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_1).child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatUser user = dataSnapshot.getValue(ChatUser.class);
                // Ponemos el nombre del usuario
                displayName.setText(user.getDisplayName());
                if (user.getImageURL().equals(FirebaseStrings.DEFAULT_IMAGE_VALUE)) {
                    // Imagen por defecto
                    imageProfile.setImageResource(R.drawable.google_user_icon);
                } else {
                    if (isAdded()) {
                        // Ponemos la foto del usuario en el ImageView con Glide
                        Glide.with(getContext()).load(user.getImageURL()).into(imageProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", databaseError.getMessage());
            }
        });

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfileImage();
            }
        });

        return view;
    }

    //TODO: Comentar métodos

    private void changeProfileImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    @SuppressWarnings("unchecked")
    private void uploadImage() {
        showProgressDialog(getString(R.string.uploading_profile_image));
        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()+"");
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("Error: ", task.getException()+"");
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // Actualizamos el campo imageURL del usuario
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();
                        reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_1).child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put(FirebaseStrings.K4_R1_CHILD2, mUri);
                        reference.updateChildren(map);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.upload_profile_img_error), Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }
            });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), getString(R.string.uploading_profile_image), Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    /**
     * Muestra un AlertDialog que emula a un ProgressDialog (la clase ProgressDialog está deprecated, por eso usamos este)
     * @param msg - El mensaje que se mostrará en el Dialog
     */
    private void showProgressDialog(String msg){
        // Creamos el AlertDialog y le aplicamos un style personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
        // Inflate de la vista
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.custom_progress_dialog, null);
        // Obtenemos el TextView de la vista para poder poner el texto
        TextView tv_message = view.findViewById(R.id.loading_msg);
        tv_message.setText(msg);
        // Ponemos la vista y lo hacemos no cancelable (para hacerlo modal)
        builder.setView(view)
                .setCancelable(false);
        progressDialog = builder.create();
        // Mostramos el Dialog
        progressDialog.show();
    }

}