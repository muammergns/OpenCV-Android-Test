package com.gns.opencvtest;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.util.Locale;

public class ScreenConf {
    private static final String TAG = "ScreenConf: ";

    public static float realX;
    public static float realY;

    public static float readX;
    public static float readY;

    public static float offsetX;
    public static float offsetY;

    public static boolean isPortrait;


    public static void setScreenMetrics(DisplayManager displayManager){
        Log.d(TAG, "setScreenMetrics: ");
        if (displayManager==null){
            Log.d(TAG, "setScreenMetrics: displayManager is null");
            return;
        }
        if (displayManager.getDisplays().length==0){
            Log.d(TAG, "setScreenMetrics: displays is empty");
            return;
        }
        Display display = displayManager.getDisplays()[0];
        Point pointRealSize = new Point();
        Point pointCurrentSmall = new Point();
        Point pointCurrentLarge = new Point();

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            display.getRealSize(pointRealSize);
            readX = pointRealSize.x;
            readY = pointRealSize.y;
            Log.d(TAG, ": *******************************");
            Log.d(TAG, "Readed size: x: "+readX+" y: "+readY);

            display.getCurrentSizeRange(pointCurrentSmall,pointCurrentLarge);
            Log.d(TAG, "getCurrentSizeRange: small x: "+pointCurrentSmall.x+" y: "+pointCurrentSmall.y);
            Log.d(TAG, "getCurrentSizeRange: large x: "+pointCurrentLarge.x+" y: "+pointCurrentLarge.y);

            Display.Mode[] modes = display.getSupportedModes();
            for (Display.Mode modex : modes){
                if (pointCurrentSmall.x == modex.getPhysicalWidth()){
                    if (readY>readX){
                        offsetX = (float) modex.getPhysicalWidth()/pointRealSize.x;
                        offsetY = (float) modex.getPhysicalHeight()/pointRealSize.y;
                        isPortrait=true;
                    }else {
                        offsetX = (float) modex.getPhysicalWidth()/pointRealSize.y;
                        offsetY = (float) modex.getPhysicalHeight()/pointRealSize.x;
                        isPortrait=false;
                    }

                    Log.d(TAG, "offset x: "+offsetX+" y: "+ offsetY);
                    realX = modex.getPhysicalWidth();
                    realY = modex.getPhysicalHeight();
                    Log.d(TAG, "display real: x: "+realX+" y: "+realY);
                }
                Log.d(TAG, "display modes: x: "+modex.getPhysicalWidth()+" y: "+modex.getPhysicalHeight());
                Log.d(TAG, "setScreenMetrics: isPortrait:"+isPortrait);
            }
            Log.d(TAG, ": *******************************");

    }

    public static final int FLAG_TOUCHABLE = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ;
    public static final int FLAG_NOT_TOUCHABLE =
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    public static final int FLAG_CAN_FOCUS = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL ;

    public static WindowManager.LayoutParams getLayoutParams(){
        int my_flag_type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { my_flag_type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; }
        else { my_flag_type = WindowManager.LayoutParams.TYPE_PHONE; }
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                my_flag_type,
                FLAG_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
    }

    public static WindowManager.LayoutParams getLayoutParams(int w, int h){
        int my_flag_type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { my_flag_type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; }
        else { my_flag_type = WindowManager.LayoutParams.TYPE_PHONE; }
        return new WindowManager.LayoutParams(
                w,
                h,
                my_flag_type,
                FLAG_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
    }

    public static WindowManager.LayoutParams getLayoutParams(int w, int h, int flag){
        int my_flag_type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { my_flag_type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; }
        else { my_flag_type = WindowManager.LayoutParams.TYPE_PHONE; }
        return new WindowManager.LayoutParams(
                w,
                h,
                my_flag_type,
                flag,
                PixelFormat.TRANSLUCENT);
    }

    public static void changeLocale(int localed, android.content.res.Resources resources){
        Locale locale;
        switch (localed){
            default:
                locale = Locale.getDefault();
                break;
            case 1:
                locale = new Locale("en");
                break;
            case 2:
                locale = new Locale("tr");
                break;
            case 3:
                locale = new Locale("ru");
                break;
            case 4:
                locale = new Locale("ar");
                break;
        }
        Configuration config = new Configuration(resources.getConfiguration());
        config.locale = locale ;
        config.setLayoutDirection(locale);
        Locale.setDefault(locale);
        resources.updateConfiguration(config,resources.getDisplayMetrics());
    }

    public static int dpFromPx(Context context, int px) {
        return Float.valueOf(px / context.getResources().getDisplayMetrics().density).intValue();
    }

    public static int pxFromDp(Context context, int dp) {
        return Float.valueOf(dp * context.getResources().getDisplayMetrics().density).intValue();
    }
}
