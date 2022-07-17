package com.gns.opencvtest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.ToneGenerator;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;

public class ScreenshotService extends Service {

    public static Bitmap bitmap;

    private static final String CHANNEL_WHATEVER="channel_whatever";
    private static final int NOTIFY_ID=9906;

    private static final String TAG = "ScreenshotService";
    static final String EXTRA_RESULT_CODE="resultCode";
    static final String EXTRA_RESULT_INTENT="resultIntent";
    private int resultCode;
    private Intent resultData;

    static final String ACTION_RECORD=
            BuildConfig.APPLICATION_ID+".RECORD";
    static final String ACTION_SHUTDOWN=
            BuildConfig.APPLICATION_ID+".SHUTDOWN";
    static final int VIRT_DISPLAY_FLAGS=
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    final private HandlerThread handlerThread=
            new HandlerThread(getClass().getSimpleName(),
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
    private Handler handler;
    private MediaProjectionManager mgr;
    private WindowManager wmgr;
    private ImageTransmogrifier it;
    final private ToneGenerator beeper=
            new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);


    @Override
    public void onCreate() {
        super.onCreate();
        mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        wmgr=(WindowManager)getSystemService(WINDOW_SERVICE);

        handlerThread.start();
        handler=new Handler(handlerThread.getLooper());
        Log.d(TAG, "onCreate: ok");
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        if (i.getAction()==null) {
            resultCode=i.getIntExtra(EXTRA_RESULT_CODE, 1337);
            resultData=i.getParcelableExtra(EXTRA_RESULT_INTENT);
            foregroundify();
        }
        else if (ACTION_RECORD.equals(i.getAction())) {
            if (resultData!=null) {
                startCapture();
            }
            else {
                Intent ui=
                        new Intent(this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(ui);
            }
        }
        else if (ACTION_SHUTDOWN.equals(i.getAction())) {
            beeper.startTone(ToneGenerator.TONE_PROP_NACK);
            stopForeground(true);
            stopSelf();
        }
        Log.d(TAG, "onStartCommand: ok");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopCapture();
        Log.d(TAG, "onDestroy: ok");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    WindowManager getWindowManager() {
        return(wmgr);
    }

    Handler getHandler() {
        return(handler);
    }

    void processImage(final byte[] png) {
        new Thread() {
            @Override
            public void run() {
                /*
                File output=new File(getExternalFilesDir(null),
                        "screenshot.png");

                try {
                    FileOutputStream fos=new FileOutputStream(output);

                    fos.write(png);
                    fos.flush();
                    fos.getFD().sync();
                    fos.close();

                    MediaScannerConnection.scanFile(ScreenshotService.this,
                            new String[] {output.getAbsolutePath()},
                            new String[] {"image/png"},
                            null);
                    Log.d(TAG, "run: "+output.getAbsolutePath());
                }
                catch (Exception e) {
                    Log.d(TAG, "run: "+e.getMessage());
                    Log.e(getClass().getSimpleName(), "Exception writing out screenshot", e);
                }

                 */
                bitmap = BitmapFactory.decodeByteArray(png, 0, png.length);
                Log.d(TAG, "run: ok");
            }
        }.start();

        beeper.startTone(ToneGenerator.TONE_PROP_ACK);
        stopCapture();
        Log.d(TAG, "processImage: ok");
    }

    private void stopCapture() {
        if (projection!=null) {
            projection.stop();
            vdisplay.release();
            projection=null;
        }
        Log.d(TAG, "stopCapture: ok");
    }

    private void startCapture() {
        projection=mgr.getMediaProjection(resultCode, resultData);
        it=new ImageTransmogrifier(this);

        MediaProjection.Callback cb=new MediaProjection.Callback() {
            @Override
            public void onStop() {
                vdisplay.release();
            }
        };

        vdisplay=projection.createVirtualDisplay("andshooter",
                it.getWidth(), it.getHeight(),
                getResources().getDisplayMetrics().densityDpi,
                VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler);
        projection.registerCallback(cb, handler);
        Log.d(TAG, "startCapture: ok");
    }

    private void foregroundify() {
        NotificationManager mgr=
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O &&
                mgr.getNotificationChannel(CHANNEL_WHATEVER)==null) {
            mgr.createNotificationChannel(new NotificationChannel(CHANNEL_WHATEVER,
                    "Whatever", NotificationManager.IMPORTANCE_DEFAULT));
        }

        NotificationCompat.Builder b=
                new NotificationCompat.Builder(this, CHANNEL_WHATEVER);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        b.setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(getString(R.string.app_name));

        b.addAction(R.mipmap.ic_launcher,
                "Notify Record",
                buildPendingIntent(ACTION_RECORD));

        b.addAction(R.mipmap.ic_launcher,
                "Notify Shutdown",
                buildPendingIntent(ACTION_SHUTDOWN));

        startForeground(NOTIFY_ID, b.build());
    }

    private PendingIntent buildPendingIntent(String action) {
        Intent i=new Intent(this, getClass());

        i.setAction(action);
        int flag;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R){
            flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        }else {
            flag = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        return(PendingIntent.getService(this, 0, i, flag));
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
        startForeground(NOTIFY_ID,notification);
    }

}
