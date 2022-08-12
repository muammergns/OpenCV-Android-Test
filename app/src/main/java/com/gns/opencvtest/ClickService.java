package com.gns.opencvtest;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.activity.result.ActivityResult;

import com.gns.opencvtest.utils.ScreenConf;

import java.nio.ByteBuffer;

public class ClickService extends AccessibilityService {


    private static final String TAG = "ClickService";
    public static ClickService match = null;
    @Override
    protected void onServiceConnected() {
        //todo click event
        super.onServiceConnected();
        match = this;
        Log.d(TAG, "onServiceConnected: ok");
    }




    @Override
    public void onCreate() {
        super.onCreate();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }
}
