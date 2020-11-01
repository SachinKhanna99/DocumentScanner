package com.example.monscanner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import android.provider.MediaStore;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *  ATTENTION : seule la résolution d'affichage peut être modifiée afin d'viter les
 *  lags dus à une qualité d'image trop haute. L'image obtenue lors de la capture de celle-ci
 *  sera toujours de la qualité la plus haute (à vérifier mais je crois)
 */
public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivityDebug";
    private static final String SETTINGS = "Settings";
    private static final String FORMAT = "idFormat";

    private ImageButton settingsButton;
    private int selectedFormat;

    private String cameraId;
    private Uri fileUri;
    private Size imageDimension;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraSession;
    private Handler backgroundHandler;
    private HandlerThread handlerThread;
    private FrameLayout frameLayout;
    private TextureView textureView;
    Camera mCamera;
    private ImageView flash;
    boolean isflash=false;


    private List<Integer> indicesFormat;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        SharedPreferences preferences = getSharedPreferences(SETTINGS, MODE_PRIVATE);
        selectedFormat = preferences.getInt(FORMAT,1);



        frameLayout = findViewById(R.id.frameLayout);
        textureView = findViewById(R.id.textureView);
     //   gallery=findViewById(R.id.galleryButton);


     flash=findViewById(R.id.flash_on);
     flash.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if(!isflash)
            {
//                flash.setText("flash is on");
                flash.setImageResource(R.drawable.ic_baseline_flash_on_24);

                isflash=true;
            }else {
           //     flash.setText("flash is off");
                flash.setImageResource(R.drawable.flashoff);
                isflash=false;
            }
         }
     });

        ImageButton button = findViewById(R.id.photoButton);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
//        gallery.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openGallery();
//            }
//        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    takePicture();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });

    }

//    private void openGallery() {
//
//
//        Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
//        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_MEDIA);
//
//        startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE);
//
////        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
////        intent.addCategory(Intent.CATEGORY_OPENABLE);
////        intent.setType("image/*");
////        startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE);
//    }


    private void openInfos(View v) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.popup_camera_infos,null);
        PopupWindow popupInfos = new PopupWindow(content, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupInfos.showAsDropDown(v);
    }

    // Ouvre le menu pour changer de résolution d'affichage
    private void openSettings(View v) throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraId = cameraManager.getCameraIdList()[0];
        CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraId);


        StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        // Ajout des différentes options en fonction de celles disponibles sur l'appareil
        PopupMenu menu = new PopupMenu(this, v);
        for (int i=0; i<indicesFormat.size(); i++) {
            assert map != null;
            int width = map.getOutputSizes(SurfaceTexture.class)[indicesFormat.get(i)].getWidth();
            int height = map.getOutputSizes(SurfaceTexture.class)[indicesFormat.get(i)].getHeight();
            if (i==selectedFormat)
                menu.getMenu().add(0, i, i, width+"x"+height+" (active)");
            else
                menu.getMenu().add(0, i, i, width+"x"+height);
        }

        // Traitement de la sélection d'une option
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences preferences = getSharedPreferences(SETTINGS, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(FORMAT, item.getItemId());
                editor.apply();
                selectedFormat = item.getItemId();

                try {
                    stopBackgroundThread();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startBackgroundThread();

                if (textureView.isAvailable()) {
                    try {
                        openCamera();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    textureView.setSurfaceTextureListener(surfaceTextureListener);
                }
                return true;
            }
        });
        menu.show();
    }


    // Recherche des indices de formats correspondant à celui du format JPEG le plus haut
    private void setIndicesFormat() throws CameraAccessException {
        indicesFormat = new ArrayList<>();

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraId = cameraManager.getCameraIdList()[0];

        CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        assert map != null;
        float rapport = ((float) map.getOutputSizes(ImageFormat.JPEG)[0].getWidth())/((float) map.getOutputSizes(ImageFormat.JPEG)[0].getHeight());

        for (int i=0; i < map.getOutputSizes(SurfaceTexture.class).length; i++) {
            float rapport2 = ((float) map.getOutputSizes(SurfaceTexture.class)[i].getWidth())/((float) map.getOutputSizes(SurfaceTexture.class)[i].getHeight());
            if (rapport == rapport2)
                indicesFormat.add(i);
        }
    }


    private void setAspectRatioTextureView(int resolutionWidth , int resolutionHeight ) {

        if(resolutionWidth > resolutionHeight) {
            int newWidth = frameLayout.getWidth();
            int newHeight = (frameLayout.getWidth()*resolutionHeight)/resolutionWidth;
            if (newHeight > frameLayout.getHeight()) {
                newHeight = frameLayout.getHeight();
                newWidth = (frameLayout.getHeight()*resolutionWidth)/resolutionHeight;
            }
            updateTextureViewSize(newWidth,newHeight);
        } else {
            int newHeight = frameLayout.getHeight();
            int newWidth = (frameLayout.getHeight()*resolutionWidth)/resolutionHeight;
            if (newWidth > frameLayout.getWidth()) {
                newWidth = frameLayout.getWidth();
                newHeight = (frameLayout.getWidth()*resolutionHeight)/resolutionWidth;
            }
            updateTextureViewSize(newWidth,newHeight);
        }
    }

    // Change la taille de la TextureView pour être la même que celle du format
    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        textureView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
    }


    // Traitement lors de l'appui sue le bouton de capture d'image
    private void takePicture() throws CameraAccessException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }
        else {
            if (cameraDevice == null)
                return;

            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = cameraManager.getCameraIdList()[0];

            CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            assert map != null;
            int width = map.getOutputSizes(ImageFormat.JPEG)[0].getWidth();
            int height = map.getOutputSizes(ImageFormat.JPEG)[0].getHeight();

            ImageReader reader = ImageReader.newInstance(width, height,ImageFormat.JPEG,1);
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            if(isflash)
            {
                captureBuilder.set(CaptureRequest.FLASH_MODE,CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);
            }
            if(!isflash)
            {
                captureBuilder.set(CaptureRequest.FLASH_MODE,CameraMetadata.FLASH_MODE_OFF);
            }

            ;
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
             captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 6);
            captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, 1500);
            CameraManager manager = (CameraManager)this.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            double exposureCompensationSteps = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP).doubleValue();
            int exposureCompensation = (int)( 2.0 / exposureCompensationSteps );
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Rotation du JPEG
            int rotation = getWindowManager().getDefaultDisplay().getRotation();

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));


            File file = createImageFile();
            final boolean isDirectoryCreated = file.getParentFile().mkdirs();
            Log.d("", "openCamera: isDirectoryCreated: " + isDirectoryCreated);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri tempFileUri = FileProvider.getUriForFile(getApplicationContext(),
                        "com.example.myabc.provider", // As defined in Manifest
                        file);

            } else {
//                Uri tempFileUri = Uri.fromFile(file);
                    File folder = new File(ScanConstants.IMAGE_PATH);
                   if (!folder.exists())
                         if (!folder.mkdirs())
                               Log.d(TAG, "takePicture: Impossible de creer le dossier");
             file = new File(ScanConstants.IMAGE_PATH + "/pic.jpg");
            }

            final File finalFile = file;
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    try (Image image = reader.acquireLatestImage()) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    try (OutputStream output = new FileOutputStream(finalFile)) {
                        output.write(bytes);
                        Intent data = new Intent();
                        Uri uri = Uri.fromFile(finalFile);
                        data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                        setResult(RESULT_OK, data);
                        finish();

                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, backgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    try {
                        startCameraPreview();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, backgroundHandler);
        }
    }
    private File createImageFile() {
        clearTempImages();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File basePath = new File(ScanConstants.IMAGE_PATH);
        if(! basePath.exists() ){
            basePath.mkdirs();
        }

        File file = new File(ScanConstants.IMAGE_PATH, "IMG_" + timeStamp +".jpg");
        fileUri = Uri.fromFile(file);
        return file;

    }
    private void clearTempImages() {
        try {
            File tempFolder = new File(ScanConstants.IMAGE_PATH);
            for (File f : tempFolder.listFiles())
                f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Range<Integer> getRange() {
        CameraManager mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics chars = null;
        try {
            chars = mCameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Range<Integer>[] ranges = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

        Range<Integer> result = null;

        for (Range<Integer> range : ranges) {
            int upper = range.getUpper();

            // 10 - min range upper for my needs
            if (upper >= 10) {
                if (result == null || upper < result.getUpper().intValue()) {
                    result = range;
                }
            }
        }
        return result;
    }
    TextureView.SurfaceTextureListener surfaceTextureListener  = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera();

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }

    };

    // Ouvre la camera dès que la TextureView est prête
    private void openCamera() throws CameraAccessException {
        final CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraId = cameraManager.getCameraIdList()[0];

        CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);



        setIndicesFormat();

        // selection du format d'affichage
        int indice = indicesFormat.get(selectedFormat);
        assert map != null;
        imageDimension = map.getOutputSizes(SurfaceTexture.class)[indice];

        setAspectRatioTextureView(imageDimension.getHeight(),imageDimension.getWidth());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
        }
        else {
            cameraManager.openCamera(cameraId, stateCallBack, null);
        }
    }

    CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                startCameraPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    // Lance l'affichage de l'image sur la surface
    private void startCameraPreview() throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());

        Surface surface = new Surface(texture);



        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
//        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 6);
//     captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, 1500);

        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,getRange());

        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if (cameraDevice == null)
                    return;

                cameraSession = session;
                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, null);
    }

    // Met à jour l'image affichée
    private void updatePreview() throws CameraAccessException {
        if (cameraDevice == null)
            return;

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        cameraSession.setRepeatingRequest(captureRequestBuilder.build(),null, backgroundHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if (textureView.isAvailable()) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    // lance le thread de capture des images pour l'affichage
    private void startBackgroundThread() {
        handlerThread = new HandlerThread("Camera background");
        handlerThread.start();

        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    protected void onPause() {
        try {
            stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    private void stopBackgroundThread() throws InterruptedException {
        handlerThread.quitSafely();
        handlerThread.join();

        backgroundHandler = null;
        handlerThread = null;
    }
}
