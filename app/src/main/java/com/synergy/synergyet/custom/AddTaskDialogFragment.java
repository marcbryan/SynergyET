package com.synergy.synergyet.custom;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.synergy.synergyet.R;
import com.synergy.synergyet.strings.IntentExtras;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddTaskDialogFragment extends DialogFragment {
    public static final String TAG = "AddTaskDialogFragment";
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private List<String> unitNames = new ArrayList<>();
    private String date;
    private String hour;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        unitNames = b.getStringArrayList(IntentExtras.EXTRA_UNITS_ARRAY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.add_task_dialog_fragment, container, false);
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
        // Ponemos los tipos de tarea disponibles en el spinner
        Spinner spinner = view.findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.task_types, android.R.layout.simple_spinner_item);
        // Ponemos el adapter al spinner
        spinner.setAdapter(adapter);
        // Ponemos las unidades en el spinner
        Spinner spinner2 = view.findViewById(R.id.unit_spinner);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, unitNames);
        // Ponemos el adapter al spinner
        spinner2.setAdapter(adapter2);
        // TextView donde se verá la fecha que selecciona el usuario
        final TextView show_date = view.findViewById(R.id.tv_date);
        // Listener del icono del calendario (para seleccionar una fecha)
        ImageView select_date = view.findViewById(R.id.iv_select_date);
        select_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date now = Calendar.getInstance().getTime();
                String str_now = sdf.format(now);
                // Primero hacemos un split para obtener la fecha
                String [] sp = str_now.split(" ");
                // A continuación otro split para obtener dia, mes y año
                String [] sp2 = sp[0].split("/");
                // Parseamos los strings de la fecha
                int day = Integer.parseInt(sp2[0]);
                // Al mes actual hay que restarle 1 porque el DatePicker empieza desde 0 (enero)
                int month = Integer.parseInt(sp2[1])-1;
                int year = Integer.parseInt(sp2[2]);
                // Creamos el DatePicker
                DatePickerDialog datePicker = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String d = dayOfMonth+"";
                        if (dayOfMonth < 10) {
                            // Si el día es menor de 10 no pone el 0, por lo tanto, lo pongo en este if
                            d = 0 + "" + dayOfMonth + "";
                        }
                        String m = (month+1)+"";
                        if ((month+1) < 10) {
                            m = 0 + "" + (month+1)+"";
                        }
                        date = d+"/"+m+"/"+year;
                        show_date.setText(date);
                    }
                }, year, month, day);
                // Lo mostramos
                datePicker.show();
            }
        });
        // TextView donde se verá la hora que seleccione el usuario
        final TextView show_hour = view.findViewById(R.id.tv_hour);
        // Listener del icono del reloj (para seleccionar una hora)
        ImageView select_hour = view.findViewById(R.id.iv_select_hour);
        select_hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePicker = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hr = hourOfDay+"";
                        if (hourOfDay < 10) {
                            hr = 0 + "" + hourOfDay;
                        }
                        String min = minute+"";
                        if (minute < 10) {
                            min = 0 + "" + minute;
                        }
                        String strHour = hr + ":" + min;
                        show_hour.setText(strHour);
                    }
                }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);
                timePicker.show();
            }
        });

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
}
