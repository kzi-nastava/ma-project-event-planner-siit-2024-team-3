package com.example.eveant.priceList;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceListFragment extends Fragment {
    private int providerId=1;
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_price_list,container,false);

        RecyclerView recyclerView=view.findViewById(R.id.priceListItem);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<PriceListItem> priceList = new ArrayList<>();
        PriceListAdapter adapter=new PriceListAdapter(priceList,this);
        recyclerView.setAdapter(adapter);


        RetrofitClient.offerService.getPriceList(providerId).enqueue(new Callback<List<PriceListItem>>() {
            @Override
            public void onResponse(Call<List<PriceListItem>> call, Response<List<PriceListItem>> response) {
                if(response.isSuccessful()&&response.body()!=null){
                    priceList.clear();
                    priceList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                }else{
                    Toast.makeText(getContext(), "greska kod price lst: " , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PriceListItem>> call, Throwable t) {
                Toast.makeText(getContext(), "greska kod price lst: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}