package com.synergy.synergyet;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.synergy.synergyet.custom.CoursesListAdapter;
import com.synergy.synergyet.fragments.ContactsFragment;
import com.synergy.synergyet.fragments.MyCoursesFragment;
import com.synergy.synergyet.model.Course;
import com.synergy.synergyet.model.Unit;
import com.synergy.synergyet.model.User;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {
    private FirebaseUser user;
    private FirebaseFirestore db;
    private Toolbar toolbar;
    private TextView textView;
    private TextView displayName;
    private TextView email;
    private ImageView profile_image;

    private CoursesListAdapter adapter;
    private ArrayList<Course> courses;

    private Dialog progressDialog;
    private DrawerLayout drawer;

    private User user_data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        /*
        // Mostramos el Dialog de espera antes de obtener los datos del usuario
        showProgressDialog(getString(R.string.loading_user_courses));
        getUserData(user.getUid());*/

        // Obtenemos el toolbar y lo añadimos al activity (para que se vean los iconos)
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View nav_header = navigationView.getHeaderView(0);
        displayName = nav_header.findViewById(R.id.user_displayName);
        email = nav_header.findViewById(R.id.user_email);
        profile_image = nav_header.findViewById(R.id.profile_image);
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
                        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        //new ProfileFragment()).commit();
                        break;

                    case R.id.nav_message:
                        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                //new MessageFragment()).commit();
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

                }

                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

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

    // TODO: Cambiar style y textColor de los AlertDialog de todas las activitys
    /**
     * Muesta un diálogo con un botón de ok y el texto que le pasamos como parámetro
     * @param dialog_txt - El texto a mostrar en el diálogo
     */
    private void showDialog(String dialog_txt) {
        // Creo un diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
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
     * Muestra un AlertDialog que emula a un ProgressDialog (la clase ProgressDialog está deprecated, por eso usamos este)
     * @param msg - El mensaje que se mostrará en el Dialog
     */
    private void showProgressDialog(String msg){
        // Creamos el AlertDialog y le aplicamos un style personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        // Inflate de la vista
        View view = getLayoutInflater().inflate(R.layout.custom_progress_dialog, null);
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
                    // Mostramos el nombre y el email del usuario
                    displayName.setText(user_data.getName());
                    email.setText(user_data.getUsername());
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
     * Busca los cursos a los que está inscrito el usuario para poder mostrarlos en el ListView
     * @param courses_ids - El array con los IDs de los cursos a los que está inscrito el usuario (podria ser nulo, lo comprueba este método)
     * @param name - El nombre del usuario para enseñar un Toast de bienvenida al finalizar este método
     */
    private void getUserCourses(ArrayList<Integer> courses_ids, String name){
        if (courses_ids != null) {
            for (int course_id : courses_ids) {
                db.collection(FirebaseStrings.COLLECTION_2)
                        .whereEqualTo(FirebaseStrings.FIELD1_C2, course_id)
                        // Aplicamos el filtro limit() para que la consulta devuelva 1 resultado
                        .limit(1)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Obtenemos el primer resultado de la consulta (no debería devolver más de 1, pero lo hago para evitar hacer un bucle)
                                    Course course = task.getResult().getDocuments().get(0).toObject(Course.class);
                                    // Añadimos el curso al array
                                    courses.add(course);
                                    // Notificamos al adapter que se añadido un curso (para que se vea en el ListView)
                                    adapter.notifyDataSetChanged();
                                } else {
                                    showDialog(getString(R.string.dialog_error_courses));
                                    //System.out.println("Error writing document -> "+task.getException());
                                }
                            }
                        });
            }
            // Mostrar Toast de bienvienida
            Toast.makeText(WelcomeActivity.this, getString(R.string.toast1) + " " + name + "!! :)",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Mostrar Toast de bienvienida
            Toast.makeText(WelcomeActivity.this, getString(R.string.toast1) + " " + name + "!! :)",
                    Toast.LENGTH_SHORT).show();
            // Mostramos en un TextView que el usuario no está inscrito en ningún curso
            textView.setText(getString(R.string.no_courses));
        }
        // Finaliza ProgressBar
        progressDialog.dismiss();
    }

    //TODO: Cambiar de Activity los métodos para hacer inserts

    private void addCourse(Course course) {
        db.collection(FirebaseStrings.COLLECTION_2)
                // El ID del documento se genera automáticamente, para hacerlo usamos el método add() en vez de set()
                .add(course)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //TODO: Quitar texto hardcordeado
                        Toast.makeText(WelcomeActivity.this, "Curso añadido correctamente",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DOC", "Error writing document", e);
                    }
                });
    }

    private void addUnit(Unit unit) {
        db.collection(FirebaseStrings.COLLECTION_3)
                // El ID del documento se genera automáticamente, para hacerlo usamos el método add() en vez de set()
                .add(unit)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //TODO: Quitar texto hardcordeado
                        Toast.makeText(WelcomeActivity.this, "Unidad añadida correctamente!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DOC", "Error writing document", e);
                    }
                });
    }
}
