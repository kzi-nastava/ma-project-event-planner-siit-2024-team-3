package com.example.eveant.service;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends ArrayAdapter<Service> {
    private ArrayList<Service> aService;
    private Activity activity;
    private FragmentManager fragmentManager;

    public ServiceAdapter(Activity context, FragmentManager fragmentManager, ArrayList<Service> services){
        super(context, R.layout.card_view, services);
        aService = services;
        activity = context;
        fragmentManager = fragmentManager;
    }

    @Override
    public int getCount() {
        return aService.size();
    }

    @Nullable
    @Override
    public Service getItem(int position) {
        return aService.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
