package com.synergy.synergyet;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.synergy.synergyet.custom.AddTaskDialogFragment;
import com.synergy.synergyet.custom.UnitExpandableListAdapter;
import com.synergy.synergyet.model.Course;
import com.synergy.synergyet.model.UnitTask;
import com.synergy.synergyet.model.Unit;
import com.synergy.synergyet.model.User;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class CourseActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private User user;
    private ExpandableListView expandableListView;
    private UnitExpandableListAdapter expandableListAdapter;
    private Map<String, List<UnitTask>> expandableListDetail = new LinkedHashMap<>();
    private List<String> expandableListTitle;
    private ArrayList<Integer> unitsIDs = new ArrayList<>();
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        Intent intent = getIntent();
        Course course = (Course) intent.getSerializableExtra(IntentExtras.EXTRA_COURSE_DATA);
        user = (User) intent.getSerializableExtra(IntentExtras.EXTRA_USER_DATA);

        // Ocultamos el botón de añadir
        fab = findViewById(R.id.fab);
        fab.hide();

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
        // Obtenemos las unidades del curso
        getUnits(course.getCourse_id());
        // Pedimos permiso de escritura, en el caso de que no haya permiso
        requestWritePermission();
    }

    /**
     * Método para obtener todas las unidades de un curso sabiendo su ID
     * @param course_id - El ID del curso que queremos las unidades
     */
    private void getUnits(final int course_id) {
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
                            // Añadimos las unidades al HashMap
                            expandableListDetail.put(unit.getName(), new ArrayList<UnitTask>());
                            unitsIDs.add(unit.getUnit_id());
                        }
                        // Creamos un ArrayList con los elementos padre (las UFs)
                        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
                        // Creamos el adapter para el ExpandableListView
                        expandableListAdapter = new UnitExpandableListAdapter(CourseActivity.this, expandableListTitle, expandableListDetail, user.getName()+" "+user.getSurname(), user.getType(), course_id);
                        // Ponemos el adapter en el ExpandableListView
                        expandableListView.setAdapter(expandableListAdapter);
                        // Después de añadir las unidades (las UFs), añadimos los hijos (las tareas) de cada unidad
                        for (Unit unit : units) {
                            getTasks(unit.getUnit_id(), unit.getName());
                        }
                        // Si el usuario es un profesor, mostraremos el botón de añadir tarea
                        if (user.getType().equals(FirebaseStrings.USER_TYPE_TEACHER)) {
                            fab.show();
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Creamos el DialogFragment
                                    AddTaskDialogFragment dialog = new AddTaskDialogFragment();
                                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                    Bundle b = new Bundle();
                                    b.putStringArrayList(IntentExtras.EXTRA_UNITS_ARRAY, new ArrayList<>(expandableListTitle));
                                    b.putIntegerArrayList(IntentExtras.EXTRA_UNITS_IDS_ARRAY, unitsIDs);
                                    b.putInt(IntentExtras.EXTRA_COURSE_ID, course_id);
                                    // Se lo pasamos al DialogFragment
                                    dialog.setArguments(b);
                                    // Lo mostramos
                                    dialog.show(ft, AddTaskDialogFragment.TAG);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Método para obtener las tareas de una unidad sabiendo su ID
     * @param unit_id - El ID de la unidad
     * @param unit_name - El nombre de la unidad (para actualizar el valor del HashMap)
     */
    private void getTasks(int unit_id, final String unit_name) {
        db.collection(FirebaseStrings.COLLECTION_4)
                .whereEqualTo(FirebaseStrings.FIELD3_C4, unit_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<UnitTask> tasks = new ArrayList<>();
                            // Añadimos las tareas al array
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                UnitTask unitTask = documentSnapshot.toObject(UnitTask.class);
                                tasks.add(unitTask);
                            }
                            // Cambiamos el valor del HashMap
                            expandableListDetail.put(unit_name, tasks);
                            // Notificamos el cambio
                            expandableListAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("ERROR", task.getException()+"");
                        }
                    }
                });

    }

    /**
     * Muestra un Dialog pidiendo permisos de escritura para poder realizar descargas,
     * si ya hay permisos no se mostrará nada
     */
    public void requestWritePermission() {
        String[] perms = { Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            // Preguntamos por permisos
            EasyPermissions.requestPermissions(this, "",
                    1, perms);
        }
    }
}
