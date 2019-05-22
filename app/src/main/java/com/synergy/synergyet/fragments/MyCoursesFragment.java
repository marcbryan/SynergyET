package com.synergy.synergyet.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.synergy.synergyet.CourseActivity;
import com.synergy.synergyet.R;
import com.synergy.synergyet.custom.CoursesListAdapter;
import com.synergy.synergyet.custom.CustomDialogFragment;
import com.synergy.synergyet.model.Course;
import com.synergy.synergyet.model.User;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.util.ArrayList;

public class MyCoursesFragment extends Fragment {
    //private FirebaseUser user;
    private FirebaseFirestore db;
    private TextView textView;

    private CoursesListAdapter adapter;
    private ArrayList<Course> courses;

    //private Dialog progressDialog;
    private CustomDialogFragment dialogFragment;

    private User user_data = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_courses, container, false);

        db = FirebaseFirestore.getInstance();
        //user = FirebaseAuth.getInstance().getCurrentUser();

        // TODO: No funciona
        // Mostramos el Dialog de espera antes de obtener los datos del usuario
        showProgressDialog(view.getContext().getString(R.string.loading_user_courses), container, view.getContext());
        // Obtenemos los datos del usuario con getSerializableExtra()
        User user = (User) getActivity().getIntent().getSerializableExtra(IntentExtras.EXTRA_USER_DATA);
        getUserCourses(user.getCourses(), user.getName());

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
                final Course course = (Course) listView.getItemAtPosition(position);
                Intent intent = new Intent(view.getContext(), CourseActivity.class);
                intent.putExtra(IntentExtras.EXTRA_COURSE_DATA, course);
                startActivity(intent);
            }
        });

        return view;
    }

    // TODO: No funciona, Cambiar style y textColor de los AlertDialog de todas las activitys
    /**
     * Muesta un diálogo con un botón de ok y el texto que le pasamos como parámetro
     * @param dialog_txt - El texto a mostrar en el diálogo
     * @param context - El contexto de la aplicación
     */
    private void showDialog(String dialog_txt, Context context) {
        // Creo un diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(dialog_txt)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialogOK_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });
        AlertDialog alert = builder.create();
        // Lo muestro
        alert.show();
    }

    //TODO: Revisar, no funciona
    /**
     * Muestra un AlertDialog que emula a un ProgressDialog (la clase ProgressDialog está deprecated, por eso usamos este)
     * @param msg - El mensaje que se mostrará en el Dialog
     * @param container - El contenedor de las vistas
     * @param context - El contexto de la aplicación
     */
    private void showProgressDialog(String msg, ViewGroup container, Context context){
        dialogFragment = CustomDialogFragment.newInstance(msg);
        dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog_fragment");
        /*
        // Creamos el AlertDialog y le aplicamos un style personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        // Inflate de la vista
        View view = getLayoutInflater().inflate(R.layout.custom_progress_dialog, container, false);
        // Obtenemos el TextView de la vista para poder poner el texto
        TextView tv_message = view.findViewById(R.id.loading_msg);
        tv_message.setText(msg);
        // Ponemos la vista y lo hacemos no cancelable (para hacerlo modal)
        builder.setView(view)
                .setCancelable(false);
        progressDialog = builder.create();
        // Mostramos el Dialog
        progressDialog.show();*/
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
                     // Finaliza ProgressBar
                     //progressDialog.dismiss();
                     dialogFragment.dismiss();
                     showDialog(getContext().getString(R.string.dialog_error_userData), getContext());
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
                                    adapter.notifyDataSetChanged();
                                } else {
                                    showDialog(getString(R.string.dialog_error_courses), getContext());
                                    //System.out.println("Error writing document -> "+task.getException());
                                }
                            }
                        });
            }
            // Mostrar Toast de bienvienida
            Toast.makeText(getContext(), getString(R.string.toast1) + " " + name + "!! :)",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Mostramos en un TextView que el usuario no está inscrito en ningún curso
            textView.setText(getString(R.string.no_courses));
        }
        // Finaliza ProgressBar
        dialogFragment.dismiss();
        //progressDialog.dismiss();
    }
}
