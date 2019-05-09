package com.synergy.synergyet.custom;

import android.content.Context;
import android.widget.BaseExpandableListAdapter;

import java.util.List;

public class InscribeExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> expandableListTitle;
    /*
    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        //return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition);
    }*/

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }
}
