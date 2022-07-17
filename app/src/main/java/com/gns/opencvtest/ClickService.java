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

import java.nio.ByteBuffer;

public class ClickService extends AccessibilityService {


    private static final String TAG = "ClickService";
    public static ClickService match = null;
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        match = this;
        Log.d(TAG, "onServiceConnected: ok");
    }

    public void start(ActivityResult result){
        if (result.getResultCode()!= Activity.RESULT_OK) return;
        MediaProjection mediaProjection = getSystemService(MediaProjectionManager.class).getMediaProjection(result.getResultCode(),result.getData());
        MediaProjection.Callback callback = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                super.onStop();
            }
        };
        mediaProjection.registerCallback(callback, null);
        ScreenConf.setScreenMetrics(getSystemService(DisplayManager.class));
        ImageReader imageReader = ImageReader.newInstance((int)ScreenConf.realX, (int)ScreenConf.realY, ImageFormat.FLEX_RGBA_8888, 1);
        VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay(
                "OpenCVTest",
                (int)ScreenConf.realX,
                (int)ScreenConf.realY,
                getResources().getConfiguration().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                null,null);

        imageReader.setOnImageAvailableListener(imageReader1 -> {
            Image image = imageReader1.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
            //showImage(bitmapImage);
            Log.d(TAG, "start: test is working");
        },null);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        showNotification();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    private void showNotification(){
        int flag;
        PendingIntent pendingIntent;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("reback",true);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R){
            flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        }else {
            flag = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        pendingIntent = PendingIntent.getActivity(this, 987654322, notificationIntent,flag);
        Notification notification;
        Notification.Builder builder;
        String CHANNEL_ID = "OpenCVTest";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(this,CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setSubText("SubText");
        builder.setContentTitle("ContentTitle");
        builder.setOnlyAlertOnce(true);
        builder.setContentIntent(pendingIntent);
        notification = builder.build();
        startForeground(2,notification);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }
}
