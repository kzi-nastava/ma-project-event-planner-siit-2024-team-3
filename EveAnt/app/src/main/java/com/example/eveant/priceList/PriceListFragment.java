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

        Button exportPdfButton = view.findViewById(R.id.export_pdf_button);
        exportPdfButton.setOnClickListener(v -> generatePDF(priceList));



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

    private void generatePDF(ArrayList<PriceListItem> priceList) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 veličina
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        int x = 10, y = 25;
        paint.setTextSize(12);
        canvas.drawText("Cenovnik", x, y, paint);

        y += 20;
        for (PriceListItem item : priceList) {
            String line = "Naziv: " + item.getName() + ", Cena: " + item.getPrice() +
                    ", Popust: " + item.getDiscount() + ", Cena sa popustom: " + item.getPriceWithDiscount();
            canvas.drawText(line, x, y, paint);
            y += 20;
        }

        pdfDocument.finishPage(page);

        // Sačuvaj PDF u uređaj
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Cenovnik.pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(getContext(), "PDF je sačuvan u Downloads folderu!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Greška prilikom pravljenja PDF-a", Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }
}