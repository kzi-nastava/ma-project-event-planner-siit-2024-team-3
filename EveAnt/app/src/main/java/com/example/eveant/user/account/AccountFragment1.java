package com.example.eveant.user.account;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eveant.R;

public class AccountFragment1 extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account1, container, false);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new AccountFragment2());
        transaction.commit();

        return rootView;
    }
}