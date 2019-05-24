package com.synergy.synergyet.custom;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.synergy.synergyet.R;
import com.synergy.synergyet.model.UnitTask;
import com.synergy.synergyet.strings.IntentExtras;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TaskDialogFragment extends DialogFragment {
    public static final String TAG = "TaskDialogFragment";
    private UnitTask task;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private String unitTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        unitTitle = b.getString(IntentExtras.EXTRA_UNIT_NAME);
        task = (UnitTask) b.getSerializable(IntentExtras.EXTRA_TASK_DATA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.task_dialog_fragment, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cerramos el DialogFragment
                dismiss();
            }
        });
        TextView tbTitle = view.findViewById(R.id.toolbar_title);
        tbTitle.setText(unitTitle);

        // Ponemos el nombre de la tarea
        TextView taskTitle = view.findViewById(R.id.title);
        taskTitle.setText(task.getTaskName());
        // Ponemos la fecha límite
        TextView tv_deadLine = view.findViewById(R.id.deadLine);
        tv_deadLine.setText(task.getDead_line());
        try {
            // Fecha de entrega
            Date deadLine = sdf.parse(task.getDead_line());
            // Fecha de hoy
            Date now = Calendar.getInstance().getTime();
            Button uploadFile = view.findViewById(R.id.btn_uploadFile);
            if (deadLine.compareTo(now) > 0) {
                // Todavía se puede entregar (la fecha de entrega es posterior a la de ahora)
                uploadFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: Subir archivo
                    }
                });
            } else {
                // Ya no se puede entregar la tarea
                // Ocultamos el botón de subir archivo
                uploadFile.setVisibility(Button.GONE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
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

    //TODO: Método para obtener datos de la tarea del usuario
}
