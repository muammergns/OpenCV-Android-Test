package com.gns.opencvtest;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Path;
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
    public static ClickService click = null;
    public static boolean isServiceEnable = false;
    @Override
    protected void onServiceConnected() {
        //todo click event
        click = this;
        isServiceEnable = true;
        Log.d(TAG, "onServiceConnected: ok");
        super.onServiceConnected();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        click = null;
        isServiceEnable = false;
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void play(Path path, long _duration){
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        GestureDescription description = gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, _duration, _duration)).build();
        boolean played = dispatchGesture(description, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "onCompleted: click");
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG, "onCancelled: click");
                super.onCancelled(gestureDescription);
            }
        }, null);
        Log.d(TAG, "play: "+played);
    }



    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }
}
