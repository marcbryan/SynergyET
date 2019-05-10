package com.synergy.synergyet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;

import com.synergy.synergyet.custom.InscribeExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InscribeCourseActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ExpandableListView expandableListView;
    private InscribeExpandableListAdapter expandableListAdapter;
    private HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();
    private List<String> expandableListTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscribe_course);
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

        expandableListView = findViewById(R.id.expandable_list);
        expandableListDetail = getData();
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());

        expandableListAdapter = new InscribeExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {

            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return false;
            }
        });

    }

    public HashMap<String, List<String>> getData() {
        List<String> cfgs = new ArrayList<>();
        cfgs.add("Informática");
        expandableListDetail.put("Ciclos Formativos (Grado Superior)", cfgs);
        return expandableListDetail;
    }

}
