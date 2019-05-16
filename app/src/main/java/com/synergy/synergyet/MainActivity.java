package com.synergy.synergyet;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.synergy.synergyet.custom.DelayedProgressDialog;
import com.synergy.synergyet.model.User;
import com.synergy.synergyet.strings.FirebaseStrings;

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
    private FirebaseFirestore db;
    private AlertDialog.Builder builder;
    private String dialog_txt1;
    private String dialog_txt2;
    private String dialogOK;
    private String toast_txt1;
    private boolean showing_pass = false;

    private DelayedProgressDialog progressDialog = new DelayedProgressDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Para obtener el usuario actual (el último que usó el usuario)
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Si el usuario ya ha iniciado sesión antes, se le mostrará la pantalla principal de la aplicación
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            //TODO: Pasarle sus datos al siguiente activity
            startActivity(intent);
        }

        // Obtener los textos de strings.xml
        dialog_txt1 = getString(R.string.dialog1_txt1);
        dialog_txt2 = getString(R.string.dialog1_txt2);
        dialogOK = getString(R.string.dialogOK_button);
        toast_txt1 = getString(R.string.toast1);

        // Obtener los componentes del activity
        et_user = findViewById(R.id.et_username);
        et_pass = findViewById(R.id.et_password);

        // Obtener las imagenes de visible y no visible
        visible = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_eye);
        not_visible = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_visibility_off_white_24dp);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        eye = findViewById(R.id.eye_icon);
        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_pass.getText().toString().equals("")) {
                    if (!showing_pass) {
                        //System.out.println("showing");
                        //et_pass.setSelection(et_pass.getText().length());
                        // Muestra la contraseña
                        et_pass.setTransformationMethod(null);
                        eye.setImageDrawable(not_visible);
                        showing_pass = true;
                    } else {
                        //System.out.println("hiding");
                        //et_pass.setSelection(et_pass.getText().length());
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
                    showDialog(dialog_txt1);
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
                    public void onClick(View v) {
                        // Obtenemos el email que ha introducido el usuario
                        String pwd = et_dialogEmail.getText().toString();
                        if (TextUtils.isEmpty(et_dialogEmail.getText())) {
                            System.out.println("IS EMPTY");
                            til.setError(getString(R.string.dialog_empty_email));
                        } else {
                            if (Patterns.EMAIL_ADDRESS.matcher(et_dialogEmail.getText()).matches()) {
                                //ENVIAR EL EMAIL
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
                //TODO: Abrir el activity para crear una cuenta
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        }


    /**
     * Muesta un diálogo con un botón de ok y el texto que le pasamos como parámetro
     * @param dialog_txt - El texto a mostrar en el diálogo
     */
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
     * Hace login en Firebase
     * @param email - El email con el que queremos acceder
     * @param password - La contraseña con la que queremos acceder
     */
    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Si el login se realiza con éxito, accederá a la aplicación
                            user = mAuth.getCurrentUser();
                            getUserData(user.getUid());
                        } else {
                            // Si el login falla, se mostrará al usuario un diálogo con un mensaje de error
                            //Log.w("FirebaseError", "signInWithEmail:failure", task.getException());
                            showDialog(dialog_txt2);
                        }
                    }
                });
    }

    /**
     * Busca los datos de un usuario en Cloud Firestore sabiendo su UID
     * @param UID - El UID del usuario del que queremos los datos
     */
    private void getUserData(String UID){
        final DocumentReference docRef = db.collection(FirebaseStrings.COLLECTION_1).document(UID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // Obtener los datos del usuario
                User user = documentSnapshot.toObject(User.class);
                // Mostrar Toast de bienvienida
                Toast.makeText(MainActivity.this, toast_txt1 + " " + user.getName() + "!! :)",
                Toast.LENGTH_SHORT).show();
                // Creamos el intent de la pantalla de bienvenida
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                //TODO: Pasarle sus datos (Clase User) al siguiente activity, para no volver a consultarlos
                //intent.putExtra(IntentExtras.EXTRA_USER_DATA, (User) user);
                // Iniciamos el activity
                progressDialog.cancel();
                startActivity(intent);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Error -> " + e);
            }
        });
    }

}
