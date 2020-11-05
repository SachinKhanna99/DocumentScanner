package com.example.myabc.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myabc.R;
import com.example.myabc.adapter.MergePdfAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MergePdf extends Fragment {

    private ArrayList<String> selectedPdfList = new ArrayList<>();
    private ArrayList<Uri> selectedPdfUriList = new ArrayList<>();
    private MergePdfAdapter myAdapter;
    private RecyclerView rvMergePdf;
    private Button btnMergePdf;
    private String dirPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_merge_pdf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMergePdf = view.findViewById(R.id.rvMergePdf);
        btnMergePdf = view.findViewById(R.id.btnMergePdf);

        setupRecyclerView();

        btnMergePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mergePDFs();
            }
        });

    }

    private void mergePDFs() {
        if(selectedPdfUriList != null && !selectedPdfUriList.isEmpty()){
            Toast.makeText(requireContext(), "Extracting Images..", Toast.LENGTH_SHORT).show();
            try {
                PdfDocument pdfDocument = new PdfDocument();

                for(int i = 0; i<selectedPdfUriList.size(); i++){

                    ParcelFileDescriptor parcelFileDescriptor = getContext().getContentResolver()
                            .openFileDescriptor(selectedPdfUriList.get(i), "r");

                    if(parcelFileDescriptor != null){
                        PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);

                        int pageCount = renderer.getPageCount();

                        for(int pageIndex = 0; pageIndex < pageCount ; pageIndex++) {

                            //GETTING IMAGE FROM PDF
                            PdfRenderer.Page page = renderer.openPage(pageIndex);
                            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);

                            Canvas canvas = new Canvas(bitmap);
                            canvas.drawColor(Color.WHITE);

                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                            //ADDING IMAGE TO PDF
                            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595,
                                    842, 1).create();
                            PdfDocument.Page pdfPage = pdfDocument.startPage(pageInfo);
                            Canvas pdfCanvas = pdfPage.getCanvas();

                            pdfCanvas.drawBitmap(bitmap, 0, 0, null);
                            pdfDocument.finishPage(pdfPage);

                            page.close();

                        }
                        renderer.close();
                    }
                }
                savePdfDocument(pdfDocument);
                pdfDocument.close();

                Toast.makeText(requireContext(), "Merged PDF saved at " + dirPath, Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(requireContext(), "Please select a PDF file first..", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePdfDocument(PdfDocument pdfDocument) {

        final String path = Environment.getExternalStorageDirectory().toString();
        dirPath = path + "/Scanner Document/Merge PDF/";
        final File directory = new File(dirPath);

        if(!directory.exists()){
            directory.mkdirs();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
        final String timestamp = simpleDateFormat.format( new Date() );

        String finalFilePath = directory + "/" + "Merge" + timestamp + ".pdf";

        File file = new File(finalFilePath);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        myAdapter = new MergePdfAdapter(new ArrayList<String>());

        rvMergePdf.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMergePdf.setAdapter(myAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.pdf_to_image_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.miSelectPdf) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, 0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == Activity.RESULT_OK && requestCode == 0) {
            if(selectedPdfList != null || !selectedPdfList.isEmpty()){
                selectedPdfList.clear();
            }

            if(selectedPdfUriList != null || !selectedPdfUriList.isEmpty()){
                selectedPdfUriList.clear();
            }

            if(intent.getClipData() != null) {
                int pdfCount = intent.getClipData().getItemCount();

                for(int i = 0; i < pdfCount; i++) {
                    Uri pdfUri = intent.getClipData().getItemAt(i).getUri();
                    selectedPdfUriList.add(pdfUri);
                    selectedPdfList.add(new File(pdfUri.getPath()).getName());

                }
                myAdapter.setList(selectedPdfList);
                myAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(requireContext(), "Please Select more than one PDFs..", Toast.LENGTH_LONG).show();
            }
    }
}
}