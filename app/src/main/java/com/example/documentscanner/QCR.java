package com.example.documentscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchUIUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.documentscanner.Util.Ui;
import com.example.documentscanner.Util.UtilQCR;
import com.example.myabc.R;

import java.io.File;
import java.util.ArrayList;

public class QCR extends AppCompatActivity {
EditText ocrtext;
ProgressBar progressBar;
Button share;
    public static String FILE_PATH = "file_path";
    RelativeLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_c_r);
        layout=findViewById(R.id.lau);

        ocrtext=findViewById(R.id.ocrText);
        progressBar=findViewById(R.id.Progress);
        share=findViewById(R.id.shareBtn);

        Ui.setNavigationBar(layout,this);
        ocrtext.setText("Please Wait");
        setTitle("Text Extracting");

        progressBar.setVisibility(View.INVISIBLE);
        share.setVisibility(View.GONE);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToShare = ocrtext.getText().toString();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, textToShare);
                startActivity(Intent.createChooser(sharingIntent, "Share Through"));
            }
        });
        Bundle bundle = getIntent().getExtras();
        final String filePath = bundle.getString(FILE_PATH);
        new OCRExtractTask( this, getApplicationContext(), filePath )
                .execute();
    }
    public void setText( String content ){
        this.ocrtext.setText( content );
    }
    private class OCRExtractTask extends AsyncTask<String, Void, String> {

        private QCR ocrActivity;
        private Context context;
        private String filePath;

        public OCRExtractTask(QCR ocrActivity, Context context, String filePath ){
            this.ocrActivity = ocrActivity;
            this.context = context;
            this.filePath = filePath;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                ArrayList<Bitmap> bitmaps = new ArrayList<>();
                final String baseDirectory = context.getString(R.string.storage_path);
                final File sd = Environment.getExternalStorageDirectory();

                File toOcr = new File(sd, baseDirectory + this.filePath);

                PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(toOcr, ParcelFileDescriptor.MODE_READ_ONLY));

                Bitmap bitmap;
                final int pageCount = renderer.getPageCount();
                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = renderer.openPage(i);

                    int width = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                    int height = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    bitmaps.add(bitmap);

                    // close the page
                    page.close();

                }

                // close the renderer
                renderer.close();

                StringBuffer extractedText = new StringBuffer();
                for (Bitmap eachPage : bitmaps ) {
                    extractedText.append(
                            UtilQCR.getText(context, eachPage)
                    );

                }

                Log.d(  "Clean Scan", "detected text : " + extractedText );
                this.ocrActivity.setText(extractedText.toString() );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        share.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }catch (Exception e){
                Log.e( "Document Scaner", "Unable to extract text", e );
                this.ocrActivity.setText( " OOPS ! Unable to extract text ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        share.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }
            finally {

                return  null;
            }
        }
    }
}