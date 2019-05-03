package com.synergy.synergyet;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private EditText et_name;
    private EditText et_surname;
    private EditText et_email;
    private EditText et_password1;
    private EditText et_password2;

    private String dialogOK;
    private String notCompleted;
    private String diff_pass;
    private String create_acc_failed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dialogOK = getString(R.string.dialogOK_button);
        notCompleted = getString(R.string.dialog2_txt1);
        diff_pass = getString(R.string.dialog2_txt2);
        create_acc_failed = getString(R.string.dialog2_txt3);

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
                        String hash = encryptSHA256(pass_1);
                        System.out.println(hash);
                        if (hash.equals(encryptSHA256(pass_1))){
                            System.out.println("correcto");
                        }
                        finish();
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

    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
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

    public String encryptSHA256 (String text) {
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

}
