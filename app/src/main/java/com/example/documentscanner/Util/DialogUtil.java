package com.example.documentscanner.Util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.documentscanner.MainActivity;
import com.example.documentscanner.R;

public class DialogUtil {

    public static void UserFilename(final Context context, String FileName, String Category, final UtilDialogCallback callback ){

        final Toast s=Toast.makeText(context,"File saved",Toast.LENGTH_LONG);
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.file_input, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(mView);

        final EditText fileNameText = mView.findViewById(R.id.userInputDialog);
        if( FileName != null ){
            fileNameText.setText(FileName);
        }

        final Spinner categorySelection = mView.findViewById(R.id.userInputCategory);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.category, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySelection.setAdapter(adapter);

        if(Category != null ){
            String[] categoryArray = mView.getResources().getStringArray( R.array.category );
            for( int i = 0; i < categoryArray.length; i++ ){
                if(Category.equals( categoryArray[i] ) ) {
                    categorySelection.setSelection(i);
                }
            }
        }

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        callback.onSave(
                                fileNameText.getText().toString(),
                                categorySelection.getSelectedItem().toString(),
                                s

                        );
                    }

                }
                )

                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();

    }
}
