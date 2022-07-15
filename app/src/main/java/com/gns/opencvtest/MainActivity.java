package com.gns.opencvtest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
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
                Mat image = Utils.loadResource(MainActivity.this,R.drawable.myimage,CvType.CV_8UC1);
                Log.d(TAG, "Image: "+image);
                Mat template = Utils.loadResource(MainActivity.this,R.drawable.playstore,CvType.CV_8UC1);
                Log.d(TAG, "Template: "+template);
                Mat result = new Mat(0,0,CvType.CV_8UC1);
                Imgproc.matchTemplate(image,template,result,Imgproc.TM_CCOEFF_NORMED);//
                Log.d(TAG, "Result: "+result);
                Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
                Log.d(TAG, "maxLoc: "+mmr.maxLoc + " maxVal: "+mmr.maxVal);

                Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.myimage);
                Bitmap tmp = BitmapFactory.decodeResource(getResources(),R.drawable.playstore);
                Bitmap tempBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(tempBitmap);
                Log.d(TAG, "Canvas: W:"+canvas.getWidth()+" H:"+canvas.getHeight());
                Paint p = new Paint();
                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.YELLOW);
                p.setStrokeWidth(4);

                Rect rect = new Rect((int)mmr.maxLoc.x,(int)mmr.maxLoc.y,(int)mmr.maxLoc.x+tmp.getWidth(),(int)mmr.maxLoc.y+tmp.getHeight());
                canvas.drawRect(rect,p);
                Rect rect2 = new Rect(4,4,bmp.getWidth()-4,bmp.getHeight()-4);
                canvas.drawRect(rect2,p);
                imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));


            } catch (CvException | IOException e) {
                Log.d(TAG, "Catch: "+e.getMessage());
            }

        });
    }





}