package com.synergy.synergyet.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.synergy.synergyet.R;
import com.synergy.synergyet.model.TaskDelivered;
import com.synergy.synergyet.strings.FirebaseStrings;

import java.io.File;
import java.util.List;

public class CorrectListAdapter extends ArrayAdapter<TaskDelivered> {
    private List<TaskDelivered> tasksDelivered;
    private Context context;
    private FirebaseFirestore db;
    private Dialog progressDialog;

    public CorrectListAdapter(Context context, List<TaskDelivered> tasksDelivered) {
        // Le añadimos el contexto, el layout que utilizará y la lista de datos
        super(context, R.layout.correct_item, tasksDelivered);
        this.context = context;
        this.tasksDelivered = tasksDelivered;
        // Instancia de FirebaseFirestore
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Obtenemos los datos de la posición actual
        final TaskDelivered taskDelivered = getItem(position);
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.correct_item, parent, false);
            holder.tv_student_name = convertView.findViewById(R.id.student_name);
            holder.tv_file_name = convertView.findViewById(R.id.tv_file_name);
            holder.tv_grade = convertView.findViewById(R.id.tv_grade);
            holder.correct_task = convertView.findViewById(R.id.iv_correct_task);
            holder.download_file = convertView.findViewById(R.id.iv_download_file);
            // Guardamos la vista en la memoria caché
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // Ponemos los datos en los TextViews correspondientes
        holder.tv_student_name.setText(taskDelivered.getDelivered_by());
        holder.tv_file_name.setText(taskDelivered.getFile_name());
        if (taskDelivered.getGrade() >= 0) {
            String strGrade = taskDelivered.getGrade()+"";
            holder.tv_grade.setText(strGrade);
        }
        // Listener corregir tarea
        holder.correct_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creamos el InputDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
                LayoutInflater inflater = LayoutInflater.from(v.getContext());
                // Le ponemos la vista personalizada
                View dialogView = inflater.inflate(R.layout.grade_input_dialog, null);
                builder.setCancelable(false);
                builder.setView(dialogView);

                final TextInputLayout til = dialogView.findViewById(R.id.text_input_layout);
                // Ponemos un contorno al TextInputLayout tipo Google (ejemplo -> https://i.stack.imgur.com/t2stI.png)
                til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                til.setBoxCornerRadii(5, 5, 5, 5);

                // Obtenemos el TextInputEditText del InputDialog (para poder obtener el texto que introduce el usuario)
                final TextInputEditText et_grade = dialogView.findViewById(R.id.input_grade);
                et_grade.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (til.getError() != null) {
                            til.setError(null);
                        }
                    }
                });
                // Creamos el listener del botón Aceptar vacío (más adelante lo sobreescribiremos)
                builder.setPositiveButton(getContext().getString(R.string.dialogOK_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                // Listener del botón Cancelar
                builder.setNegativeButton(getContext().getString(R.string.dialogCancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Si pulsa Cancelar se cerrará el Dialog
                        dialog.cancel();
                    }
                });

                final AlertDialog dialog = builder.create();
                dialog.show();
                // Sobreescribimos el listener del botón Aceptar (si lo hacemos de esta manera evitamos que se cierre el Dialog al pulsar 'Aceptar')
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Obtenemos la contraseña que ha introducido el usuario
                        String grade = et_grade.getText().toString();
                        if (grade.equals("")) {
                            // Mostramos mensaje de error (no ha escrito contraseña)
                            til.setError(getContext().getString(R.string.no_grade));
                        } else {
                            try {
                                double num = Double.parseDouble(grade);
                                if (num >= 0 && num <= 10) {
                                    showProgressDialog(context.getString(R.string.updating_grade));
                                    updateTaskGrade(taskDelivered.getStudent_uid(), taskDelivered.getTask_id(), num, holder.tv_grade);
                                    dialog.dismiss();
                                } else {
                                    // Mostramos mensaje de error (nota incorrecta)
                                    til.setError(getContext().getString(R.string.invalid_grade));
                                }
                            } catch (NumberFormatException e) {
                                // Mostramos mensaje de error (nota incorrecta)
                                til.setError(getContext().getString(R.string.invalid_grade));
                            }
                        }
                    }
                });
            }
        });
        // Listener descargar tarea
        holder.download_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Mostramos toast de comienzo de descarga
                Toast.makeText(context, context.getString(R.string.download_started), Toast.LENGTH_SHORT).show();
                // Para descargar un archivo de Cloud Storage
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(taskDelivered.getUrl_file_delivered());
                // Nombre del archivo que se descargará
                String filename = storageRef.getName();
                // El archivo se guardará en la carpeta de descargas
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File localFile = new File(downloadsDir.getPath()+"/", filename);
                storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // El archivo se ha descargado correctamente, mostramos Snackbar
                        Snackbar.make(v, context.getString(R.string.download_finished), Snackbar.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Mostramos Toast de error
                        Toast.makeText(context, context.getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        // Devolvemos la vista con los cambios realizados para mostrarla en la pantalla
        return convertView;
    }

    private class ViewHolder {
        TextView tv_student_name;
        TextView tv_file_name;
        TextView tv_grade;
        ImageView correct_task;
        ImageView download_file;
    }

    /**
     * Muestra un AlertDialog que emula a un ProgressDialog (la clase ProgressDialog está deprecated, por eso usamos este)
     * @param msg - El mensaje que se mostrará en el Dialog
     */
    private void showProgressDialog(String msg){
        // Creamos el AlertDialog y le aplicamos un style personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        // Inflate de la vista
        View view = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog, null);
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
     * Actualiza la nota de una tarea entregada. Primero realiza una consulta para obtener
     * el documento y después actualiza la nota
     * @param UID - El UID del usuario que entregó la tarea
     * @param task_id - El ID de la tarea
     * @param grade - La nueva nota para actualizar
     * @param tv_grade - El TextView donde se mostrará la nota actualizada
     */
    private void updateTaskGrade(String UID, int task_id, final double grade, final TextView tv_grade){
        // Primero realizamos una consulta para obtener el documento de la tarea entregada
        db.collection(FirebaseStrings.COLLECTION_5)
                .whereEqualTo(FirebaseStrings.FIELD2_C5, UID)
                .whereEqualTo(FirebaseStrings.FIELD5_C5, task_id)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : snapshots) {
                            // Obtenemos el ID del documento
                            String docID = documentSnapshot.getId();
                            // Hacemos un update para actualizar la nota
                            DocumentReference docRef = db.collection(FirebaseStrings.COLLECTION_5).document(docID);
                            docRef.update(FirebaseStrings.FIELD6_C5, grade)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // Cerramos ProgressDialog
                                            progressDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                // Mostramos la nota en el TextView
                                                tv_grade.setText(String.valueOf(grade));
                                                // Mostramos Toast de nota actualizada
                                                Toast.makeText(context, context.getString(R.string.toast_updated_grade), Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Mostramos Toast de error
                                                Toast.makeText(context, context.getString(R.string.toast_error_updating_grade), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Cerramos ProgressDialog
                        progressDialog.dismiss();
                        // Mostramos Toast de error
                        Toast.makeText(context, context.getString(R.string.toast_error_updating_grade), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
