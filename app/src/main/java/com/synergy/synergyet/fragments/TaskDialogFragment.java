package com.synergy.synergyet.fragments;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.synergy.synergyet.R;
import com.synergy.synergyet.model.TaskDelivered;
import com.synergy.synergyet.model.UnitTask;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TaskDialogFragment extends DialogFragment {
    public static final String TAG = "TaskDialogFragment";
    public final int CHOOSE_FILE_REQUESTCODE = 100;
    private UnitTask taskData;
    private String unitTitle;
    private int course_id;
    private String nameUser;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private TaskDelivered taskDelivered;
    private TextView tv_status;
    private TextView tv_dateDelivered;
    private TextView tv_fileDelivered;
    private Button btn_deliver;
    private ImageView uploadFile;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private boolean canBeDelivered = false;
    private Dialog progressDialog;
    private Uri uri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
        // Obtenemos los argumentos del bundle
        Bundle b = getArguments();
        taskData = (UnitTask) b.getSerializable(IntentExtras.EXTRA_TASK_DATA);
        unitTitle = b.getString(IntentExtras.EXTRA_UNIT_NAME);
        course_id = b.getInt(IntentExtras.EXTRA_COURSE_ID);
        nameUser = b.getString(IntentExtras.EXTRA_NAME_USER);
        // Instancia de Firebase Firestore
        db = FirebaseFirestore.getInstance();
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
        taskTitle.setText(taskData.getTaskName());
        // Ponemos la fecha límite
        TextView tv_deadLine = view.findViewById(R.id.deadLine);
        tv_deadLine.setText(taskData.getDead_line());

        btn_deliver = view.findViewById(R.id.btn_deliver);
        uploadFile = view.findViewById(R.id.iv_uploadFile);

        try {
            // Fecha de entrega
            Date deadLine = sdf.parse(taskData.getDead_line());
            // Fecha de hoy
            Date now = Calendar.getInstance().getTime();
            if (deadLine.compareTo(now) > 0) {
                // Todavía se puede entregar (la fecha de entrega es posterior a la de ahora)
                canBeDelivered = true;
            } else {
                // Ya no se puede entregar la tarea
                canBeDelivered = false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // TextViews donde se mostrará información de la tarea entrega (en el caso de que se haya entregado)
        tv_status = view.findViewById(R.id.taskStatus);
        tv_dateDelivered = view.findViewById(R.id.dateDelivered);
        tv_fileDelivered = view.findViewById(R.id.fileDelivered);
        // Obtenemos el usuario actual para después obtener su UID
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            // Obtenemos la tarea que hayamos entregado
            getTaskDelivered(firebaseUser.getUid(), taskData.getTask_id());
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

    /**
     * Obtiene la tarea entregada por el usuario (si la ha entregado).
     * Si se puede entregar la tarea y el usuario no ha entregado la tarea,
     * se mostrará el botón de entrega y de subida de ficheros para que la
     * pueda entregar.
     * @param UID - El UID del usuario actual, para filtrar por este campo
     * @param task_id - El ID de la tarea, para también filtrar por este campo
     */
    private void getTaskDelivered(String UID, int task_id) {
        db.collection(FirebaseStrings.COLLECTION_5)
                .whereEqualTo(FirebaseStrings.FIELD2_C5, UID)
                .whereEqualTo(FirebaseStrings.FIELD5_C5, task_id)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                taskDelivered = documentSnapshot.toObject(TaskDelivered.class);
                            }
                            if (taskDelivered != null) {
                                // Obtenemos la nota
                                double grade = taskDelivered.getGrade();
                                if (grade >= 0) {
                                    String str_grade = grade + " / 10";
                                    tv_status.setText(str_grade);
                                } else {
                                    tv_status.setText(getString(R.string.pending_qualification));
                                }
                                // Obtenemos la fecha de entrega
                                String dateDeliv = taskDelivered.getDate_delivered();
                                if (dateDeliv != null) {
                                    tv_dateDelivered.setText(dateDeliv);
                                }
                                // Nombre del fichero entregado
                                String filename = taskDelivered.getFile_name();
                                if (filename != null) {
                                    tv_fileDelivered.setText(filename);
                                }
                            } else {
                                tv_status.setText("-");
                                tv_dateDelivered.setText("-");
                                tv_fileDelivered.setText("-");
                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.error_loading_taskDelivered), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", task.getException()+"");
                        }
                        // Si todavia se puede entregar y no ha entregado nada
                        if (canBeDelivered && taskDelivered == null) {
                            // Mostramos el ImageView de subir archivo
                            uploadFile.setVisibility(ImageView.VISIBLE);
                            // Mostramos el botón de entrega
                            btn_deliver.setVisibility(Button.VISIBLE);
                            // Listeners de subir archivo y el botón de entrega
                            uploadFile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openFileExplorer("*/*");
                                }
                            });
                            btn_deliver.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (uri != null) {
                                        // Mostramos ProgressDialog
                                        showProgressDialog(getString(R.string.delivering_onProgress));
                                        // Fecha actual (será la fecha de entrega)
                                        Date now = Calendar.getInstance().getTime();
                                        String date = sdf.format(now);
                                        // Llamamos al método para subir el archivo del usuario
                                        TaskDelivered delivered = new TaskDelivered(firebaseUser.getUid(), course_id, taskData.getUnit_id(), taskData.getTask_id(), -1, taskData.getType(), date, tv_fileDelivered.getText().toString(), nameUser);
                                        uploadFileDelivered(delivered);
                                    } else {
                                        Toast.makeText(getContext(), getString(R.string.no_file_selected), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
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
            tv_fileDelivered.setText(name);
            // Guardamos la uri
            uri = data.getData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Sube el archivo que entrega el usuario a Cloud Storage
     * @param delivered - La tarea que entregará el usuario (para después insertarlo)
     */
    private void uploadFileDelivered(final TaskDelivered delivered) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        // Ruta donde se almacenará el fichero
        final StorageReference taskRef = storageRef.child("delivers/course_" + delivered.getCourse_id() + "/unit_" + delivered.getUnit_id() + "/task_" + delivered.getTask_id()
                + "/" + tv_fileDelivered.getText());
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
                    // Asignamos la uri al objeto
                    delivered.setUrl_file_delivered(mUri);
                    // Llamamos al método para que le ponga un ID
                    getLastTaskDeliveredID(delivered);
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

    /**
     * Sirve para obtener el último ID de tarea entregada y sumarle uno para no repetirlo
     * @param delivered - La tarea que entrega el usuario (para después llamar al método que la insertará)
     */
    private void getLastTaskDeliveredID(final TaskDelivered delivered) {
        db.collection(FirebaseStrings.COLLECTION_5)
                .orderBy(FirebaseStrings.FIELD1_C5, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int id = 1;
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                TaskDelivered td = documentSnapshot.toObject(TaskDelivered.class);
                                // Le sumamos uno al último ID
                                id = td.getDeliver_id() + 1;
                            }
                            delivered.setDeliver_id(id);
                            addTaskDelivered(delivered);
                        } else {
                            // Cerramos el ProgressDialog
                            progressDialog.dismiss();
                            // Mostramos toast de error
                            Toast.makeText(getContext(), getString(R.string.error_delivering_task), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", task.getException() + "");
                        }
                    }
                });
    }

    /**
     * Añade la tarea entregada por el usuario a Cloud Firestore
     * @param delivered - La tarea que entrega el usuario
     */
    private void addTaskDelivered(TaskDelivered delivered) {
        db.collection(FirebaseStrings.COLLECTION_5)
                .add(delivered)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        // Cerramos el ProgressDialog
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Mostramos Toast (La tarea se ha creado)
                            Toast.makeText(getContext(), getString(R.string.task_delivered_successfully), Toast.LENGTH_SHORT).show();
                            // Cerramos el DialogFragment
                            dismiss();
                        } else {
                            // Mostramos toast de error
                            Toast.makeText(getContext(), getString(R.string.error_delivering_task), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", task.getException() + "");
                        }
                    }
                });
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
}
