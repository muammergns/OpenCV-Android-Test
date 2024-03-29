package com.gns.opencvtest.matching;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.DrawableRes;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class TemplateMatching {
    private static final String TAG = "TemplateMatching";

    public static Core.MinMaxLocResult match(Mat image, Mat template){
        Mat result = new Mat(0,0, CvType.CV_8UC4);
        Imgproc.matchTemplate(image,template,result,5);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Log.d(TAG, "maxLoc: "+mmr.maxLoc + " maxVal: "+mmr.maxVal);
        return mmr;
    }

    public static Core.MinMaxLocResult matchNormal(Mat image, Mat template){
        Mat result = new Mat(0,0, CvType.CV_8UC4);
        Imgproc.matchTemplate(image,template,result,5);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Log.d(TAG, "maxLoc: "+mmr.maxLoc + " maxVal: "+mmr.maxVal);
        return mmr;
    }

    public static Mat resourceToMat(Context c, @DrawableRes int drawableId){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap decodeResource = BitmapFactory.decodeResource(c.getResources(),drawableId,options);
        Bitmap copy = decodeResource.copy(Bitmap.Config.ARGB_8888,true);
        Mat mat = new Mat();
        Utils.bitmapToMat(copy,mat);
        Log.d(TAG, "resourceToMat: "+mat);
        return mat;
    }
    public static Mat bitmapToMat(Bitmap bitmap){
        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        Mat mat = new Mat();
        Utils.bitmapToMat(copy,mat);
        Log.d(TAG, "resourceToMat: "+mat);
        return mat;
    }

    public static Drawable getFounded(Context c, Core.MinMaxLocResult mmr, Bitmap image, Mat mat){
        Bitmap imageBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(imageBitmap);
        Log.d(TAG, "Canvas: W:"+canvas.getWidth()+" H:"+canvas.getHeight());
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.YELLOW);
        p.setStrokeWidth(4);
        Rect rect = new Rect(
                Double.valueOf(mmr.maxLoc.x).intValue(),
                Double.valueOf(mmr.maxLoc.y).intValue(),
                Double.valueOf(mmr.maxLoc.x).intValue()+mat.cols(),
                Double.valueOf(mmr.maxLoc.y).intValue()+mat.rows()
        );
        canvas.drawRect(rect,p);
        return new BitmapDrawable(c.getResources(), imageBitmap);
    }

    public static Bitmap takeScreenshot(Activity c){
        View v1 = c.getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);
        return bitmap;
    }
}
