package com.example.eveant.priceList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceListFragment extends Fragment {
    private String providerUsername = "faks1543@gmail.com"; /*TODO da uzme korisnika a ne staticko*/
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){


        View view = inflater.inflate(R.layout.fragment_price_list,container,false);

        RecyclerView recyclerView=view.findViewById(R.id.priceListItem);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<PriceListItem> priceList = new ArrayList<>();
        PriceListAdapter adapter=new PriceListAdapter(priceList,this);
        recyclerView.setAdapter(adapter);

        Button exportPdfButton = view.findViewById(R.id.export_pdf_button);
        exportPdfButton.setOnClickListener(v -> downloadPdf(providerUsername));



        RetrofitClient.offerService.getPriceList(providerUsername).enqueue(new Callback<List<PriceListItem>>() {
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

    private void downloadPdf(String providerUsername) {
        RetrofitClient.offerService.downloadPriceListPdf(providerUsername).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean saved = savePdfToDownloads(response.body().byteStream());
                    if (saved) {
                        Toast.makeText(getContext(), "PDF saved !", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Saving failed PDF-a", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean savePdfToDownloads(InputStream inputStream) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "PriceList.pdf");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}