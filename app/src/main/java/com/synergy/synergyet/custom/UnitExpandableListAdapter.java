package com.synergy.synergyet.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.synergy.synergyet.R;
import com.synergy.synergyet.model.UnitTask;
import com.synergy.synergyet.strings.FirebaseStrings;

import java.util.List;
import java.util.Map;

public class UnitExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> expandableListTitle;
    private Map<String, List<UnitTask>> expandableListDetail;
    private String userType;

    private Drawable expand_more;
    private Drawable expand_less;

    public UnitExpandableListAdapter(Context context, List<String> expandableListTitle,
                                         Map<String, List<UnitTask>> expandableListDetail, String userType) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
        this.userType = userType;
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
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
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
        // Si es una entrega y el usuario es un alumno
        if (type.equals(FirebaseStrings.TASK_TYPE1) && userType.equals(FirebaseStrings.DEFAULT_USER_TYPE)) {
            // Pondremos un icono de subida de archivo
            actionIcon.setImageResource(R.drawable.ic_file_upload_gray_24dp);
            actionIcon.setContentDescription(convertView.getContext().getString(R.string.upload_task));
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