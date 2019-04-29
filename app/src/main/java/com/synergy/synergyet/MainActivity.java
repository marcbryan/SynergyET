package com.synergy.synergyet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText user;
    private EditText pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Para obtener el usuario actual (el último que usó el usuario)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Si el usuario ya ha iniciado sesión antes
        if (user != null) {
            //TODO: Mostrar el menu prinicipal (el calendario)
        } else {
            // Si el usuario no ha iniciado sesión anteriormente se le mostrará la pantalla de login (la actual)

        }
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
