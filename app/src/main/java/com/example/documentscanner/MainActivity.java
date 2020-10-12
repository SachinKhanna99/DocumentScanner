package com.example.documentscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.documentscanner.Util.DialogUtil;
import com.example.documentscanner.Util.PDFWriterUtil;
import com.example.documentscanner.Util.Ui;
import com.example.documentscanner.Util.UtilDialogCallback;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
FloatingActionButton camera;
    private FileAdapter fileAdapter;
    private final Context c = this;
    private List<Uri> scannedBitmaps = new ArrayList<>();

    private DocumentView viewModel;
    private LinearLayout emptyLayout;

    private String searchText = "";
    LiveData<List<Document>> liveData;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        camera=findViewById(R.id.scan);
        this.emptyLayout=findViewById(R.id.empty_list);
        recyclerView=findViewById(R.id.recycle);
        Ui.setNavigationBar(recyclerView,this);

        final String baseStorageDirectory =  getApplicationContext().getString( R.string.storage_path);
        mkdir( baseStorageDirectory );

        final String baseStagingDirectory =  getApplicationContext().getString( R.string.storage_stage);
       mkdir( baseStagingDirectory );

        final String scanningTmpDirectory = "Scanner Document/temp/";
       mkdir( scanningTmpDirectory );

        viewModel = ViewModelProviders.of(this).get(DocumentView.class);
        fileAdapter = new FileAdapter(viewModel,this);
        recyclerView.setAdapter( fileAdapter );

        liveData = viewModel.getAllDocuments();
        liveData.observe(this, new Observer<List<Document>>() {
            @Override
            public void onChanged(@Nullable List<Document> documents) {

                if( documents.size() > 0 ){
                    emptyLayout.setVisibility(View.GONE);

                } else {
                    emptyLayout.setVisibility(View.VISIBLE);
                }

                fileAdapter.setData(documents);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);



        Dexter.withContext(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        }).check();
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scannedBitmaps.clear();

                String stagingDirPath = getApplicationContext().getString( R.string.storage_stage );
                String scanningTmpDirectory =  getApplicationContext().getString( R.string.base_scantmp_path );;

                clearDirectory( stagingDirPath );
                clearDirectory( scanningTmpDirectory );


                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);

                //startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
                startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE, options.toBundle());
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.default_menu, menu);
        return true;
    }

    public  void Search(MenuInflater menuInflater)
    {
        //TODO MAKE SEARCH CLASS
    }
    public  void prefrence(MenuInflater menuInflater)
    {
        //TODO Refrence class
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<File> getAllFiles(String dirPath ){

        List<File> fileList = new ArrayList<>();

        final File sd = Environment.getExternalStorageDirectory();
        File targetDirectory = new File( sd, dirPath );

        if( targetDirectory.listFiles() != null ){
            for( File eachFile : targetDirectory.listFiles() ){
                fileList.add(eachFile);

            }
        }

        fileList.sort( new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        });
        return fileList;

    }

    public static void mkdir( String dirPath ){

        final File sd = Environment.getExternalStorageDirectory();
        File storageDirectory = new File(sd, dirPath);
        if (!storageDirectory.exists()) {
            storageDirectory.mkdir();
        }
    }

    public static void clearDirectory( String dirPath ){

        final File sd = Environment.getExternalStorageDirectory();
        File targetDirectory = new File( sd, dirPath );

        if( targetDirectory.listFiles() != null ){
            for( File tempFile : targetDirectory.listFiles() ){
                tempFile.delete();
            }
        }

    }



    public static void writeFile( String baseDirectory, String filename, FileWrite callback ) throws IOException {

        final File sd = Environment.getExternalStorageDirectory();
        String absFilename = baseDirectory + filename;
        File dest = new File(sd, absFilename);

        FileOutputStream out = new FileOutputStream(dest);

        callback.write( out );

        out.flush();
        out.close();
    }

    public static void removeFile(  String filepath) {
        final File sd = Environment.getExternalStorageDirectory();
        File targetFile = new File( sd, filepath );
        targetFile.delete();
    }

    public static void moveFile(  String oldFilepath, String newFilePath) {
        final File sd = Environment.getExternalStorageDirectory();
        File targetFile = new File( sd, oldFilepath );
        targetFile.renameTo(new File(sd, newFilePath));
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    private void saveBitmap(final Bitmap bitmap, final boolean addMore ){

        final String baseDirectory =  getApplicationContext().getString( addMore ? R.string.storage_stage : R.string.storage_path);
        final File sd = Environment.getExternalStorageDirectory();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
        final String timestamp = simpleDateFormat.format( new Date() );

        if( addMore ){

            try {

                String filename = "SCANNED_STG_" + timestamp + ".png";

            writeFile(baseDirectory, filename, new FileWrite() {
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

            DialogUtil.UserFilename( c, null, null, new UtilDialogCallback() {

                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onSave(String textValue, String category) {

                    try {

                        final PDFWriterUtil pdfWriter = new PDFWriterUtil();

                        String stagingDirPath = getApplicationContext().getString( R.string.storage_stage );

                        List<File> stagingFiles = getAllFiles( stagingDirPath );
                        for ( File stagedFile : stagingFiles ) {
                            pdfWriter.addFile( stagedFile );
                        }

                        pdfWriter.addBitmap(bitmap);

                        String itemName = textValue.replaceAll("[^a-zA-Z0-9\\s]", "");
                        String filename = timestamp + "-" +  itemName + ".pdf";

                        mkdir(baseDirectory + "/" + category);
                     writeFile(baseDirectory + "/" + category + "/", filename, new FileWrite() {
                                 @Override
                                 public void write(FileOutputStream out) {
                                     try {
                                         pdfWriter.write(out);

                                     }catch (IOException e){
                                         e.printStackTrace();
                                     }
                                 }

                             });

                             fileAdapter.notifyDataSetChanged();

                       clearDirectory( stagingDirPath );

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

                    }catch(IOException ioe){
                        ioe.printStackTrace();

                    }

                }
            });

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void saveFile() {

        final String baseDirectory = getApplicationContext().getString(R.string.storage_path);
        final File sd = Environment.getExternalStorageDirectory();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
        final String timestamp = simpleDateFormat.format(new Date());


        DialogUtil.UserFilename(c, null, null, new UtilDialogCallback() {

            @Override
            public void onSave(String textValue, String category) {
                try {

                    final PDFWriterUtil pdfWriter = new PDFWriterUtil();

                    String stagingDirPath = getApplicationContext().getString(R.string.storage_stage);

                    List<File> stagingFiles = getAllFiles(stagingDirPath);
                    for (File stagedFile : stagingFiles) {
                        pdfWriter.addFile(stagedFile);
                    }

                    String itemName = textValue.replaceAll("[^a-zA-Z0-9\\s]", "");
                    String filename = timestamp + "-" +  itemName + ".pdf";

               mkdir(baseDirectory + "/" + category + "/");
                    writeFile(baseDirectory+ "/" + category + "/", filename, new FileWrite() {
                        @Override
                        public void write(FileOutputStream out) {
                            try {
                                pdfWriter.write(out);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    fileAdapter.notifyDataSetChanged();

                    clearDirectory(stagingDirPath);

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

                } catch (IOException ioe) {
                    ioe.printStackTrace();

                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( ( requestCode == ScanConstants.PICKFILE_REQUEST_CODE || requestCode == ScanConstants.START_CAMERA_REQUEST_CODE ) &&
                resultCode == Activity.RESULT_OK) {


            boolean saveMode = data.getExtras().containsKey(ScanConstants.SAVE_PDF) ? data.getExtras().getBoolean( ScanConstants.SAVE_PDF ) : Boolean.FALSE;
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
                    Intent intent = new Intent(this, AddPagesActivity.class);
                    intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);

                    startActivityForResult(intent, ScanConstants.START_CAMERA_REQUEST_CODE);
                }


            }
        }
    }


}

