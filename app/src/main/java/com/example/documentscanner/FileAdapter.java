package com.example.documentscanner;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import androidx.appcompat.view.ActionMode;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

import java.util.List;


public class FileAdapter extends RecyclerView.Adapter<FL> {

    final Context context;
    protected ActionMode mActionMode;

    public boolean multiSelect = false;
    private List<Document> documentList = new ArrayList<>();
    public List<Document> selectedItems = new ArrayList<>();

    private DocumentView viewModel;

    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            multiSelect = true;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            if( selectedItems.size() == 0 || selectedItems.size() == 1 ){
                MenuInflater inflater = mode.getMenuInflater();
                menu.clear();
                inflater.inflate(R.menu.single_select_menu, menu);
                mode.setTitle( "1 Selected" );
                return true;

            } else {
                MenuInflater inflater = mode.getMenuInflater();
                menu.clear();
                inflater.inflate(R.menu.multi_select_menu, menu);
                mode.setTitle( selectedItems.size() + " Selected" );
                return true;
            }

        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelect = false;
            selectedItems.clear();
            notifyDataSetChanged();
        }
    };


    public FileAdapter( DocumentView viewModel, Context context ){
        this.viewModel = viewModel;
        this.context = context;
    }

    public void setData(List<Document> documents){
        this.documentList.clear();
        this.documentList.addAll( documents );
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FL onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item_view,parent,false);
        FL viewHolder = new FL(view, actionModeCallbacks, this );

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FL holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }



}
