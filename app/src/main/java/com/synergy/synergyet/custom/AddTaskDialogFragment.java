package com.synergy.synergyet.custom;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.synergy.synergyet.R;
import com.synergy.synergyet.model.UnitTask;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddTaskDialogFragment extends DialogFragment {
    public static final String TAG = "AddTaskDialogFragment";
    public final int CHOOSE_FILE_REQUESTCODE = 100;
    private SimpleDateFormat sdf_date = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat sdf_hour = new SimpleDateFormat("HH:mm");
    private List<String> unitNames = new ArrayList<>();
    private List<Integer> unitsIDs = new ArrayList<>();
    private int course_id;
    private FirebaseFirestore db;
    private TextView tv_fileName;
    private Uri uri;
    private Dialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtenemos los extras que añadimos antes de mostrar este DialogFragment
        Bundle b = getArguments();
        unitNames = b.getStringArrayList(IntentExtras.EXTRA_UNITS_ARRAY);
        unitsIDs = b.getIntegerArrayList(IntentExtras.EXTRA_UNITS_IDS_ARRAY);
        course_id = b.getInt(IntentExtras.EXTRA_COURSE_ID);
        // Instancia de Firebase Firestore
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        tv_fileName = view.findViewById(R.id.tv_file_name);

        final TextInputLayout til_taskName = view.findViewById(R.id.til_taskName);
        final TextInputLayout til_date = view.findViewById(R.id.til_date);
        final TextInputLayout til_hour = view.findViewById(R.id.til_hour);

        final RelativeLayout row4 = view.findViewById(R.id.row4);
        final RelativeLayout row5 = view.findViewById(R.id.row5);
        final RelativeLayout row6 = view.findViewById(R.id.row6);

        final String [] types = getResources().getStringArray(R.array.task_types);
        // Ponemos los tipos de tarea disponibles en el spinner
        final Spinner spinner = view.findViewById(R.id.type_spinner);
        // Listener spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = parent.getItemAtPosition(position).toString();
                if (type.equals(types[0]) || type.equals(types[2])) {
                    // Si el tipo de tarea que selecciona el usuario es "Entrega" o "Examen"
                    // Ocultamos la opción de subir archivo
                    row6.setVisibility(RelativeLayout.GONE);
                    // Mostramos la selección de fecha y hora
                    row4.setVisibility(RelativeLayout.VISIBLE);
                    row5.setVisibility(RelativeLayout.VISIBLE);
                }
                else if (type.equals(types[1])) {
                    // Si el tipo de tarea que selecciona el usuario es "Documento para alumnos"
                    // Ocultamos la selección de fecha y hora
                    row4.setVisibility(RelativeLayout.GONE);
                    row5.setVisibility(RelativeLayout.GONE);
                    // Mostramos la opción de subir archivo
                    row6.setVisibility(RelativeLayout.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.task_types, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Ponemos el adapter al spinner
        spinner.setAdapter(adapter);
        // Ponemos las unidades en el spinner
        final Spinner spinner2 = view.findViewById(R.id.unit_spinner);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item, unitNames);
        adapter2.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Ponemos el adapter al spinner
        spinner2.setAdapter(adapter2);
        // TextInputEditText donde se verá la fecha que selecciona el usuario
        final TextInputEditText show_date = view.findViewById(R.id.et_date);
        show_date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (til_date.getError() != null) {
                    // Una vez el usuario escriba, se borrará el error
                    til_date.setError(null);
                }
            }
        });

        // Listener del icono del calendario (para seleccionar una fecha)
        ImageView select_date = view.findViewById(R.id.iv_select_date);
        select_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Seleccionamos la fecha de hoy
                int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                int month = Calendar.getInstance().get(Calendar.MONTH);
                int year = Calendar.getInstance().get(Calendar.YEAR);
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
                            // Si el mes es menor de 10 no pone el 0, por lo tanto, lo pongo en este if
                            m = 0 + "" + (month+1)+"";
                        }
                        // Guardamos la fecha en la variable y la mostramos
                        String date = d+"/"+m+"/"+year;
                        show_date.setText(date);
                    }
                }, year, month, day);
                // Lo mostramos
                datePicker.show();
            }
        });

        // TextInputEditText donde se verá la hora que seleccione el usuario
        final TextInputEditText show_hour = view.findViewById(R.id.et_hour);
        show_hour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (til_hour.getError() != null) {
                    // Una vez el usuario escriba, se borrará el error
                    til_hour.setError(null);
                }
            }
        });

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
                            // Si la hora es menor de 10 no pone el 0, por lo tanto, lo pongo en este if
                            hr = 0 + "" + hourOfDay;
                        }
                        String min = minute+"";
                        if (minute < 10) {
                            // Si el minuto es menor de 10 no pone el 0, por lo tanto, lo pongo en este if
                            min = 0 + "" + minute;
                        }
                        // Guardamos la hora en la variable y la mostramos
                        String hour = hr + ":" + min;
                        show_hour.setText(hour);
                    }
                }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);
                timePicker.show();
            }
        });

        ImageView choose_file = view.findViewById(R.id.iv_upload_file);
        choose_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mimeType -> "*/*"
                openFileExplorer("*/*");
            }
        });

        // TextInputEditText donde se verá el nombre que eliga el usuario
        final TextInputEditText et_taskName = view.findViewById(R.id.et_taskName);
        et_taskName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (til_taskName.getError() != null) {
                    // Una vez el usuario escriba, se borrará el error
                    til_taskName.setError(null);
                }
            }
        });

        // Botón para añadir una tarea
        Button add_task = view.findViewById(R.id.btn_add_task);
        add_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean err1 = false, err2 = false, err3 = false, err4 = false, err5 = false;
                String taskName = et_taskName.getText().toString();
                if (taskName.equals("")) {
                    // Nombre vacío
                    til_taskName.setError(getString(R.string.no_task_name));
                    err1 = true;
                }
                // Miramos que tipo de tarea ha elegido el usuario
                String type = spinner.getSelectedItem().toString();
                // Si el tipo de tarea que selecciona el usuario es "Entrega" o "Examen"
                if (type.equals(types[0]) || type.equals(types[2])) {
                    String date = show_date.getText().toString();
                    String hour = show_hour.getText().toString();
                    if (date.equals("")) {
                        til_date.setError(getString(R.string.select_date));
                        err2 = true;
                    } else {
                        try {
                            // Probamos si puede parsear la fecha
                            Date dt = sdf_date.parse(date);
                        } catch (ParseException e) {
                            // Formato de fecha incorrecto
                            til_date.setError(getString(R.string.wrong_format));
                            err3 = true;
                        }
                    }
                    if (hour.equals("")) {
                        til_hour.setError(getString(R.string.select_hour));
                        err4 = true;
                    } else {
                        try {
                            // Probamos si puede parsear la hora
                            Date hr = sdf_hour.parse(hour);
                        } catch (ParseException e) {
                            // Formato de hora incorrecto
                            til_hour.setError(getString(R.string.wrong_format));
                            err5 = true;
                        }
                    }
                    // Si no hay errores, haremos el insert de la tarea
                    if (!err1 && !err2 && !err3 && !err4 && !err5) {
                        String type_tag = getTaskTypeTAG(type, types);
                        // Miramos que unidad ha elegido y le damos el ID de la unidad correspondiente
                        int unit_id = unitsIDs.get(spinner2.getSelectedItemPosition());
                        // Creamos la tarea
                        UnitTask task = new UnitTask(taskName, unit_id, type_tag, date+" "+hour);
                        // Mostramos el ProgressDialog
                        showProgressDialog(getString(R.string.uploading_file));
                        // Llamamos al método que le pondrá el ID y después la insertará
                        getLastTaskID(task);
                    }
                }
                // Si el tipo de tarea que selecciona el usuario es "Documento para alumnos"
                else if (type.equals(types[1])) {
                    if (uri == null) {
                        Toast.makeText(getContext(), getString(R.string.no_file_selected), Toast.LENGTH_SHORT).show();
                    }
                    if (uri != null && !taskName.equals("")) {
                        String type_tag = getTaskTypeTAG(type, types);
                        // Miramos que unidad ha elegido y le damos el ID de la unidad correspondiente
                        int unit_id = unitsIDs.get(spinner2.getSelectedItemPosition());
                        // Creamos la tarea
                        UnitTask task = new UnitTask(taskName, unit_id, type_tag, null);
                        showProgressDialog(getString(R.string.creating_deliver));
                        // Subimos el archivo
                        uploadFile(uri, course_id, task);
                    }
                }
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

    /**
     * Sirve para obtener el último ID de tarea y sumarle uno para no repetirlo
     * @param unitTask - La tarea a añadir, para después llamar al método addTask()
     */
    private void getLastTaskID(final UnitTask unitTask) {
        db.collection(FirebaseStrings.COLLECTION_4)
                .orderBy(FirebaseStrings.FIELD1_C4, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int id = 1;
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                UnitTask unitTask = documentSnapshot.toObject(UnitTask.class);
                                // Le sumamos uno al último ID
                                id = unitTask.getTask_id() + 1;
                            }
                            unitTask.setTask_id(id);
                            addTask(unitTask);
                        } else {
                            // Cerramos el ProgressDialog
                            progressDialog.dismiss();
                            // Mostramos toast de error
                            Toast.makeText(getContext(), getString(R.string.error_adding_task), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", task.getException() + "");
                        }
                    }
                });

    }

    /**
     * Añade una tarea a Cloud Firestore
     * @param unitTask - La tarea a añadir
     */
    private void addTask(UnitTask unitTask) {
        db.collection(FirebaseStrings.COLLECTION_4)
                .add(unitTask)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        // Cerramos el ProgressDialog
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Mostramos Toast (La tarea se ha creado)
                            Toast.makeText(getContext(), getString(R.string.task_created), Toast.LENGTH_SHORT).show();
                            // Cerramos el DialogFragment
                            dismiss();
                            // IMPORTANTE! Para ver los cambios vuelve a Mis cursos y pulsa en la unidad seleccionada anteriormente
                        } else {
                            // Mostramos toast de error
                            Toast.makeText(getContext(), getString(R.string.error_adding_task), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", task.getException() + "");
                        }
                    }
                });
    }

    /**
     * Sirve para obtener el valor que se guardará en Cloud Firestore dependiendo del tipo de tarea elegido
     * @param type - El tipo de tarea elegido por el usuario
     * @param types - Array de strings con los tipos de tarea que se pueden elegir en el spinner
     * @return Devuelve el valor que se guardará en Cloud Firestore
     */
    private String getTaskTypeTAG(String type, String [] types) {
        String type_tag = null;
        // Tipo "Entrega"
        if (type.equals(types[0])) {
            type_tag = FirebaseStrings.TASK_TYPE1;
        }
        // Tipo "Documento para alumnos"
        if (type.equals(types[1])) {
            type_tag = FirebaseStrings.TASK_TYPE2;
        }
        // Tipo "Examen"
        if (type.equals(types[2])) {
            type_tag = FirebaseStrings.TASK_TYPE3;
        }
        return type_tag;
    }

    /**
     * Abre el explorador para seleccionar un fichero
     * @param mimeType - mimeType
     */
    private void openFileExplorer(String mimeType) {
        // Este intent no funciona en móviles Samsung
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Intent especial para el explorador de archivos de Samsung
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        sIntent.putExtra("CONTENT_TYPE", mimeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getActivity().getPackageManager().resolveActivity(sIntent, 0) != null){
            // Explorador de archivos de Samsung
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }
        try {
            // Abrimos el explorador de archivos
            startActivityForResult(chooserIntent, CHOOSE_FILE_REQUESTCODE);
        } catch (ActivityNotFoundException ex) {
            Log.e("ERROR", ex.getMessage());
            Toast.makeText(getContext(), getString(R.string.error_opening_file_manager), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Se activa cuando se cierra o se elige un archivo en el explorador de archivos
     * @param requestCode - El código de petición
     * @param resultCode - El código de resultado
     * @param data - Un intent con datos del archivo seleccionado (si ha elegido uno, si no el intent será null)
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            // Buscamos el nombre del fichero
            Cursor returnCursor =
                    getContext().getContentResolver().query(data.getData(), null, null, null, null);
            assert returnCursor != null;
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            returnCursor.close();
            // Lo mostramos en el TextView
            tv_fileName.setText(name);
            // Guardamos la uri
            uri = data.getData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Muestra un AlertDialog que emula a un ProgressDialog (la clase ProgressDialog está deprecated, por eso usamos este)
     * @param msg - El mensaje que se mostrará en el Dialog
     */
    private void showProgressDialog(String msg){
        // Creamos el AlertDialog y le aplicamos un style personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
        // Inflate de la vista
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.custom_progress_dialog, null);
        // Obtenemos el TextView de la vista para poder poner el texto
        TextView tv_message = view.findViewById(R.id.loading_msg);
        tv_message.setText(msg);
        // Ponemos la vista y lo hacemos no cancelable (para hacerlo modal)
        builder.setView(view)
                .setCancelable(false);
        progressDialog = builder.create();
        // Mostramos el Dialog
        progressDialog.show();
    }

    /**
     * Sube un archivo a Firebase Storage
     * @param uri - La uri del archivo que queremos subir
     * @param course_id - La ID del curso al que pertenece la unidad de la tarea
     * @param uTask - La tarea que después insertaremos en Cloud Firestore
     */
    private void uploadFile(Uri uri, int course_id, final UnitTask uTask) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        // Ruta donde se almacenará el fichero
        final StorageReference taskRef = storageRef.child("tasks/course_" + course_id + "/unit_" + uTask.getUnit_id() + "/" + tv_fileName.getText());
        UploadTask uploadTask = taskRef.putFile(uri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (!task.isSuccessful()) {
                    // Mostramos toast de error
                    Toast.makeText(getContext(), getString(R.string.error_uploading_file), Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", task.getException()+"");
                    // Cerramos el ProgressDialog
                    progressDialog.dismiss();
                }
                return taskRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    // Obtenemos la uri de donde se ha subido el fichero
                    Uri downloadUri = task.getResult();
                    String mUri = downloadUri.toString();
                    uTask.setFileURL(mUri);
                    getLastTaskID(uTask);
                } else {
                    // Cerramos el ProgressDialog
                    progressDialog.dismiss();
                    // Mostramos toast de error
                    Toast.makeText(getContext(), getString(R.string.error_uploading_file), Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", task.getException()+"");
                }
            }
        });
    }
}
