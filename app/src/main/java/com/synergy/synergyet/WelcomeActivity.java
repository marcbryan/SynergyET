package com.synergy.synergyet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.synergy.synergyet.custom.CoursesListAdapater;
import com.synergy.synergyet.model.Course;
import com.synergy.synergyet.model.Unit;
import com.synergy.synergyet.strings.FirebaseStrings;

import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Toolbar toolbar;
    private ListView listView;
    private CoursesListAdapater adapater;
    private ArrayList<Course> courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Obtenemos el toolbar y lo añadimos al activity (para que se vean los iconos)
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = findViewById(R.id.courses_list);
        db = FirebaseFirestore.getInstance();

        courses = new ArrayList<>();
        // TODO: Prueba, borrar al acabar
        courses.add(new Course(1, "AMS2 M6 - Acceso a datos", "Informática", "CFGS", 99, "pwd", "2018-2019", false));
        courses.add(new Course(2, "AMS2 M7 - Interficies", "Informática", "CFGS", 99, "pwd2", "2018-2019", false));
        /*
        courses.add(new Course(1, "AMS2 M6 - Acceso a datos", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(2, "AMS2 M7 - Interficies", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));
        courses.add(new Course(1, "AMS2 M6 - Acceso a datos", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(2, "AMS2 M7 - Interficies", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));
        courses.add(new Course(1, "AMS2 M6 - Acceso a datos", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(2, "AMS2 M7 - Interficies", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));
        courses.add(new Course(1, "AMS2 M6 - Acceso a datos", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(2, "AMS2 M7 - Interficies", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));
        courses.add(new Course(1, "AMS2 M6 - Acceso a datos", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(2, "AMS2 M7 - Interficies", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));
        courses.add(new Course(1, "AMS2 M6 - Acceso a datos", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(2, "AMS2 M7 - Interficies", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));
        */
        adapater = new CoursesListAdapater(courses, this);

        // Añadimos el adapter al ListView
        listView.setAdapter(adapater);

    }

    @Override
    public void onBackPressed() {
        // Mostrar home screen de Android
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Hace un Inflate del menu, esto añade los elementos al ActionBar si está presente
        getMenuInflater().inflate(R.menu.course_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Obtenemos el id del item seleccionado
        int id = item.getItemId();
        if (id == R.id.action_inscribeBook) {
            // Si el id es igual al icono del libro se abrirá un activity con todos los cursos
            Intent intent = new Intent(WelcomeActivity.this, CategoriesActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
