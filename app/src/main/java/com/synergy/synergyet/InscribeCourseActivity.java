package com.synergy.synergyet;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.synergy.synergyet.custom.CoursesListAdapter;
import com.synergy.synergyet.model.Course;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.util.ArrayList;

public class InscribeCourseActivity extends AppCompatActivity {
    private FirebaseUser user;
    private FirebaseFirestore db;

    private Toolbar toolbar;
    private ListView listView;
    private ArrayList<Course> courses;
    private CoursesListAdapter adapter;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscribe_course);
        Intent intent = getIntent();
        String category = intent.getStringExtra(IntentExtras.EXTRA_CATEGORY_NAME);
        String group = intent.getStringExtra(IntentExtras.EXTRA_GROUP_NAME);

        // Obtenemos el toolbar y lo añadimos al activity (para que se vean los iconos)
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Mostrar la flecha para volver atrás
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Listener para la flecha para volver atrás
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // Mostramos la categoria y el grupo al que pertenece (el que seleccionó el usuario en el activity anterior)
        TextView tv_category = findViewById(R.id.infoBar_title);
        String txt = group+" > "+category;
        tv_category.setText(txt);

        // Obtener la instancia FirebaseFirestore (MUY IMPORTANTE!!)
        db = FirebaseFirestore.getInstance();
        // Para obtener el usuario actual
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Creamos el array que tendrá los datos del ListView
        courses = new ArrayList<>();
        // Mostramos Dialog de espera
        showProgressDialog(getString(R.string.loading_cg_courses));
        getCourses(category);

        // Buscamos el ListView
        listView = findViewById(R.id.courses_list);
        // Añadimos un listener (al pulsar un elemento del ListView)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Course course = (Course) listView.getItemAtPosition(position);
                // Creamos el InputDialog
                builder = new AlertDialog.Builder(InscribeCourseActivity.this, R.style.CustomAlertDialog);
                LayoutInflater inflater = getLayoutInflater();
                // Le ponemos la vista personalizada
                View dialogView = inflater.inflate(R.layout.input_dialog, null);
                builder.setCancelable(false);
                builder.setView(dialogView);

                final TextInputLayout til = dialogView.findViewById(R.id.text_input_layout);
                // Ponemos un contorno al TextInputLayout tipo Google (ejemplo -> https://i.stack.imgur.com/t2stI.png)
                til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                til.setBoxCornerRadii(5, 5, 5, 5);

                // Obtenemos el TextInputEditText del InputDialog (para poder obtener el texto que introduce el usuario)
                final TextInputEditText et_pass = dialogView.findViewById(R.id.input_password);
                et_pass.addTextChangedListener(new TextWatcher() {
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
                        // Obtenemos la contraseña que ha introducido el usuario
                        String pwd = et_pass.getText().toString();
                        if (pwd.equals("")) {
                            // Mostramos mensaje de error (no ha escrito contraseña)
                            til.setError(getString(R.string.dialog3_error_msg1));
                        } else {
                            if (pwd.equals(course.getPassword())) {
                                //TODO: Mostrar ProgressDialog 2
                                checkAlreadyInscribed(user.getUid(), course.getCourse_id());
                            } else {
                                // Mostramos mensaje de error (contraseña incorrecta)
                                til.setError(getString(R.string.dialog3_error_msg2));
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_course_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.searchCourse));
        // Añadimos el listener de búsquedas mientras el usuario escribe texto en el input
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Para que el adapter filtre según el texto escrito
                adapter.getFilter().filter(s);
                return true;
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Muesta un diálogo con un botón de ok y el texto que le pasamos como parámetro
     * @param dialog_txt - El texto a mostrar en el diálogo
     */
    private void showDialog(String dialog_txt) {
        // Creo un diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(InscribeCourseActivity.this);
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
     * Obtiene todos los cursos que sean de la categoria que le pasamos como parámetro
     * @param category - La categoria de los cursos
     */
    private void getCourses(String category){
        db.collection(FirebaseStrings.COLLECTION_2)
                .whereEqualTo(FirebaseStrings.FIELD3_C2, category)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                Course course = documentSnapshot.toObject(Course.class);
                                courses.add(course);
                            }
                            // Creamos el adapter
                            adapter = new CoursesListAdapter(courses, InscribeCourseActivity.this);
                            // Se lo asignamos al ListView
                            listView.setAdapter(adapter);
                            // Activamos la filtración de datos para poder hacer búsquedas
                            listView.setTextFilterEnabled(true);
                            // Finaliza ProgressDialog
                            progressDialog.dismiss();
                        } else {
                            // Finaliza ProgressDialog
                            progressDialog.dismiss();
                            // Muestro AlertDialog de error
                            showDialog(getString(R.string.dialog4_error));
                            //System.out.println("Error getting documents: "+task.getException());
                        }
                    }
                });
    }

    /**
     * Comprueba si el usuario está inscrito en un curso o no
     * @param UID - El UID del usuario que se quiere inscribir a un curso
     * @param course_id - El ID del curso del que queremos saber si está inscrito o no
     */
    private void checkAlreadyInscribed(final String UID, final int course_id) {
        db.collection(FirebaseStrings.COLLECTION_1)
                .whereEqualTo(FirebaseStrings.FIELD1_C1, UID)
                // Este Where equivale a "El array contiene el id del curso?"
                .whereArrayContains(FirebaseStrings.FIELD1_C7, course_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                //TODO: Finaliza ProgressDialog 2
                                // Cerramos el InputDialog
                                dialog.dismiss();
                                // Mostramos AlertDialog de error diciendo que ya está inscrito al curso
                                showDialog(getString(R.string.dialog6_txt));
                            } else {
                                // Si el usuario no está inscrito, actualizar los cursos (añadimos el ID del curso al array de IDs de cursos del usuario en Cloud Firestore)
                                updateUserCourses(UID, course_id);
                            }
                        } else {
                            //TODO: Finaliza ProgressDialog 2
                            // Muestro AlertDialog de error
                            showDialog(getString(R.string.dialog5_error));
                            //System.out.println("Error getting documents: "+task.getException());
                        }
                    }
                });
    }

    /**
     * Añade el ID del curso al que se inscribe el usuario a su array de cursos (En Cloud Firestore)
     * @param UID - El UID del usuario que se quiere inscribir a un curso
     * @param course_id - El ID del curso al que el usuario quiere inscribirse
     */
    private void updateUserCourses(String UID, int course_id) {
        DocumentReference documentRef = db.collection(FirebaseStrings.COLLECTION_1).document(UID);
        documentRef
                .update(FirebaseStrings.FIELD1_C7, FieldValue.arrayUnion(course_id))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //TODO: Finaliza ProgressDialog 2
                        // Mostramos un toast diciendo que se ha inscrito correctamente
                        Toast.makeText(InscribeCourseActivity.this, getString(R.string.toast3), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(InscribeCourseActivity.this, WelcomeActivity.class);
                        // Finaliza este Activity
                        finish();
                        // Volvemos al primero (llamada asíncrona)
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //TODO: Finaliza ProgressDialog 2
                        // Muestro AlertDialog de error
                        showDialog(getString(R.string.dialog5_error));
                    }
                });
    }

}
