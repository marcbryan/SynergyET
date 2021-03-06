package com.synergy.synergyet;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.synergy.synergyet.custom.CategoryExpandableListAdapter;
import com.synergy.synergyet.strings.FirebaseStrings;
import com.synergy.synergyet.strings.IntentExtras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {
    private HashMap<String, List<String>> expandableListDetail = new HashMap<>();
    private List<String> expandableListTitle;

    private String group1;
    private String group2;
    private String group3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        // Obtenemos el toolbar y lo añadimos al activity (para que se vean los iconos)
        Toolbar toolbar = findViewById(R.id.toolbar);
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

        ExpandableListView expandableListView = findViewById(R.id.expandable_list);
        expandableListDetail = getData();
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        // Creamos el adapter para el ExpandableListView
        CategoryExpandableListAdapter expandableListAdapter = new CategoryExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        // Ponemos el adapter en el ExpandableListView
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                // Solo podremos inscribirnos a cursos de Informática, los otros no estan disponibles
                // Al hacer click en el elemento "Informática" (dentro de Ciclos Formativos), se abre un Activity con los cursos de esa categoria
                if (expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition).equals(FirebaseStrings.IT_CATEGORY)) {
                    String group_name = expandableListTitle.get(groupPosition);
                    String category_name = expandableListDetail.get(group_name).get(childPosition);
                    Intent intent = new Intent(v.getContext(), InscribeCourseActivity.class);
                    intent.putExtra(IntentExtras.EXTRA_GROUP_NAME, group_name);
                    intent.putExtra(IntentExtras.EXTRA_CATEGORY_NAME, category_name);
                    startActivity(intent);
                } else {
                    // Mostramos Toast diciendo que no está disponible
                    Toast.makeText(v.getContext(), getString(R.string.course_not_available), Toast.LENGTH_SHORT).show();
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
