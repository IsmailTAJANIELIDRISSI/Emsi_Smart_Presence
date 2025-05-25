package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SiteSpinnerAdapter extends ArrayAdapter<Site> {
    public SiteSpinnerAdapter(Context context, List<Site> sites) {
        super(context, R.layout.spinner_item_site, sites);
        setDropDownViewResource(R.layout.spinner_dropdown_item_site);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.spinner_item_site);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.spinner_dropdown_item_site);
    }

    private View createView(int position, View convertView, ViewGroup parent, int layoutResource) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutResource, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.tv_site_name);
        Site site = getItem(position);
        if (site != null) {
            textView.setText(site.siteId);
        }

        return convertView;
    }
}