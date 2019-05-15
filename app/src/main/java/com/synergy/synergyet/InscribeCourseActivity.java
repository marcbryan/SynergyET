package com.synergy.synergyet;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
    private AlertDialog.Builder builder;

    private String ok_text;
    private String cancel_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscribe_course);
        Intent intent = getIntent();
        String category = intent.getStringExtra(IntentExtras.EXTRA_CATEGORY_NAME);
        String group = intent.getStringExtra(IntentExtras.EXTRA_GROUP_NAME);
        //TODO: Borrar toast de pruebas
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
        // Mostramos la categoria y el grupo al que pertenece (el que seleccionó el usuario en el activity anterior)
        TextView tv_category = findViewById(R.id.infoBar_title);
        String txt = group+" > "+category;
        tv_category.setText(txt);

        // Obtenemos los textos 'Aceptar' y 'Cancelar' de strings.xml (para ponerlo en el Dialog)
        ok_text = getString(R.string.dialogOK_button);
        cancel_text = getString(R.string.dialogCancel);

        // Buscamos el ListView
        listView = findViewById(R.id.courses_list);
        // Añadimos un listener (al pulsar un elemento del ListView)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course course = (Course) listView.getItemAtPosition(position);
                // Creamos el InputDialog
                builder = new AlertDialog.Builder(InscribeCourseActivity.this, R.style.CustomAlertDialog);
                LayoutInflater inflater = getLayoutInflater();
                // Le ponemos la vista personalizada
                View dialogView = inflater.inflate(R.layout.input_dialog, null);
                builder.setCancelable(false);
                builder.setView(dialogView);
                // Obtenemos el TextInputEditText del InputDialog (para poder obtener el texto que introduce el usuario)
                final TextInputEditText et_pass = dialogView.findViewById(R.id.input_password);
                final TextInputLayout til = dialogView.findViewById(R.id.text_input_layout);
                // Creamos el listener del botón Aceptar vacío (más adelante lo sobreescribiremos)
                builder.setPositiveButton(ok_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                // Listener del botón Cancelar
                builder.setNegativeButton(cancel_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Si pulsa Cancelar se cerrará el Dialog
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                // Sobreescribimos el listener del botón Aceptar (si lo hacemos de esta manera evitamos que se cierre el Dialog al pulsar 'Aceptar')
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Obtenemos la contraseña que ha introducido el usuario
                        String pwd = et_pass.getText().toString();
                        if (pwd.equals("")) {
                            // Mostramos mensaje de error
                            til.setError(getString(R.string.dialog3_error_msg1));
                        } else {
                            //TODO: Comprobar contraseña del curso
                        }
                    }
                });
            }
        });
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
