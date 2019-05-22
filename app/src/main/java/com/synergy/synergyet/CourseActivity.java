package com.synergy.synergyet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.synergy.synergyet.custom.UnitExpandableListAdapter;
import com.synergy.synergyet.model.Course;
import com.synergy.synergyet.model.Task;
import com.synergy.synergyet.model.Unit;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CourseActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ExpandableListView expandableListView;
    private UnitExpandableListAdapter expandableListAdapter;
    private Map<String, List<Task>> expandableListDetail = new LinkedHashMap<>();
    private List<String> expandableListTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        Intent intent = getIntent();
        Course course = (Course) intent.getSerializableExtra(IntentExtras.EXTRA_COURSE_DATA);

        // Obtenemos el toolbar y lo añadimos al activity (para que se vean los iconos)
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        // Ponemos el nombre del curso como título en el toolbar
        TextView tv_title = findViewById(R.id.toolbar_title);
        tv_title.setText(course.getName());
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

        // Instancia de Firebase Firestore
        db = FirebaseFirestore.getInstance();
        // Buscamos el ExpandableListView en activity_course.xml
        expandableListView = findViewById(R.id.expandable_list);
        getUnits(course.getCourse_id());
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //TODO: Quitar toast de prueba
                Toast.makeText(v.getContext(), expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition).getTaskName(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });


    }

    private void getUnits(int course_id) {
        db.collection(FirebaseStrings.COLLECTION_3)
                .whereEqualTo(FirebaseStrings.FIELD6_C3, course_id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        // Convertimos los documentos en una lista de unidades (las UFs)
                        List<Unit> units = snapshot.toObjects(Unit.class);
                        // Ordenamos el array
                        Collections.sort(units, new Comparator<Unit>() {
                            @Override
                            public int compare(Unit o1, Unit o2) {
                                // Si devuelve un valor positivo significa que el valor 1 es mayor que el valor 2
                                // Si devuelve un valor negativo significa que el valor 1 es menor que el valor 2
                                // Si devuelve 0 significa que son iguales
                                return o1.getOrder() - o2.getOrder();
                            }
                        });
                        for (Unit unit : units) {
                            // Añadimos los nombres de los trabajos en un array
                            List <Task> tasks = new ArrayList<>();
                            if (unit.getTasks() != null) {
                                tasks = unit.getTasks();
                            }
                            // Guardamos los datos en el LinkedHashMap (muy importante utilizar LinkedHashMap, si no el orden del HashMap será diferente)
                            expandableListDetail.put(unit.getName(), tasks);
                        }
                        // Creamos un ArrayList con los elementos padre (las UFs)
                        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
                        // Creamos el adapter para el ExpandableListView
                        expandableListAdapter = new UnitExpandableListAdapter(CourseActivity.this, expandableListTitle, expandableListDetail);
                        // Ponemos el adapter en el ExpandableListView
                        expandableListView.setAdapter(expandableListAdapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
