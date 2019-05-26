package com.synergy.synergyet.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.synergy.synergyet.R;
import com.synergy.synergyet.model.UnitTask;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.io.File;
import java.util.List;
import java.util.Map;

public class UnitExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> expandableListTitle;
    private Map<String, List<UnitTask>> expandableListDetail;
    private String nameUser;
    private String userType;
    private int course_id;

    private Drawable expand_more;
    private Drawable expand_less;

    public UnitExpandableListAdapter(Context context, List<String> expandableListTitle,
                                         Map<String, List<UnitTask>> expandableListDetail, String nameUser, String userType, int course_id) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
        this.nameUser = nameUser;
        this.userType = userType;
        this.course_id = course_id;
        // Las imagenes de las flechas del ExpandableListView
        expand_less = ContextCompat.getDrawable(context, R.drawable.ic_expand_less_gray_24dp);
        expand_more = ContextCompat.getDrawable(context, R.drawable.ic_expand_more_gray_24dp);
    }

    @Override
    public UnitTask getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(final int listPosition, final int expandedListPosition,
                             final boolean isLastChild, View convertView, final ViewGroup parent) {
        final String expandedListText = getChild(listPosition, expandedListPosition).getTaskName();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expandable_item, null);
        }
        TextView expandedListTextView = convertView.findViewById(R.id.itemTitle);
        expandedListTextView.setText(expandedListText);
        ImageView actionIcon = convertView.findViewById(R.id.actionIcon);
        String type = getChild(listPosition, expandedListPosition).getType();
        // Si la tarea es una entrega o un examen y el usuario es un alumno
        if ((type.equals(FirebaseStrings.TASK_TYPE1) || type.equals(FirebaseStrings.TASK_TYPE3)) && userType.equals(FirebaseStrings.DEFAULT_USER_TYPE)) {
            // Obtenemos la tarea
            final UnitTask task = getChild(listPosition, expandedListPosition);
            // Pondremos un icono de subida de archivo
            actionIcon.setImageResource(R.drawable.ic_file_upload_gray_24dp);
            actionIcon.setContentDescription(convertView.getContext().getString(R.string.upload_file));
            actionIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TaskDialogFragment dialog = new TaskDialogFragment();
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    Bundle b = new Bundle();
                    // Le pasamos el nombre de la unidad (UF)
                    b.putString(IntentExtras.EXTRA_UNIT_NAME, getGroup(listPosition).toString());
                    // Ponemos la tarea en el bundle
                    b.putSerializable(IntentExtras.EXTRA_TASK_DATA, task);
                    // Ponemos el ID del curso
                    b.putInt(IntentExtras.EXTRA_COURSE_ID, course_id);
                    // Le pasamos el nombre del usuario
                    b.putString(IntentExtras.EXTRA_NAME_USER, nameUser);
                    // Se lo pasamos al DialogFragment
                    dialog.setArguments(b);
                    // Lo mostramos
                    dialog.show(ft, TaskDialogFragment.TAG);
                }
            });
        }
        // Si la tarea es un documento y el usuario es un alumno
        else if (type.equals(FirebaseStrings.TASK_TYPE2) && userType.equals(FirebaseStrings.DEFAULT_USER_TYPE)) {
            // Obtenemos la tarea
            final UnitTask task = getChild(listPosition, expandedListPosition);
            // Pondremos un icono de descarga de archivo
            actionIcon.setImageResource(R.drawable.ic_file_download_gray_24dp);
            actionIcon.setContentDescription(convertView.getContext().getString(R.string.download_file));
            actionIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    // Mostramos toast de comienzo de descarga
                    Toast.makeText(context, context.getString(R.string.download_started), Toast.LENGTH_SHORT).show();
                    // Para descargar un archivo de Cloud Storage
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl(task.getFileURL());
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
        }
        // Si la tarea es una entrega o examen y el usuario es un profesor
        else if ((type.equals(FirebaseStrings.TASK_TYPE1) || type.equals(FirebaseStrings.TASK_TYPE3)) && userType.equals(FirebaseStrings.USER_TYPE_TEACHER)) {
            // Obtenemos la tarea
            final UnitTask task = getChild(listPosition, expandedListPosition);
            // Pondremos un icono para indicar que es una correción al ImageView
            actionIcon.setImageResource(R.drawable.ic_school_gray_24dp);
            actionIcon.setContentDescription(convertView.getContext().getString(R.string.description_correct));
            actionIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CorrectDialogFragment dialog = new CorrectDialogFragment();
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    Bundle b = new Bundle();
                    // Ponemos la tarea en el bundle
                    b.putSerializable(IntentExtras.EXTRA_TASK_DATA, task);
                    // Se lo pasamos al DialogFragment
                    dialog.setArguments(b);
                    // Lo mostramos
                    dialog.show(ft, CorrectDialogFragment.TAG);
                }
            });
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expandable_list, null);
        }
        TextView listTitleTextView = convertView.findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        ImageView indicator = convertView.findViewById(R.id.indicator);
        if (isExpanded) {
            // Cuando se está abriendo el ExpandableListView, cambiamos de imagen (la flecha hacia arriba)
            indicator.setImageDrawable(expand_less);
        } else {
            // Cuando se está cerrando el ExpandableListView, cambiamos de imagen (la flecha hacia abajo)
            indicator.setImageDrawable(expand_more);
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}