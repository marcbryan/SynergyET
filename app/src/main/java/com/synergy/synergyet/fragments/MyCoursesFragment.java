package com.synergy.synergyet.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.synergy.synergyet.CourseActivity;
import com.synergy.synergyet.R;
import com.synergy.synergyet.custom.CoursesListAdapter;
import com.synergy.synergyet.model.Course;
import com.synergy.synergyet.model.User;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.util.ArrayList;

public class MyCoursesFragment extends Fragment {
    private FirebaseFirestore db;
    private TextView textView;
    private CoursesListAdapter adapter;
    private ArrayList<Course> courses;
    private User user;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_courses, container, false);
        // Instancia de Cloud Firestore
        db = FirebaseFirestore.getInstance();
        // Obtenemos los datos del usuario con getSerializableExtra()
        user = (User) getActivity().getIntent().getSerializableExtra(IntentExtras.EXTRA_USER_DATA);
        getUserCourses(user.getCourses());

        // TextView en el que se mostrará un mensaje solo si el usuario no tiene cursos
        textView = view.findViewById(R.id.zero_courses);

        courses = new ArrayList<>();
        adapter = new CoursesListAdapter(courses, view.getContext());
        final ListView listView = view.findViewById(R.id.courses_list);
        // Añadimos el adapter al ListView
        listView.setAdapter(adapter);
        // Añadimos un listener (al pulsar un elemento del ListView)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtenemos los datos del curso
                final Course course = (Course) listView.getItemAtPosition(position);
                // Se los pasamos al siguiente Activity
                Intent intent = new Intent(view.getContext(), CourseActivity.class);
                intent.putExtra(IntentExtras.EXTRA_COURSE_DATA, course);
                intent.putExtra(IntentExtras.EXTRA_USER_DATA, user);
                startActivity(intent);
            }
        });
        return view;
    }

    /**
     * Muesta un diálogo con un botón de OK y el texto que le pasamos como parámetro
     * @param dialog_txt - El texto a mostrar en el diálogo
     * @param context - El contexto de la aplicación
     */
    private void showDialog(String dialog_txt, Context context) {
        // Creo un diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
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
     * Busca los cursos a los que está inscrito el usuario para poder mostrarlos en el ListView
     * @param courses_ids - El array con los IDs de los cursos a los que está inscrito el usuario (podria ser nulo, lo comprueba este método)
     */
    private void getUserCourses(ArrayList<Integer> courses_ids){
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
                                    showDialog(getString(R.string.dialog_error_courses), getContext());
                                    Log.e("Error", " -> "+task.getException());
                                }
                            }
                        });
            }
        } else {
            // Si el usuario es un alumno
            if (user.getType().equals(FirebaseStrings.DEFAULT_USER_TYPE)) {
                // Mostramos en un TextView que el usuario no está inscrito en ningún curso
                textView.setText(getString(R.string.no_courses));
            }
        }
    }
}
