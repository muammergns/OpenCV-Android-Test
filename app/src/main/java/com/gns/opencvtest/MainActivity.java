package com.gns.opencvtest;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();
        Button button = findViewById(R.id.button);
        ImageView imageView = findViewById(R.id.imageView);
        button.setOnClickListener(view -> {

            try {
                Core.MinMaxLocResult mmr = getResultFromBitmap(R.drawable.playstore);
                Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.myimage);
                Bitmap tempBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(tempBitmap);
                Log.d(TAG, "Canvas: W:"+canvas.getWidth()+" H:"+canvas.getHeight());
                Paint p = new Paint();
                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.YELLOW);
                p.setStrokeWidth(4);

                canvas.drawRect(convertToRect(mmr,1f),p);
                Rect rect2 = new Rect(4,4,bmp.getWidth()-4,bmp.getHeight()-4);
                canvas.drawRect(rect2,p);
                imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));


            } catch (CvException e) {
                Log.d(TAG, "Catch: "+e.getMessage());
            }

        });
    }

    private Core.MinMaxLocResult getResultFromBitmap(@DrawableRes int drawableId){
        try {

            Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.myimage);
            Bitmap srcbmp = bmp.copy(Bitmap.Config.ARGB_8888,true);
            Mat image = new Mat();
            Utils.bitmapToMat(srcbmp,image);
            Log.d(TAG, "Image: "+image);

            Bitmap bmp2 = BitmapFactory.decodeResource(getResources(),drawableId);
            Bitmap srcbmp2 = bmp2.copy(Bitmap.Config.ARGB_8888,true);
            Mat template = new Mat();
            Utils.bitmapToMat(srcbmp2,template);
            Log.d(TAG, "Template: "+template);

            Mat result = new Mat(0,0,CvType.CV_8UC4);
            Imgproc.matchTemplate(image,template,result,5);//
            Log.d(TAG, "Result: "+result);
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
            Log.d(TAG, "maxLoc: "+mmr.maxLoc + " maxVal: "+mmr.maxVal);
            return mmr;
        } catch (Exception e) {
            Log.d(TAG, "Catch: "+e.getMessage());
        }
        return new Core.MinMaxLocResult();
    }

    private Core.MinMaxLocResult getResultFromResources(@DrawableRes int drawableId){
        try {
            Mat image = Utils.loadResource(MainActivity.this,R.drawable.myimage, CvType.CV_8UC1);
            Log.d(TAG, "Image: "+image);
            Mat template = Utils.loadResource(MainActivity.this,drawableId,CvType.CV_8UC1);
            Log.d(TAG, "Template: "+template);
            Mat result = new Mat(0,0,CvType.CV_8UC1);
            Imgproc.matchTemplate(image,template,result,Imgproc.TM_CCOEFF_NORMED);//
            Log.d(TAG, "Result: "+result);
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
            Log.d(TAG, "maxLoc: "+mmr.maxLoc + " maxVal: "+mmr.maxVal);
            return mmr;
        } catch (IOException e) {
            Log.d(TAG, "Catch: "+e.getMessage());
        }
        return new Core.MinMaxLocResult();
    }

    private Rect convertToRect(Core.MinMaxLocResult mmr,float mul){

        return new Rect(
                Double.valueOf(mmr.maxLoc.x*mul).intValue(),
                Double.valueOf(mmr.maxLoc.y*mul).intValue(),
                Double.valueOf(mmr.maxLoc.x*mul).intValue()+200,
                Double.valueOf(mmr.maxLoc.y*mul).intValue()+200
        );
    }




}