package com.example.myabc.fragments;

import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myabc.Document;
import com.example.myabc.R;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import pdf.converter.PdfConverter;


public class PdfToImageFragment extends Fragment {







    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root= inflater.inflate(R.layout.fragment_pdf_to_image, container, false);
       ;
        return  root;
    }
}