package com.synergy.synergyet.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.synergy.synergyet.R;
import com.synergy.synergyet.model.Course;

import java.util.ArrayList;

public class CoursesListAdapater extends ArrayAdapter<Course> implements View.OnClickListener {
    private TextView course_name;
    private TextView teacher_name;

    private ArrayList<Course> courses_list;
    Context context;

    public CoursesListAdapater(ArrayList<Course> data, Context context) {
        super(context, R.layout.course_item, data);
        this.courses_list = data;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        Course course = (Course) object;
        //TODO: Abrir Activity del curso
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Obtener los datos del objeto Course de esta posición
        Course course = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.course_item, parent, false);
            // Buscar los componentes para asignarle los datos
            course_name = convertView.findViewById(R.id.title);
            teacher_name = convertView.findViewById(R.id.subtitle);
        }
        // Asignar los datos a los componentes
        course_name.setText(course.getName());
        //TODO: Testeo
        teacher_name.setText("Profesor: ");
        // Devuelve la vista completada para mostrarla en la pantalla
        return convertView;
    }
}
