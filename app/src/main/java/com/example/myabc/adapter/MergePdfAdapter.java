package com.example.myabc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myabc.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MergePdfAdapter extends RecyclerView.Adapter<MergePdfAdapter.MyViewHolder> {

    private List<String> list;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public View itemView;
        public MyViewHolder(View v) {
            super(v);
            itemView = v;
        }
    }

    public MergePdfAdapter(List<String> list) {
        this.list = list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    @NotNull
    @Override
    public MergePdfAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.merge_pdf_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String curPdfName = list.get(position);
        TextView tvPdfName = holder.itemView.findViewById(R.id.tvPdfName);
        tvPdfName.setText(curPdfName);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
