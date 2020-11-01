package com.example.monscanner;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import static org.opencv.core.Core.ROTATE_90_CLOCKWISE;
import static org.opencv.core.Core.ROTATE_90_COUNTERCLOCKWISE;
import static org.opencv.core.Core.rotate;
/**
 * ATTENTION: afin d'augmenter les performances les modifications ne sont appliquées que sur
 * l'image redimmentionnée afin que l'utilisateur ai rapidement un visuel de celles ci.
 * L'état actuel (couleur + rotation) de l'image redimentionnée est enregistré, seules les
 * modifications finales sont appliquées à l'image d'origine lors de la validation.
 */
public class EditionFragment extends Fragment {

    private static final String TAG = "EditionFragmentDebug";

    private final int ORIGINAL = 0;
    private final int MAGIC = 2;
    private final int GRAY = 4;
    private final int BW = 5;
    private final int SUMMER = 1;
    private final int PINK=3;
    private int couleur;
    private int angleFinal;
    private ImageView imageView;
    private View view;
    private static ProgressDialogFragment progressDialogFragment;
    private Mat edited;
    private Mat scaled;
    Bitmap original;
    Bitmap bmScaled;
    Bitmap transformed;
    private ScanActivity scanner;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        scanner = (ScanActivity) activity;
    }

    public EditionFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edition, null);
        init();
        return view;
    }

    private void init() {
        angleFinal = 0;
        couleur = ORIGINAL;

        // Set des OnClickListeners des boutons
        Button originalButton = view.findViewById(R.id.originalButton);
        originalButton.setOnClickListener(new OriginalListener());
        Button magicButton = view.findViewById(R.id.magicButton);
        magicButton.setOnClickListener(new MagicListener());
        Button grayButton = view.findViewById(R.id.grayButton);
        grayButton.setOnClickListener(new GrayListener());
        Button bwButton = view.findViewById(R.id.bwButton);

        Button summer=view.findViewById(R.id.summerButton);
        summer.setOnClickListener(new SummerColor());
        bwButton.setOnClickListener(new BWListener());
        ImageView right=view.findViewById(R.id.right);
        ImageView left=view.findViewById(R.id.left);

//        ImageButton rgaucheButton = view.findViewById(R.id.rgaucheButton);
        left.setOnClickListener(new RotationListener(ROTATE_90_COUNTERCLOCKWISE));

        right.setOnClickListener(new RotationListener(ROTATE_90_CLOCKWISE));
        Button validationButton = view.findViewById(R.id.validationButton);
        validationButton.setOnClickListener(new ValidationListener());



        try {
            imageView = view.findViewById(R.id.rognedImageView);
            File file = new File(((Uri) Objects.requireNonNull(getArguments().getParcelable(ScanConstants.SCANNED_RESULT))).getPath());
            Uri uri = Uri.fromFile(file);
            original = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);

            // redimention de l'image
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    scaled = new Mat();
                    bmScaled = scanner.scaledBitmap(original,imageView.getWidth(), imageView.getHeight());
                    imageView.setImageBitmap(bmScaled);
                    Utils.bitmapToMat(bmScaled, scaled);
                    edited = scaled.clone();

                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // mise à jour de l'affichage
    private void updateImage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.matToBitmap(edited, bmScaled);
                imageView.setImageBitmap(bmScaled);

            }
        });
    }

    // affichage du dialogue d'attente lors d'une tache
    protected synchronized void showProgressDialog(String message) {
        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment.dismissAllowingStateLoss();
        }
        progressDialogFragment = null;
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    // fermeture du dialogue d'attente
    protected synchronized void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }

    // enregistrement de l'image dans les medias et renvoi de l'Uri de celle-ci
    @RequiresApi(api = Build.VERSION_CODES.N)
    private Uri getUri(Context context, final Bitmap bitmap) {
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
//        return Uri.parse(path);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
        String timestamp = simpleDateFormat.format(new Date());

        final String baseDirectory =  context.getString(R.string.base_scantmp_path);
        String filename = "TMP_STG_" + timestamp + ".png";

        try {
            String absPath = writeFile(baseDirectory, filename, new FileWritingCallbackS() {
                @Override
                public void write(FileOutputStream out) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
            });

            return Uri.parse(absPath);

        }catch (IOException ioe){
            ioe.printStackTrace();
            return null;
        }
    }
    public static String writeFile( String baseDirectory, String filename, FileWritingCallbackS callback ) throws IOException {

        final File sd = Environment.getExternalStorageDirectory();
        String absFilename = baseDirectory + filename;
        File dest = new File(sd, absFilename);

        FileOutputStream out = new FileOutputStream(dest);

        callback.write( out );

        out.flush();
        out.close();

        return  absFilename;
    }
    private interface FileWritingCallbackS {
        public void write(FileOutputStream out);
    }
    // set l'image en couleur originale
    private class OriginalListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.application));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    edited = scaled.clone();
                    updateImage();
                    couleur = ORIGINAL;
                }
            });
            dismissDialog();
        }
    }

    // set l'image en couleur renforcée
    private class MagicListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.application));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    float alpha = 1.9f;
                    float beta = -80;
                    scaled.convertTo(edited, -1, alpha, beta);
                    updateImage();
                    couleur = MAGIC;

                }
            });
            dismissDialog();
        }
    }


    // set l'image en couleur "niveau de gris"
    private class GrayListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.application));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Imgproc.cvtColor(scaled, edited, Imgproc.COLOR_BGR2GRAY);
                    updateImage();
                    couleur = GRAY;


                }
            });
            dismissDialog();
        }
    }
    private class  SummerColor implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.application));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "run: ------------->Working" );
                    Imgproc.cvtColor(scaled, edited, Imgproc.COLORMAP_SUMMER);
                    updateImage();
                    couleur = SUMMER;

                }
            });
            dismissDialog();
        }
    }
    private class  Pink implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.application));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Imgproc.cvtColor(scaled, edited, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
                    updateImage();
                    couleur = PINK;

                }
            });
            dismissDialog();
        }
    }
    // set l'image en couleur noir & blanc
    private class BWListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.application));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Imgproc.cvtColor(scaled, edited, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.threshold(edited,edited,0,255,Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
                    updateImage();
                    couleur = BW;
                }
            });
            dismissDialog();
        }
    }

    // effectue une rotation de 90° ou -90° de l'image
    private class RotationListener implements View.OnClickListener {
        private int angle;

        RotationListener(int i) {
            this.angle = i;
        }

        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.rotation));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    if (angle == ROTATE_90_CLOCKWISE)
                        angleFinal+=90;
                    else
                        angleFinal-=90;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bmScaled = Bitmap.createBitmap(bmScaled, 0, 0, bmScaled.getWidth(), bmScaled.getHeight(), matrix, true);
                    rotate(edited, edited, angle);
                    rotate(scaled,scaled,angle);
                    updateImage();
                }
            });
            dismissDialog();
        }
    }

    // validation des modifications et application des modifications à l'image d'origine
    private class ValidationListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.validation));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    bmScaled.recycle();
                    Matrix matrix = new Matrix();
                    matrix.postRotate(angleFinal);
                    original = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
                    Mat finie = new Mat();
                    Utils.bitmapToMat(original,finie);
                    switch(couleur) {
                        case ORIGINAL:
                            break;

                        case SUMMER:
                            Imgproc.cvtColor(finie,finie,Imgproc.COLORMAP_SUMMER);
                            Utils.matToBitmap(finie,original);
                            break;
                        case MAGIC:
                            float alpha = 1.9f;
                            float beta = -80f;
                            finie.convertTo(finie, -1, alpha, beta);
                            Utils.matToBitmap(finie, original);
                            break;

                        case PINK:
                            Imgproc.cvtColor(finie,finie,Imgproc.THRESH_BINARY);
                            Imgproc.threshold(finie,finie,0,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C|Imgproc.THRESH_BINARY);
                            Utils.matToBitmap(finie,original);
                            break;

                        case GRAY:
                            Imgproc.cvtColor(finie, finie, Imgproc.COLOR_BGR2GRAY);
                            Utils.matToBitmap(finie, original);
                            break;

                        case BW:
                            Imgproc.cvtColor(finie, finie, Imgproc.COLOR_BGR2GRAY);
                            Imgproc.threshold(finie,finie,0,255,Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
                            Utils.matToBitmap(finie, original);
                            break;
                    }
                    try {
                        Intent data = new Intent();
                        Bitmap bitmap = transformed;
                        if (bitmap == null) {
                            bitmap = original;
                        }
                        Uri uri = getUri(getActivity().getApplicationContext(), bitmap);
                        data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                        data.putExtra(ScanConstants.SCAN_MORE, true);
                        getActivity().setResult(Activity.RESULT_OK, data);

                        original.recycle();
                        System.gc();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "run: ----------->eveenvenvnevnev" );
                                dismissDialog();
                                getActivity().finish();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
//                    try {
//                        Intent data = new Intent();
//                        Bitmap bitmap = transformed;
//                        if (bitmap == null) {
//                            bitmap = original;
//                        }
//                        Uri uri = getUri(getActivity().getApplicationContext(), original);
//                        data.putExtra(ScanConstants.SCANNED_RESULT, uri);
//                        data.putExtra(ScanConstants.SCAN_MORE, false);
//                        getActivity().setResult(Activity.RESULT_OK, data);
//                        original.recycle();
//                        System.gc();
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                dismissDialog();
//                                getActivity().finish();
//                            }
//                        });
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        Intent data = new Intent();
//                        Uri uri = getUri(getActivity().getApplicationContext(), original);
//                        data.putExtra(ScanActivity.SCAN_RESULT, uri);
//                        getActivity().setResult(Activity.RESULT_OK, data);
//                        original.recycle();
//                        scanner.clearTempImages();
//                        System.gc();
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                dismissDialog();
//                                getActivity().finish();
//                            }
//                        });
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            });
        }
    }
}
