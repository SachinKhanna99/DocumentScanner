package com.example.myabc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myabc.Util.DialogUtil;
import com.example.myabc.Util.FileIo;
import com.example.myabc.Util.UtilDialogCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pdf.converter.PdfConverter;

import static java.security.AccessController.getContext;


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


//


        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {



            final String baseDirectory = context.getString(R.string.base_storage_path);
            final File sd = Environment.getExternalStorageDirectory();



            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
            final String timestamp = simpleDateFormat.format(new Date());


            switch (item.getItemId()) {
                case R.id.menu_delete:

                    for(Document documentitem:selectedItems)
                    {
                        File convert=new File(sd,baseDirectory+documentitem.getPath());

                        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                    }


//                    for (Document documentItem  : selectedItems) {
//
//                        File toDelete = new File( sd, baseDirectory + documentItem.getPath() );
//                        toDelete.delete();
//                        viewModel.deleteDocument(documentItem);
//                        FileIo.removeFile(baseDirectory + documentItem.getPath());
//                    }

                    mode.finish();
                    return true;

                case R.id.menu_edit:

                    final Document docToRename = selectedItems.get(0);
                    DialogUtil.UserFilename(context, docToRename.getName(), docToRename.getCategory(), new UtilDialogCallback() {
                        @Override
                        public void onSave(String textValue, String category, Toast messgae) {
                            String oldFilePath = docToRename.getPath();
                            String itemName = textValue.replaceAll("[^a-zA-Z0-9\\s]", "");
                            String newFilePath = timestamp + "-" +  itemName + ".pdf";

                            FileIo.mkdir(baseDirectory + "/" + category + "/");
                            FileIo.moveFile(
                                    baseDirectory + oldFilePath,
                                    baseDirectory + "/" + category + "/" + newFilePath
                            );

                            docToRename.setName( textValue );
                            docToRename.setCategory( category );
                            docToRename.setPath(category + "/" + newFilePath);
                            viewModel.updateDocument(docToRename);

                            Toast toast = Toast.makeText(context, "Renamed to " + textValue, Toast.LENGTH_SHORT);
                            toast.show();

                            notifyDataSetChanged();

                        }
                    } );

                    mode.finish();
                    return true;

                case R.id.menu_ocr:

                    final Document docToOcr = selectedItems.get(0);
                    File toOcr = new File( sd, baseDirectory + docToOcr.getPath() );

                    Intent intent = new Intent( context, QCR.class);
                    Bundle bundle = new Bundle();
                    bundle.putString( QCR.FILE_PATH, docToOcr.getPath()); //Your id
                    intent.putExtras(bundle); //Put your id to your next Intent
                    context.startActivity(intent);

                    mode.finish();
                    return true;


//

                default:
                    return false;
            }
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelect = false;
            selectedItems.clear();
            notifyDataSetChanged();
        }
    };


    public FileAdapter(DocumentView viewModel, Context context ){
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


        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item_view,parent,false);
        FL viewHolder = new FL(view, actionModeCallbacks, this );

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FL holder, int position) {
        holder.setDocument( this.documentList.get(position) );
    }

    @Override
    public int getItemCount() {
        return this.documentList.size();
    }



}
