package com.synergy.synergyet.custom;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.synergy.synergyet.R;
import com.synergy.synergyet.model.TaskDelivered;
import com.synergy.synergyet.model.UnitTask;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.util.ArrayList;
import java.util.List;

public class CorrectDialogFragment extends DialogFragment {
    public static final String TAG = "CorrectDialogFragment";
    private UnitTask taskData;
    private FirebaseFirestore db;
    private ListView listView;
    private List<TaskDelivered> tasksDelivered = new ArrayList<>();
    private CorrectListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        taskData = (UnitTask) b.getSerializable(IntentExtras.EXTRA_TASK_DATA);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.correct_dialog_fragment, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Si se pulsa la cruz, cerramos el DialogFragment
                dismiss();
            }
        });
        // Ponemos el titulo en toolbar
        TextView tbTitle = view.findViewById(R.id.toolbar_title);
        tbTitle.setText(taskData.getTaskName());
        listView = view.findViewById(R.id.listTasksDelivered);
        // Llamamos al método para que ponga las tareas entregadas en el ListView
        getStudentsTasks(taskData.getTask_id());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        // Después de que se cree la vista la haremos pantalla completa
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    /**
     * Obtiene todas las tareas que se hayan entregado de una entrega
     * @param task_id - El ID de la tarea que los alumnos tienen que entregar
     */
    private void getStudentsTasks(int task_id) {
        db.collection(FirebaseStrings.COLLECTION_5)
                .whereEqualTo(FirebaseStrings.FIELD5_C5, task_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                TaskDelivered tDelivered = documentSnapshot.toObject(TaskDelivered.class);
                                tasksDelivered.add(tDelivered);
                            }
                            // Creamos el adapter y se lo asignamos al ListView
                            adapter = new CorrectListAdapter(getContext(), tasksDelivered);
                            listView.setAdapter(adapter);
                        } else {
                            Log.e("ERROR", task.getException()+"");
                        }
                    }
                });
    }
}
