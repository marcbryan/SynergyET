package com.synergy.synergyet;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.synergy.synergyet.custom.CoursesListAdapater;
import com.synergy.synergyet.model.Course;
import com.synergy.synergyet.strings.IntentExtras;

import java.util.ArrayList;

public class InscribeCourseActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ListView listView;
    private ArrayList<Course> courses;
    private CoursesListAdapater adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscribe_course);
        Intent intent = getIntent();
        String category = intent.getStringExtra(IntentExtras.EXTRA_CATEGORY_NAME);
        String group = intent.getStringExtra(IntentExtras.EXTRA_GROUP_NAME);
        Toast.makeText(getApplicationContext(), "Categoria: "+category, Toast.LENGTH_SHORT).show();

        // Obtenemos el toolbar y lo añadimos al activity (para que se vean los iconos)
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Mostrar la flecha para volver atrás
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Listener para la flecha para volver atrás
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView tv_category = findViewById(R.id.infoBar_title);
        String txt = group+" > "+category;
        tv_category.setText(txt);

        // Bucamos el ListView
        listView = findViewById(R.id.courses_list);
        // Creamos el array que tendrá los datos del ListView
        courses = new ArrayList<>();
        //TODO: Cambiar datos hardcodeados por datos de consulta
        courses.add(new Course(1, "AMS2 M6 - Acceso a datos (1)", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(2, "AMS2 M7 - Interficies (1)", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));
        courses.add(new Course(3, "AMS2 M6 - Acceso a datos (2)", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(4, "AMS2 M7 - Interficies (2)", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));
        courses.add(new Course(5, "AMS2 M6 - Acceso a datos (3)", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(6, "AMS2 M7 - Interficies (3)", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));
        courses.add(new Course(7, "AMS2 M6 - Acceso a datos (4)", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(8, "AMS2 M7 - Interficies (4)", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));
        courses.add(new Course(9, "AMS2 M6 - Acceso a datos (5)", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(10, "AMS2 M7 - Interficies (5)", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));
        courses.add(new Course(11, "AMS2 M6 - Acceso a datos (6)", "Informática", "CFGS", "99", "pwd", "2018-2019", false));
        courses.add(new Course(12, "AMS2 M7 - Interficies (6)", "Informática", "CFGS", "99", "pwd2", "2018-2019", false));

        // Creamos el adapter
        adapter = new CoursesListAdapater(courses, this);
        // Se lo asignamos al ListView
        listView.setAdapter(adapter);
        // Activamos la filtración de datos para poder hacer búsquedas
        listView.setTextFilterEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_course_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.searchCourse));
        // Añadimos el listener de búsquedas mientras el usuario escribe texto en el input
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Para que el adapter filtre según el texto escrito
                adapter.getFilter().filter(s);
                return true;
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

}
