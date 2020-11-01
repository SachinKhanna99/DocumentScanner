package com.example.myabc.Util.ui.home;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monscanner.ScanActivity;
import com.example.monscanner.ScanConstants;
import com.example.myabc.AddPagesActivity;
import com.example.myabc.Document;
import com.example.myabc.DocumentView;
import com.example.myabc.FileAdapter;
import com.example.myabc.FileWrite;
import com.example.myabc.HomePageActitvity;
import com.example.myabc.R;
import com.example.myabc.SearchActivity;
import com.example.myabc.SettingActivity;
import com.example.myabc.Util.DialogUtil;
import com.example.myabc.Util.FileIo;
import com.example.myabc.Util.PDFWriterUtil;
import com.example.myabc.Util.Ui;
import com.example.myabc.Util.UtilDialogCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    FloatingActionButton camera;
     public static FileAdapter fileAdapter;
    private final HomeFragment c = this;
    private List<Uri> scannedBitmaps = new ArrayList<>();

   public static DocumentView viewModel;
    private LinearLayout emptyLayout;

    private String searchText = "";
    public static LiveData<List<Document>> liveData;


public HomeFragment(){}

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( ( requestCode == ScanConstants.PICKFILE_REQUEST_CODE || requestCode == ScanConstants.START_CAMERA_REQUEST_CODE ) &&
                resultCode == Activity.RESULT_OK) {


            boolean saveMode = data.getExtras().containsKey(ScanConstants.SAVE_PDF) ? data.getExtras().getBoolean(ScanConstants.SAVE_PDF ) : Boolean.FALSE;
            if(saveMode){
                saveFile();

            } else {
                Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
                boolean doScanMore = data.getExtras().getBoolean(ScanConstants.SCAN_MORE);

                final File sd = Environment.getExternalStorageDirectory();
                File src = new File(sd, uri.getPath());
                Bitmap bitmap = BitmapFactory.decodeFile(src.getAbsolutePath());

                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                saveBitmap(bitmap, doScanMore);

                if (doScanMore) {
                    scannedBitmaps.add(uri);
                    Intent intent = new Intent(getActivity(), AddPagesActivity.class);
                    intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);

                    startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
                }


            }
        }


    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final RecyclerView recyclerView=root.findViewById(R.id.recycle);
       final FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                scannedBitmaps.clear();

                String stagingDirPath = getActivity().getApplicationContext().getString(R.string.storage_staging_path);
                String scanningTmpDirectory = getActivity().getApplicationContext().getString(R.string.base_scantmp_path);

                FileIo.clearDirectory(stagingDirPath);
                FileIo.clearDirectory(scanningTmpDirectory);


                Intent intent = new Intent(getContext(), ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);
                //startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity());
                startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE, options.toBundle());
            }
        });
        final LinearLayout emptyLayout=root.findViewById(R.id.empty_list);

        final String baseStorageDirectory =  getActivity().getApplicationContext().getString( R.string.storage_path);
        FileIo.mkdir( baseStorageDirectory);

        final String baseStagingDirectory = getActivity().getApplicationContext().getString( R.string.storage_staging_path);
        FileIo.mkdir( baseStagingDirectory );

        final String scanningTmpDirectory =  getActivity().getApplicationContext().getString( R.string.base_scantmp_path);;
        FileIo.mkdir( scanningTmpDirectory );

        viewModel = ViewModelProviders.of(this).get(DocumentView.class);
        fileAdapter = new FileAdapter(viewModel,getContext());
        recyclerView.setAdapter( fileAdapter );
        liveData = viewModel.getAllDocuments();
        liveData.observe(getActivity(), new Observer<List<Document>>() {
            @Override
            public void onChanged(@Nullable List<Document> documents) {

                if( documents.size() > 0 )
                {
                    emptyLayout.setVisibility(View.GONE);

                } else {
                    emptyLayout.setVisibility(View.VISIBLE);
                }

                fileAdapter.setData(documents);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        return root;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.default_menu, menu);
    }



    public void goToSearch(MenuItem mi){
        Intent intent = new Intent(getActivity().getApplicationContext(), SearchActivity.class);
        startActivityForResult(intent, 0);
    }


    public void goToPreferences(MenuItem mi){
        Intent intent = new Intent(getActivity().getApplicationContext(), SettingActivity.class);
        startActivityForResult(intent, 0);
    }

    private void saveBitmap(final Bitmap bitmap, final boolean addMore ){

        final String baseDirectory =  getActivity().getApplicationContext().getString( addMore ? R.string.storage_staging_path : R.string.storage_path);
        final File sd = Environment.getExternalStorageDirectory();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
        final String timestamp = simpleDateFormat.format( new Date() );

        if( addMore ){

            try {

                String filename = "SCANNED_STG_" + timestamp + ".png";

                FileIo.writeFile(baseDirectory, filename, new FileWrite() {
                    @Override
                    public void write(FileOutputStream out) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    }
                });

                bitmap.recycle();
                System.gc();

            }catch(IOException ioe){
                ioe.printStackTrace();
            }

        } else {

            DialogUtil.UserFilename( getContext(), null, null, new UtilDialogCallback() {


                @Override
                public void onSave(String textValue, String category, Toast mess) {

                    try {

                        final PDFWriterUtil pdfWriter = new PDFWriterUtil();

                        String stagingDirPath = getActivity().getApplicationContext().getString( R.string.storage_staging_path );

                        List<File> stagingFiles = FileIo.getAllFiles( stagingDirPath );
                        for ( File stagedFile : stagingFiles ) {
                            pdfWriter.addFile( stagedFile );
                        }

                        pdfWriter.addBitmap(bitmap);

                        String itemName = textValue.replaceAll("[^a-zA-Z0-9\\s]", "");
                        String filename = timestamp + "-" +  itemName + ".pdf";

                        FileIo.mkdir(baseDirectory + "/" + category);
                        FileIo.writeFile(baseDirectory + "/" + category + "/", filename, new FileWrite() {
                            @Override
                            public void write(FileOutputStream out) {
                                try {
                                    pdfWriter.write(out);

                                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                            }

                        });

                        fileAdapter.notifyDataSetChanged();

                        FileIo.clearDirectory( stagingDirPath );

                        SimpleDateFormat simpleDateFormatView = new SimpleDateFormat("dd-MM-yyyy hh:mm");
                        final String timestampView = simpleDateFormatView.format(new Date());

                        Document newDocument = new Document();
                        newDocument.setName( textValue );
                        newDocument.setCategory( category );
                        newDocument.setPath( category + "/" + filename );
                        newDocument.setScanned( timestampView );
                        newDocument.setPageCount( pdfWriter.getPageCount() );
                        viewModel.saveDocument(newDocument);

                        pdfWriter.close();

                        bitmap.recycle();
                        System.gc();

                    }
                    catch(IOException ioe){
                        ioe.printStackTrace();

                    }

                }
            });

        }
    }

    private void saveFile() {

        final String baseDirectory = getActivity().getApplicationContext().getString(R.string.storage_path);
        final File sd = Environment.getExternalStorageDirectory();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
        final String timestamp = simpleDateFormat.format(new Date());


        DialogUtil.UserFilename(getContext(), null, null, new UtilDialogCallback() {

            @Override
            public void onSave(String textValue, String category, final Toast mess) {
                try {

                    final PDFWriterUtil pdfWriter = new PDFWriterUtil();

                    String stagingDirPath = getActivity().getApplicationContext().getString(R.string.storage_staging_path);

                    List<File> stagingFiles = FileIo.getAllFiles(stagingDirPath);
                    for (File stagedFile : stagingFiles) {
                        pdfWriter.addFile(stagedFile);
                    }

                    String itemName = textValue.replaceAll("[^a-zA-Z0-9\\s]", "");
                    String filename = timestamp + "-" +  itemName + ".pdf";

                    FileIo.mkdir(baseDirectory + "/" + category + "/");
                    FileIo.writeFile(baseDirectory+ "/" + category + "/", filename, new FileWrite() {
                        @Override
                        public void write(FileOutputStream out) {
                            try {
                                pdfWriter.write(out);
                                Toast.makeText(getContext(), "File Saved", Toast.LENGTH_SHORT).show();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    fileAdapter.notifyDataSetChanged();

                    FileIo.clearDirectory(stagingDirPath);

                    SimpleDateFormat simpleDateFormatView = new SimpleDateFormat("dd-MM-yyyy hh:mm");
                    final String timestampView = simpleDateFormatView.format(new Date());

                    Document newDocument = new Document();
                    newDocument.setName(textValue);
                    newDocument.setCategory(category);
                    newDocument.setPath(category + "/" + filename);
                    newDocument.setScanned(timestampView);
                    newDocument.setPageCount(pdfWriter.getPageCount());
                    viewModel.saveDocument(newDocument);

                    pdfWriter.close();

                    System.gc();

                }
                catch (IOException ioe) {
                    ioe.printStackTrace();

                }
            }
        });
    }

}
