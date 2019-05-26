package com.synergy.synergyet;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.synergy.synergyet.fragments.ContactsFragment;
import com.synergy.synergyet.fragments.MyCoursesFragment;
import com.synergy.synergyet.fragments.ProfileFragment;
import com.synergy.synergyet.model.ChatUser;
import com.synergy.synergyet.model.User;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import de.hdodenhof.circleimageview.CircleImageView;

public class WelcomeActivity extends AppCompatActivity {
    private FirebaseUser user;
    private FirebaseFirestore db;
    private DatabaseReference reference;
    private Toolbar toolbar;
    private TextView displayName;
    private TextView email;
    private CircleImageView profile_image;
    private DrawerLayout drawer;
    private User user_data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Obtenemos el toolbar y lo añadimos al activity (para que se vean los iconos)
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View nav_header = navigationView.getHeaderView(0);
        displayName = nav_header.findViewById(R.id.user_displayName);
        email = nav_header.findViewById(R.id.user_email);
        profile_image = nav_header.findViewById(R.id.user_profile_image);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_courses:
                        // Primero cambiamos el título
                        toolbar.setTitle(getString(R.string.titleWelcome));
                        // Reemplazamos el fragment por el de bienvenida (el de los cursos)
                        MyCoursesFragment myCoursesFragment = new MyCoursesFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, myCoursesFragment).addToBackStack(null)
                                .commit();
                        break;

                    case R.id.nav_profile:
                        // Primero cambiamos el título
                        toolbar.setTitle(getString(R.string.my_profile));
                        // Reemplazamos el fragment por el del perfil del usuario
                        ProfileFragment profileFragment = new ProfileFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, profileFragment).addToBackStack(null)
                                .commit();
                        break;

                    case R.id.nav_chat:
                        // Primero cambiamos el título
                        toolbar.setTitle(getString(R.string.titleContacts));
                        // Reemplazamos el fragment por el de contactos
                        ContactsFragment contactsFragment = new ContactsFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, contactsFragment).addToBackStack(null)
                                .commit();
                        break;

                    case R.id.nav_logout:
                        // Cerramos sesión
                        FirebaseAuth.getInstance().signOut();
                        // Volvemos al Activity de login
                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;

                    case R.id.nav_website:
                        // Abre la página del Esteve Terradas
                        Intent web = new Intent(Intent.ACTION_VIEW);
                        web.setData(Uri.parse("http://www.iesesteveterradas.cat/"));
                        startActivity(web);
                        break;

                    case R.id.nav_facebook:
                        Intent fb = new Intent(Intent.ACTION_VIEW);
                        fb.setData(Uri.parse("https://www.facebook.com/pages/IES-Esteve-Terradas-i-Illa/123296617686766"));
                        startActivity(fb);
                        break;

                    case R.id.nav_twitter:
                        Intent tw = new Intent(Intent.ACTION_VIEW);
                        tw.setData(Uri.parse("https://twitter.com/iesteveterradas?lang=ca"));
                        startActivity(tw);
                        break;
                }

                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        getProfile(user.getUid());
        getUserData(user.getUid());
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Mostrar home screen de Android
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Hace un Inflate del menu, esto añade los elementos al ActionBar si está presente
        getMenuInflater().inflate(R.menu.course_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Obtenemos el id del item seleccionado
        int id = item.getItemId();
        switch (id) {
            case R.id.action_inscribeBook:
                // Si el id es igual al icono del libro se abrirá un activity con todos los cursos
                Intent intent = new Intent(WelcomeActivity.this, CategoriesActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Busca los datos de un usuario en Cloud Firestore sabiendo su UID
     * @param UID - El UID del usuario del que queremos los datos
     */
    private void getUserData(String UID){
        final DocumentReference docRef = db.collection(FirebaseStrings.COLLECTION_1).document(UID);
        docRef.get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    // Obtener los datos del usuario
                    user_data = documentSnapshot.toObject(User.class);
                    // Mostramos el email del usuario
                    email.setText(user_data.getEmail());
                    // Ponemos los datos del usuario en el Intent de este activity
                    getIntent().putExtra(IntentExtras.EXTRA_USER_DATA, user_data);
                    // Creamos el fragment principal
                    MyCoursesFragment fragment = new MyCoursesFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }
            }).addOnFailureListener(new OnFailureListener() {
                 @Override
                 public void onFailure(@NonNull Exception e) {
                     e.printStackTrace();
                 }
             });
    }

    /**
     * Busca los datos del usuario en Realtime Database sabiendo su ID
     * @param UID - El UID del usuario del que queremos los datos
     */
    private void getProfile(String UID) {
        reference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.REFERENCE_1).child(UID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatUser user = dataSnapshot.getValue(ChatUser.class);
                // Ponemos el nombre que utiliza el usuario en el chat en el navigation header
                displayName.setText(user.getDisplayName());
                if (user.getImageURL().equals(FirebaseStrings.DEFAULT_IMAGE_VALUE)) {
                    // Si el usuario tiene como ImageURL el valor 'default', le pondremos la imagen de usuario por defecto
                    profile_image.setImageResource(R.drawable.google_user_icon);
                } else {
                    // Pone la imagen del usuario en el CircleImageView
                    if (user.getImageURL() != null) {
                        Glide.with(WelcomeActivity.this).load(user.getImageURL()).into(profile_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", databaseError.getMessage());
            }
        });
    }
}
