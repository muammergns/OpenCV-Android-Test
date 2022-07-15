package com.gns.opencvtest;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;


public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "MainActivity";
    ImageView imageView;
    Mat imageMat, templateMat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();
        Button button = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        templateMat = TemplateMatching.resourceToMat(this, R.drawable.playstore);
        button.setOnClickListener(view -> {
            //@DrawableRes int[] tmps = {R.drawable.game1,R.drawable.game2,R.drawable.playstore,R.drawable.gos};
            long startMillis = System.currentTimeMillis();
            Bitmap bmp = takeScreenshot();
            Core.MinMaxLocResult mmr = TemplateMatching.match(TemplateMatching.bitmapToMat(bmp),templateMat);
            if (mmr.maxVal>0.45)
                imageView.setImageDrawable(TemplateMatching.getFounded(this, mmr, bmp, R.drawable.playstore));
            Log.d(TAG, "onCreate: "+(System.currentTimeMillis()-startMillis));
        });
    }

    private Bitmap takeScreenshot(){
        View v1 = getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);
        return bitmap;
    }




}