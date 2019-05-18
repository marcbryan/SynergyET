package com.synergy.synergyet;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.synergy.synergyet.model.ChatUser;
import com.synergy.synergyet.model.User;
import com.synergy.synergyet.strings.FirebaseStrings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseReference reference;

    private EditText et_name;
    private EditText et_surname;
    private EditText et_email;
    private EditText et_password1;
    private EditText et_password2;

    private String dialogOK;
    private String notCompleted;
    private String diff_pass;
    private String create_acc_failed;
    private String create_acc_ok;

    private final String TAG = "DOC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Obtener las instancias de FirebaseAuth y FirebaseFirestore (MUY IMPORTANTE!!)
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dialogOK = getString(R.string.dialogOK_button);
        notCompleted = getString(R.string.dialog2_txt1);
        diff_pass = getString(R.string.dialog2_txt2);
        create_acc_failed = getString(R.string.dialog2_txt3);
        create_acc_ok = getString(R.string.toast2);

        et_name = findViewById(R.id.et_name);
        et_surname = findViewById(R.id.et_surname);
        et_email = findViewById(R.id.et_email);
        et_password1 = findViewById(R.id.et_password1);
        et_password2 = findViewById(R.id.et_password2);
        Button btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                String surname = et_surname.getText().toString();
                String email = et_email.getText().toString();
                String pass_1 = et_password1.getText().toString();
                String pass_2 = et_password2.getText().toString();
                if (name.equals("") || surname.equals("") || email.equals("") || pass_1.equals("") || pass_2.equals("")) {
                    showDialog(notCompleted);
                } else {
                    if (!pass_1.equals(pass_2)) {
                        showDialog(diff_pass);
                    } else {
                        // Crear cuenta
                        String hashed_pwd = encryptSHA256(pass_1);
                        User user = new User(name, surname, email, hashed_pwd, FirebaseStrings.DEFAULT_USER_TYPE);
                        //TODO: Mostrar ProgressDialog
                        checkEmailAlreadyExists(email, user);
                    }
                }
            }
        });

    }

    private void showDialog(String dialog_txt) {
        // Creo un diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        builder.setMessage(dialog_txt)
                .setCancelable(false)
                .setPositiveButton(dialogOK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        // Lo muestro
        alert.show();
    }

    private String encryptSHA256 (String text) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        md.update(text.getBytes());
        byte[] digest = md.digest();
        return Base64.encodeToString(digest, Base64.DEFAULT);
    }

    //TODO: Comentar método
    private void checkEmailAlreadyExists(final String email, final User user){
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.getResult().getSignInMethods().size() == 0){
                    // No hay un usuario registrado con ese email
                    signUp(email, user.getPassword(), user);
                }else {
                    //TODO: Finaliza ProgressDialog
                    // Ya hay un usuario registrado con ese email
                    showDialog(getString(R.string.dialog_email_already_exists));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //TODO: Mostrar AlertDialog de error
                e.printStackTrace();
            }
        });
    }

    private void signUp(String email, String password, final User user) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Una vez se cree la cuenta, se podrá poner el UID
                            user.setUID(mAuth.getCurrentUser().getUid());
                            System.out.println(user.toString());
                            // Ahora añadimos los datos de la cuenta a Cloud Firestore
                            addAccount(user);
                            // Si la cuenta se ha creado correctamente, volverá a la pantalla de login para poder acceder a la cuenta
                            //Log.d(TAG, "createUserWithEmail:success");
                            //user = mAuth.getCurrentUser();

                        } else {
                            // Si falla el registro, se mostrará un mensaje
                            showDialog(create_acc_failed);
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            //Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                            //        Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        mAuth.getPendingAuthResult();
    }

    private void addAccount(final User user) {
        db.collection(FirebaseStrings.COLLECTION_1)
                // El ID del documento será el UID de Firebase del usuario que se registra (para luego buscar el documento por el ID)
                .document(user.getUID())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_1).child(user.getUID());
                        // Datos a insertar en Realtime Database
                        ChatUser chatUser = new ChatUser(user.getUID(), user.getName()+" "+user.getSurname(), FirebaseStrings.DEFAULT_IMAGE_VALUE);
                        // Crea el usuario en Realtime Database (para usar el chat)
                        reference.setValue(chatUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Mostrar Toast de cuenta creada
                                    Toast.makeText(SignUpActivity.this, create_acc_ok, Toast.LENGTH_SHORT).show();
                                    //Cerrar el activity
                                    finish();
                                } else {
                                    //TODO: Mostrar AlertDialog de error
                                    System.err.println("Error: "+task.getException());
                                }
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //TODO: Mostrar AlertDialog de error
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }
}
