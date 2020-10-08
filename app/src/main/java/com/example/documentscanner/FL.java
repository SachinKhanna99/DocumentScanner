package com.example.documentscanner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FL extends RecyclerView.ViewHolder {
    private ImageView categoryIcon;
    private TextView textViewLabel;
    private TextView textViewTime;
    private TextView textViewCategory;
    private TextView textPageCount;
    private LinearLayout itemLayout;
    private ActionMode.Callback actionModeCallbacks;
    private FileAdapter adapter;
    private com.example.documentscanner.Document documnt;

    private Map<String, Integer> categoryImageMap = new HashMap<>();

    public FL(View itemView, ActionMode.Callback actionModeCallbacks, FileAdapter adapter) {
        super(itemView);
        this.categoryIcon = itemView.findViewById(R.id.imageView);
        this.textViewLabel = itemView.findViewById(R.id.fileName);
        this.textViewTime = itemView.findViewById(R.id.timeLabel);
        this.textViewCategory = itemView.findViewById(R.id.categoryLabel);
        this.textPageCount = itemView.findViewById(R.id.pageCount);
        this.itemLayout = itemView.findViewById(R.id.relativeLayout);
       this.adapter=adapter;
        this.actionModeCallbacks = actionModeCallbacks;

        //TODO ADDING CATOGERY WISE
    }

   //TODO ADD IMPLEMENTATION
}