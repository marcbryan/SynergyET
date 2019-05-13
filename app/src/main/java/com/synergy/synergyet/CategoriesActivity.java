package com.synergy.synergyet;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.synergy.synergyet.custom.CategoryExpandableListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ExpandableListView expandableListView;
    private CategoryExpandableListAdapter expandableListAdapter;
    private HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();
    private List<String> expandableListTitle;

    private String group1;
    private String group2;
    private String group3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
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

        // Obtenemos los nombres de los grupos
        group1 = getString(R.string.group1);
        group2 = getString(R.string.group2);
        group3 = getString(R.string.group3);

        expandableListView = findViewById(R.id.expandable_list);
        expandableListDetail = getData();
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        // Creamos el adapter para el ExpandableListView
        expandableListAdapter = new CategoryExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        // Ponemos el adapter en el ExpandableListView
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //TODO: Pruebas, cambiar por implementación final
                // Al hacer click en el elemento "Informática" (dentro de Ciclos Formativos), abrir Activity con los cursos de esa categoria
                if (expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition).equals("Informática")) {
                    Toast.makeText(v.getContext(), "Informática -> Abrir activity", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(), expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

    }

    /**
     * Obtiene los arrays de strings de strings.xml y los coloca en un HashMap
     * @return Devuelve un HashMap con el nombre del grupo (ej. Ciclos Formativos) y el array de strings correspondiente al grupo
     */
    private HashMap<String, List<String>> getData() {
        Resources res = getResources();
        // Primero obtenemos el array de strings con los textos que necesitamos (están en strings.xml), después lo añadimos al HashMap(clave, valores)
        List<String> g1 = Arrays.asList(res.getStringArray(R.array.array_g1));
        expandableListDetail.put(group1, g1);
        List<String> g2 = Arrays.asList(res.getStringArray(R.array.array_g2));
        expandableListDetail.put(group2, g2);
        List<String> g3 = Arrays.asList(res.getStringArray(R.array.array_g3));
        expandableListDetail.put(group3, g3);
        return expandableListDetail;
    }

}
