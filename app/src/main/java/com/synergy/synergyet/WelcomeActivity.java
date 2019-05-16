package com.synergy.synergyet;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.synergy.synergyet.custom.CoursesListAdapater;
import com.synergy.synergyet.model.Course;
import com.synergy.synergyet.model.Unit;
import com.synergy.synergyet.model.User;
import com.synergy.synergyet.strings.FirebaseStrings;

import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {
    private FirebaseUser user;
    private FirebaseFirestore db;
    private Toolbar toolbar;
    private TextView textView;
    private ListView listView;
    private CoursesListAdapater adapater;
    private ArrayList<Course> courses;

    private String toast_txt1;

    private User user_data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        getUserData(user.getUid());

        toast_txt1 = getString(R.string.toast1);

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
                    getUserCourses(user_data.getCourses(), user_data.getName());
                }
            }).addOnFailureListener(new OnFailureListener() {
                 @Override
                 public void onFailure(@NonNull Exception e) {
                     showDialog(getString(R.string.dialog_error_userData));
                     //System.out.println("Error -> " + e);
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
                                    adapater.notifyDataSetChanged();
                                } else {
                                    showDialog(getString(R.string.dialog_error_courses));
                                    //System.out.println("Error writing document -> "+task.getException());
                                }
                            }
                        });
            }
            //TODO: Finaliza ProgressBar

            // Mostrar Toast de bienvienida
            Toast.makeText(WelcomeActivity.this, toast_txt1 + " " + name + "!! :)",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Mostrar Toast de bienvienida
            Toast.makeText(WelcomeActivity.this, toast_txt1 + " " + name + "!! :)",
                    Toast.LENGTH_SHORT).show();
            // Mostramos en un TextView que el usuario no está inscrito en ningún curso
            textView.setText(getString(R.string.no_courses));
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
