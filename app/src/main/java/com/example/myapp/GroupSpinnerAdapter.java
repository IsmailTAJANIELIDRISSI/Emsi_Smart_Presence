package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class GroupSpinnerAdapter extends ArrayAdapter<Group> {
    public GroupSpinnerAdapter(Context context, List<Group> groups) {
        super(context, R.layout.spinner_item_group, groups);
        setDropDownViewResource(R.layout.spinner_dropdown_item_group);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.spinner_item_group);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.spinner_dropdown_item_group);
    }

    private View createView(int position, View convertView, ViewGroup parent, int layoutResource) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutResource, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.tv_group_name);
        Group group = getItem(position);
        if (group != null) {
            textView.setText(group.groupId);
        }

        return convertView;
    }
}