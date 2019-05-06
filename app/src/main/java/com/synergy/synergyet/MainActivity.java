package com.synergy.synergyet;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.synergy.synergyet.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private EditText et_user;
    private EditText et_pass;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private String dialog_txt1;
    private String dialog_txt2;
    private String dialogOK;
    private String toast_txt1;

    private final String COLLECTION_1 = "users";
    private final String FIELD1_C1 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtener los textos de strings.xml
        dialog_txt1 = getString(R.string.dialog1_txt1);
        dialog_txt2 = getString(R.string.dialog1_txt2);
        dialogOK = getString(R.string.dialogOK_button);
        toast_txt1 = getString(R.string.toast1);

        // Obtener los componentes del activity
        et_user = findViewById(R.id.et_user);
        et_pass = findViewById(R.id.et_pass);

        mAuth = FirebaseAuth.getInstance();
        // Para obtener el usuario actual (el último que usó el usuario)
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Si el usuario ya ha iniciado sesión antes, se le mostrará la pantalla principal de la aplicación
            //TODO: Mostrar la pantalla principal
        }
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_user.getText().toString();
                String password = et_pass.getText().toString();
                if (email.equals("") || password.equals("")) {
                    showDialog(dialog_txt1);
                } else {
                    // Si el usuario introduce email y password, comprobaremos si las credenciales son correctas
                    signIn(email, encryptSHA256(password));
                }
            }
        });
        TextView signUp = findViewById(R.id.tvCreateAcc);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Abrir el activity para crear una cuenta
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showDialog(String dialog_txt) {
        // Creo un diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(dialog_txt)
                .setCancelable(false)
                .setPositiveButton(dialogOK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Borra el texto del EditText del usuario y de la contraseña
                        et_user.setText("");
                        et_pass.setText("");
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

    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Si el login se realiza con éxito, accederá a la aplicación
                            user = mAuth.getCurrentUser();
                            //TODO: Mostrar el nombre del usuario al hacer login correctamente. ej: 'Bienvendido Marc!! :)'
                            Toast.makeText(MainActivity.this, toast_txt1+" (nombre)!! :)",
                                    Toast.LENGTH_SHORT).show();
                            //TODO: Abrir el activity prinicipal de la aplicación

                        } else {
                            // Si el login falla, se mostrará al usuario un diálogo con un mensaje de error
                            showDialog(dialog_txt2);
                        }
                    }
                });
    }

    //TODO: Incompleto (no funciona)
    private void getUserData(String UID){
        //DocumentReference docRef = db.collection(COLLECTION_1).document();
        //CollectionReference colRef =

                                        //.whereEqualTo(UID, user.getUid())
                                        //.get();
        db.collection(COLLECTION_1)
                .whereEqualTo(UID, user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            //for
                        } else {

                        }
                    }
                });
    }

}
