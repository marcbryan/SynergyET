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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.synergy.synergyet.custom.CoursesListAdapater;
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
    private ListView listView;
    private CoursesListAdapater adapater;
    private ArrayList<Course> courses;

    private User user_data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //TODO: Pruebas, no definitivo
        // Obtenemos los datos del usuario (solo funcionará si hizo login)
        user_data = (User) getIntent().getSerializableExtra(IntentExtras.EXTRA_USER_DATA);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        checkUserData(user.getUid());

        // Obtenemos el toolbar y lo añadimos al activity (para que se vean los iconos)
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = findViewById(R.id.zero_courses);

        courses = new ArrayList<>();
        adapater = new CoursesListAdapater(courses, this);
        listView = findViewById(R.id.courses_list);
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
        // Hace un Inflate del menu, esto añade los elementos al ActionBar si está presente
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

    //TODO: Comentar método (método no definitivo, puede cambiar)
    private void checkUserData(String UID) {
        if (user_data != null){
            if (user_data.getCourses() != null) {
                getUserCourses(user_data.getCourses());
            } else {
                // Mostramos en un TextView que el usuario no está inscrito en ningún curso
                textView.setText(getString(R.string.no_courses));
            }
        } else {
            //TODO: Obtener datos del usuario
        }
    }

    //TODO: Comentar método
    public void getUserCourses(ArrayList<Integer> courses_ids){
        for (int course_id : courses_ids) {
            db.collection(FirebaseStrings.COLLECTION_2)
                    .whereEqualTo(FirebaseStrings.FIELD1_C2, course_id)
                    // Lo ordenamos por el ID para poder aplicar el filtro limit()
                    .orderBy(FirebaseStrings.FIELD1_C2)
                    // La consulta devolverá 1 resultado
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                //TODO: Pruebas
                                System.out.println("Curso -> " + task.getResult().getDocuments().get(0).toObject(Course.class).toString());
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Course course = document.toObject(Course.class);
                                    // Añadimos el curso al array
                                    courses.add(course);
                                    // Notificamos al adapter que se añadido un curso (para que se vea en el ListView)
                                    adapater.notifyDataSetChanged();
                                    break;
                                }
                            } else {
                                //TODO: Mostrar AlertDialog de error
                            }
                        }
                    });
        }
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
