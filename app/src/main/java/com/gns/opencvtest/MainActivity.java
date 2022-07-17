package com.gns.opencvtest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;


public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "MainActivity";


    MediaProjectionManager mediaProjectionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        Button button1 = findViewById(R.id.button2);
        initTestV1();


        button.setOnClickListener(view -> {
            projection.launch(mediaProjectionManager.createScreenCaptureIntent());
            //matchTestV1();
        } );

        button1.setOnClickListener(view -> {
            showImage(ScreenshotService.bitmap);
        });






    }


    ActivityResultLauncher<Intent> projection = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode()==RESULT_OK){
            Intent i=
                    new Intent(this, ScreenshotService.class)
                            .putExtra(ScreenshotService.EXTRA_RESULT_CODE, result.getResultCode())
                            .putExtra(ScreenshotService.EXTRA_RESULT_INTENT, result.getData());

            startService(i);
        }
    });


    //ImageView imageView;
    Mat templateMat;
    private void initTestV1(){
        OpenCVLoader.initDebug();
        //imageView = findViewById(R.id.imageView);
        templateMat = TemplateMatching.resourceToMat(this, R.drawable.playstore);
        mediaProjectionManager = getSystemService(MediaProjectionManager.class);
    }
    private void matchTestV1(){
        //@DrawableRes int[] tmps = {R.drawable.game1,R.drawable.game2,R.drawable.playstore,R.drawable.gos};
        long startMillis = System.currentTimeMillis();
        Bitmap bmp = TemplateMatching.takeScreenshot(this);
        Core.MinMaxLocResult mmr = TemplateMatching.match(TemplateMatching.bitmapToMat(bmp),templateMat);
        if (mmr.maxVal>0.45)
            showImage(TemplateMatching.getFounded(this, mmr, bmp, R.drawable.playstore));
        Log.d(TAG, "onCreate: "+(System.currentTimeMillis()-startMillis));
    }
    private void showImage(Drawable drawable){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setNeutralButton("Exit",null);
        ImageView iv = new ImageView(this);
        ConstraintLayout cl = new ConstraintLayout(this);
        iv.setAdjustViewBounds(true);
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        iv.setImageDrawable(drawable);
        cl.addView(iv);
        adb.setView(cl);
        adb.show();
    }
    private void showImage(Bitmap bitmap){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setNeutralButton("Exit",null);
        ImageView iv = new ImageView(this);
        ConstraintLayout cl = new ConstraintLayout(this);
        iv.setAdjustViewBounds(true);
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        iv.setImageBitmap(bitmap);
        cl.addView(iv);
        adb.setView(cl);
        adb.show();
    }
}