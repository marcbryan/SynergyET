package com.synergy.synergyet;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.synergy.synergyet.custom.DelayedProgressDialog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    private EditText et_user;
    private EditText et_pass;
    private ImageView eye;
    private Drawable visible;
    private Drawable not_visible;
    private AlertDialog dialog;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private AlertDialog.Builder builder;
    private DelayedProgressDialog progressDialog = new DelayedProgressDialog();
    private boolean showing_pass = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Para obtener el usuario actual (el último que usó el usuario)
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Si el usuario ya ha iniciado sesión antes, se le mostrará la pantalla principal de la aplicación
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
        }

        // Obtener los componentes del activity
        et_user = findViewById(R.id.et_username);
        et_pass = findViewById(R.id.et_password);

        // Obtener las imagenes de visible y no visible
        visible = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_eye);
        not_visible = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_visibility_off_white_24dp);

        mAuth = FirebaseAuth.getInstance();

        eye = findViewById(R.id.eye_icon);
        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_pass.getText().toString().equals("")) {
                    if (!showing_pass) {
                        // Muestra la contraseña
                        et_pass.setTransformationMethod(null);
                        eye.setImageDrawable(not_visible);
                        showing_pass = true;
                    } else {
                        // Oculta la contraseña
                        et_pass.setTransformationMethod(new PasswordTransformationMethod());
                        eye.setImageDrawable(visible);
                        showing_pass = false;
                    }
                }
            }
        });

        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_user.getText().toString();
                String password = et_pass.getText().toString();
                if (email.equals("") || password.equals("")) {
                    // Mostramos Dialog de error
                    showDialog(getString(R.string.dialog_enter_email_pass));
                } else {
                    // Si el usuario introduce email y password, comprobaremos si las credenciales son correctas
                    progressDialog.show(getSupportFragmentManager(), "tag");
                    signIn(email, hashSHA256(password));
                }
            }
        });

        TextView recoverPassword = findViewById(R.id.lbl_forgot_password);
        recoverPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creamos el InputDialog
                builder = new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                LayoutInflater inflater = getLayoutInflater();
                // Le ponemos la vista personalizada
                View dialogView = inflater.inflate(R.layout.input_dialog_recoverpassword, null);
                builder.setCancelable(false);
                builder.setView(dialogView);
                // Obtenemos el TextInputEditText del InputDialog (para poder obtener el texto que introduce el usuario)
                final TextInputLayout til = dialogView.findViewById(R.id.text_input_layout);
                final TextInputEditText et_dialogEmail = dialogView.findViewById(R.id.input_password);
                et_dialogEmail.requestFocus();
                et_dialogEmail.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (til.getError() != null) {
                            til.setError(null);
                        }
                    }
                });
                // Creamos el listener del botón Aceptar vacío (más adelante lo sobreescribiremos)
                builder.setPositiveButton(getString(R.string.dialogOK_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                // Listener del botón Cancelar
                builder.setNegativeButton(getString(R.string.dialogCancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Si pulsa Cancelar se cerrará el Dialog
                        dialog.cancel();
                    }
                });

                dialog = builder.create();
                dialog.show();
                // Sobreescribimos el listener del botón Aceptar (si lo hacemos de esta manera evitamos que se cierre el Dialog al pulsar 'Aceptar')
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        if (TextUtils.isEmpty(et_dialogEmail.getText())) {
                            til.setError(getString(R.string.dialog_empty_email));
                        } else {
                            if (Patterns.EMAIL_ADDRESS.matcher(et_dialogEmail.getText()).matches()) {
                                final String email = et_dialogEmail.getText().toString();
                                mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                        // Si la cuenta existe, se enviará el correo, si no existe, no se enviará nada
                                        if (task.getResult().getSignInMethods().size() > 0){
                                            mAuth.sendPasswordResetEmail(email);
                                        }
                                        // Cerramos el Dialog
                                        dialog.dismiss();
                                        // Mostraremos el Snackbar de mail enviado (aunque la cuenta no exista)
                                        Snackbar.make(getWindow().getDecorView().getRootView(), getString(R.string.snackbar_mail_sent), Snackbar.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            } else {
                                til.setError(getString(R.string.dialog_wrong_email));
                            }
                        }
                    }
                });
            }
        });

        TextView signUp = findViewById(R.id.lbl_create_user);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrimos el formulario para registrarse
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Mostrar home screen de Android
        moveTaskToBack(true);
    }

    /**
     * Muesta un diálogo con un botón de OK y el texto que le pasamos como parámetro
     * @param dialog_txt - El texto a mostrar en el diálogo
     */
    private void showDialog(String dialog_txt) {
        // Creo un diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
        builder.setMessage(dialog_txt)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialogOK_button), new DialogInterface.OnClickListener() {
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

    /**
     * Hashea un string en SHA-256 (hash)
     * @param text - El texto que queremos hashear
     * @return Devuelve el hash del string que le pasamos como parámetro
     */
    private String hashSHA256(String text) {
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

    /**
     * Hace login en Firebase y inicia el siguiente activity si el usuario ha podido loguearse correctamente
     * @param email - El email con el que queremos acceder
     * @param password - La contraseña con la que queremos acceder
     */
    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Finaliza el ProgressDialog
                        progressDialog.cancel();
                        if (task.isSuccessful()) {
                            // Si el login se realiza con éxito, accederá a la aplicación
                            user = mAuth.getCurrentUser();
                            // Creamos el intent de la pantalla de bienvenida
                            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                            // Iniciamos el activity
                            startActivity(intent);
                        } else {
                            // Si el login falla, se mostrará al usuario un diálogo con un mensaje de error
                            showDialog(getString(R.string.dialog_wrong_combination));
                        }
                    }
                });
    }

}
