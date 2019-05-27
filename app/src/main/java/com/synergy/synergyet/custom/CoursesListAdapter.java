package com.synergy.synergyet.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.synergy.synergyet.R;
import com.synergy.synergyet.model.Course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CoursesListAdapter extends ArrayAdapter<Course> implements View.OnClickListener {
    // Array con todos los cursos que le pase el constructor
    private final ArrayList<Course> courses_list;
    // Array con los cursos filtrados (cuando se hacen búsquedas)
    private ArrayList<Course> filtered_courses;
    private Comparator<Course> comparator;
    private Context context;

    public CoursesListAdapter(ArrayList<Course> data, Context context) {
        super(context, R.layout.course_item, data);
        this.context = context;
        courses_list = data;
        filtered_courses = data;
        comparator = new Comparator<Course>() {
            @Override
            public int compare(Course c1, Course c2) {
                String s1 = c1.getName();
                String s2 = c2.getName();
                return s1.toLowerCase(Locale.getDefault()).compareTo(s2.toLowerCase());
            }
        };
        // Ordenamos los arrays
        Collections.sort(courses_list, comparator);
        Collections.sort(filtered_courses, comparator);
    }

    @Override
    public void onClick(View v) {}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Para evitar tener elementos duplicados en el ListView
        //convertView = null;
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.course_item, parent, false);
            // Buscar los componentes para asignarle los datos
            holder.course_name = convertView.findViewById(R.id.title);
            holder.teacher_name = convertView.findViewById(R.id.subtitle);
            // Guardamos la vista en la memoria caché
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // Obtener los datos del objeto Course de esta posición
        Course course = getItem(position);
        // Asignar los datos a los componentes
        holder.course_name.setText(course.getName());
        holder.teacher_name.setText("");
        if (course.getTeachers() != null) {
            String str_teacher = context.getString(R.string.teacher);
            String str;
            int size = course.getTeachers().size();
            for (int i=0; i < size; i++) {
                str = str_teacher;
                if (size == 1 || (i == course.getTeachers().size()-1)) {
                    str += " " + course.getTeachers().get(i);
                } else {
                    str += " " + course.getTeachers().get(i) + System.getProperty("line.separator");
                }
                str = holder.teacher_name.getText() + str;
                holder.teacher_name.setText(str);
            }
        }

        // Devuelve la vista completada para mostrarla en la pantalla
        return convertView;
    }

    private class ViewHolder {
        TextView course_name;
        TextView teacher_name;
    }


    @Override
    public int getCount() {
        return filtered_courses.size();
    }

    @Override
    public Course getItem(int position) {
        return filtered_courses.get(position);
    }

    @Override
    public int getPosition(Course item) {
        return filtered_courses.indexOf(item);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Course> filteredResults = new ArrayList<>();
                // Buscamos en cada elemento del array si contiene la búsqueda realizada
                for (Course course : courses_list) {
                    if (course.getName().toLowerCase(Locale.getDefault()).contains(constraint.toString().toLowerCase(Locale.getDefault()))) {
                        filteredResults.add(course);
                    }
                }
                // Los ordenamos
                Collections.sort(filteredResults, comparator);
                FilterResults results = new FilterResults();
                results.values = filteredResults;
                results.count = filteredResults.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // Si la búsqueda encuentra al menos un elemento, notificará al ListView de que los datos han cambiado para que los muestre
                filtered_courses = (ArrayList<Course>) results.values;
                if (results.count > 0){
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
