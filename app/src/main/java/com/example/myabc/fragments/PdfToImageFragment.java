package com.example.myabc.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myabc.Document;
import com.example.myabc.R;
import com.example.myabc.Util.FileIo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import kotlinx.coroutines.Dispatchers;


public class PdfToImageFragment extends Fragment {

    private Uri selectedPdfUri;
    private String selectedPdfName;
    private TextView tvPdfName;
    private ImageView ivPdfSymbol;
    private Button btnConvertToImages;
    private String dirPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_pdf_to_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvPdfName = view.findViewById(R.id.tvPdfName);
        ivPdfSymbol = view.findViewById(R.id.ivPdfSymbol);
        btnConvertToImages = view.findViewById(R.id.btnConvertToImages);

        btnConvertToImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convertPdfToImages();
            }
        });
    }

    private void convertPdfToImages() {
        if(selectedPdfUri != null){
            Toast.makeText(requireContext(), "Extracting Images..", Toast.LENGTH_SHORT).show();
            try {

                String filePath = selectedPdfUri.getPath();
                ParcelFileDescriptor parcelFileDescriptor = getContext().getContentResolver().openFileDescriptor(selectedPdfUri,
                        "r");

                if(parcelFileDescriptor != null){
                    PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);

                    int pageCount = renderer.getPageCount();

                    for(int pageIndex = 0; pageIndex < pageCount ; pageIndex++) {

                        PdfRenderer.Page page = renderer.openPage(pageIndex);
                        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);

                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawColor(Color.WHITE);

                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                        page.close();
                        saveImages(bitmap, Integer.toString((pageIndex + 1)));
                    }

                    renderer.close();

                    Toast.makeText(requireContext(), "Images saved successfully at " + dirPath, Toast.LENGTH_SHORT).show();

                }
            } catch(Exception e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(requireContext(), "Please select a PDF file first..", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImages(Bitmap finalBitmap, String fileName) {
        String dirName = getFileNameWithoutExtension(selectedPdfName);

        final String path = Environment.getExternalStorageDirectory().toString();
        dirPath = path + "/Scanner Document/Pdf To Image/" + dirName;
        final File directory = new File(dirPath);

        if(!directory.exists()){
            directory.mkdirs();
        }

        String finalFilePath = directory + "/" + fileName + ".jpg";

        File file = new File(finalFilePath);

        try {
            OutputStream outputStream = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch(Exception e){
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getFileNameWithoutExtension(String fileName){
        return fileName.substring(0, fileName.lastIndexOf("."));
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
            startActivityForResult(intent, 0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == Activity.RESULT_OK && requestCode == 0) {
            Uri fileUri = intent.getData();
            if(fileUri != null){
                selectedPdfUri = fileUri;
                selectedPdfName = new File(fileUri.getPath()).getName();
                tvPdfName.setText(selectedPdfName);
                tvPdfName.setVisibility(View.VISIBLE);
                ivPdfSymbol.setVisibility(View.VISIBLE);
            }
        }
    }
}