package com.synergy.synergyet;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
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

    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Obtener las instancias de FirebaseAuth y FirebaseFirestore (MUY IMPORTANTE!!)
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
                    showDialog(getString(R.string.dialog2_txt1));
                } else {
                    if (!pass_1.equals(pass_2)) {
                        showDialog(getString(R.string.dialog2_txt2));
                    } else {
                        // Crear cuenta
                        String hashed_pwd = encryptSHA256(pass_1);
                        User user = new User(name, surname, email, FirebaseStrings.DEFAULT_USER_TYPE);
                        showProgressDialog(getString(R.string.creating_account));
                        checkEmailAlreadyExists(email, hashed_pwd, user);
                    }
                }
            }
        });

    }

    /**
     * Muesta un diálogo con un botón de OK y el texto que le pasamos como parámetro
     * @param dialog_txt - El texto a mostrar en el diálogo
     */
    private void showDialog(String dialog_txt) {
        // Creo un diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this, R.style.CustomAlertDialog);
        builder.setMessage(dialog_txt)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialogOK_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
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
    private String encryptSHA256(String text) {
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
     * Muestra un AlertDialog que emula a un ProgressDialog (la clase ProgressDialog está deprecated, por eso usamos este)
     * @param msg - El mensaje que se mostrará en el Dialog
     */
    private void showProgressDialog(String msg){
        // Creamos el AlertDialog y le aplicamos un style personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this, R.style.CustomAlertDialog);
        // Inflate de la vista
        View view = LayoutInflater.from(SignUpActivity.this).inflate(R.layout.custom_progress_dialog, null);
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

    /**
     * Comprueba si el email ya está siendo utilizado por algun usuario
     * @param email - El email que se quiere comprobar
     * @param password - La contraseña que quiere el usuario (para luego llamar al método que cree la cuenta)
     * @param user - Los datos del usuario, los que ha escrito en el formulario (para luego llamar al método que cree la cuenta)
     */
    private void checkEmailAlreadyExists(final String email, final String password, final User user){
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.getResult().getSignInMethods().size() == 0){
                    // No hay un usuario registrado con ese email
                    signUp(email, password, user);
                }else {
                    // Cerramos ProgressDialog
                    progressDialog.dismiss();
                    // Ya hay un usuario registrado con ese email
                    showDialog(getString(R.string.dialog_email_already_exists));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Mostramos Toast de error
                Toast.makeText(SignUpActivity.this, getString(R.string.error_creating_account), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    /**
     * Crea una cuenta con contraseña en Firebase (los datos se almacenan en Authentication)
     * @param email - El email que utilizará la cuenta
     * @param password - La contraseña que utilizará la cuenta
     * @param user - Los datos que ha escrito el usuario en el formulario
     */
    private void signUp(String email, String password, final User user) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Una vez se cree la cuenta, se podrá poner el UID
                            user.setUID(mAuth.getCurrentUser().getUid());
                            // Ahora añadimos los datos de la cuenta a Cloud Firestore
                            addAccount(user);
                        } else {
                            // Cerramos ProgressDialog
                            progressDialog.dismiss();
                            // Si falla el registro, se mostrará un mensaje
                            showDialog(getString(R.string.error_creating_account));
                        }
                    }
                });
        mAuth.getPendingAuthResult();
    }

    /**
     * Método que añade los datos necesarios del usuario a Realtime Database
     * para que pueda utilizar el chat
     * @param user - Los datos del usuario que se registra
     */
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
                                // Cerramos ProgressDialog
                                progressDialog.dismiss();
                                // Si la cuenta se ha creado correctamente, volverá a la pantalla de login para poder acceder a la cuenta
                                if (task.isSuccessful()) {
                                    // IMPORTANTE! Si el usuario cerrará la aplicación y la volviera abrir se se loguearia con el usuario que acaba de crear
                                    FirebaseAuth.getInstance().signOut();
                                    // Mostrar Toast de cuenta creada
                                    Toast.makeText(SignUpActivity.this, getString(R.string.toast2), Toast.LENGTH_SHORT).show();
                                    //Cerrar el activity
                                    finish();
                                } else {
                                    // Mostramos Toast de error
                                    Toast.makeText(SignUpActivity.this, getString(R.string.error_creating_account), Toast.LENGTH_SHORT).show();
                                    System.err.println("Error: "+task.getException());
                                }
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Cerramos ProgressDialog
                        progressDialog.dismiss();
                        // Mostramos Toast de error
                        Toast.makeText(SignUpActivity.this, getString(R.string.error_creating_account), Toast.LENGTH_SHORT).show();
                        Log.w("DOC", "Error writing document", e);
                    }
                });

    }
}
