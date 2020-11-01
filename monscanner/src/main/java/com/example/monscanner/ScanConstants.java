package com.example.monscanner;

import android.os.Environment;

/**
 * Constantes qui sont utilisées dans la librairie
 */
public abstract class ScanConstants {

    public static final int OPEN_MEDIA = 1;
    public static final int OPEN_CAMERA = 2;

    static final String PHOTO_RESUL = "photo_result";
    public final  static int PICKFILE_REQUEST_CODE = 3;
    public  final static int START_CAMERA_REQUEST_CODE = 4;
    public final static String SCAN_MORE = "scanMore";
    final static String IMAGE_PATH =  Environment.getExternalStorageDirectory().getPath() + "/Scanner Document/tmp";
    public static final String OPEN_INTENT_PREFERENCE = "open_intent_preference";
    final static String SELECTED_BITMAP = "selectedBitmap";
    public static final String SCANNED_RESULT = "scannedResult";
    public final static String SAVE_PDF = "savePdf";

}
