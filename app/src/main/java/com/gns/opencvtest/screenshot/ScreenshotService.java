package com.gns.opencvtest.screenshot;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.ToneGenerator;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gns.opencvtest.BuildConfig;
import com.gns.opencvtest.matching.TemplateMatching;
import com.gns.opencvtest.utils.ImageStorageManager;
import com.gns.opencvtest.MainActivity;
import com.gns.opencvtest.R;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;

public class ScreenshotService extends Service {
    private static final String TAG = "ScreenshotService";

    private static final String CHANNEL_WHATEVER="channel_whatever";
    private static final int NOTIFY_ID=9906;

    public static final String EXTRA_RESULT_CODE="resultCode";
    public static final String EXTRA_RESULT_INTENT="resultIntent";
    public static final String EXTRA_SAVE_PIC = "savePic";
    public static final String EXTRA_KEY_BEEP = "keyBeep";
    public static final String EXTRA_CLICK = "clickPosition";

    //todo add stop for lock and call

    static final String ACTION_RECORD=
            BuildConfig.APPLICATION_ID+".RECORD";
    static final String ACTION_SHUTDOWN=
            BuildConfig.APPLICATION_ID+".SHUTDOWN";
    static final String ACTION_EXIT=
            BuildConfig.APPLICATION_ID+".EXIT";


    static final int VIRT_DISPLAY_FLAGS=
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    private MediaProjection projection;
    private VirtualDisplay vdisplay;

    final private HandlerThread handlerThread=
            new HandlerThread(getClass().getSimpleName(),
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);

    final private ToneGenerator beeper=
            new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

    Mat templateMat;
    long millis = 0;
    boolean keyBeep;
    boolean savePic;
    boolean clickPosition;
    private int resultCode;
    private Intent resultData;

    @Override
    public void onCreate() {
        super.onCreate();
        Bitmap tmpBitmap = ImageStorageManager.getImageFromInternalStorage(this,"template");
        assert tmpBitmap != null;
        templateMat = TemplateMatching.bitmapToMat(tmpBitmap);
        Log.d(TAG, "onCreate: templateMat: width:"+templateMat.width()+" height:"+templateMat.height());
        Log.d(TAG, "onCreate: templateMat: cols:"+templateMat.cols()+" rows:"+templateMat.rows());
        Log.d(TAG, "onCreate: ok");
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        if (i.getAction()==null) {
            resultCode=i.getIntExtra(EXTRA_RESULT_CODE, 1337);
            resultData=i.getParcelableExtra(EXTRA_RESULT_INTENT);
            savePic = i.getBooleanExtra(EXTRA_SAVE_PIC,false);
            keyBeep = i.getBooleanExtra(EXTRA_KEY_BEEP,false);
            clickPosition = i.getBooleanExtra(EXTRA_CLICK,false);
            foregroundify();
            MediaProjectionManager mgr = getSystemService(MediaProjectionManager.class);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            projection= mgr.getMediaProjection(resultCode, resultData);
            MediaProjection.Callback cb=new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    stopCapture();
                }
            };
            projection.registerCallback(cb, handler);

            Display display=getSystemService(WindowManager.class).getDefaultDisplay();
            Point size=new Point();
            display.getRealSize(size);
            this.width=size.x;
            this.height=size.y;


        }else {
            runAction(i.getAction());
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


    public void processImage(Bitmap bmp) {
        Log.d(TAG, "run: bitmap width: "+bmp.getWidth() + " height: "+ bmp.getHeight());
        Core.MinMaxLocResult mmr = TemplateMatching.match(TemplateMatching.bitmapToMat(bmp),templateMat);
        Log.d(TAG, "run: found max value: "+mmr.maxVal);
        Log.d(TAG, "run: found millis: "+(System.currentTimeMillis()-millis));
        if (mmr.maxVal>0.95){
            if (savePic){
                Drawable drawable = TemplateMatching.getFounded(ScreenshotService.this, mmr, bmp, templateMat);
                ImageStorageManager.saveToInternalStorage(ScreenshotService.this,drawableToBitmap(drawable),"deneme");
                Log.d(TAG, "run: resim dosya olarak kaydedildi ok");
                Log.d(TAG, "run: save millis: "+(System.currentTimeMillis()-millis));
            }
            if (keyBeep){
                beeper.startTone(ToneGenerator.TONE_PROP_BEEP);//TONE_PROP_ACK TONE_PROP_NACK
            }
            if (clickPosition){
                //todo click
            }
        }
        Log.d(TAG, "processImage: ok");
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void stopCapture() {
        if (vdisplay!=null){
            vdisplay.release();
            vdisplay=null;
        }
        if (imageReader!=null){
            imageReader.close();
            imageReader=null;
        }
        beeper.startTone(ToneGenerator.TONE_PROP_NACK);
        Log.d(TAG, "stopCapture: ok");
    }

    private int width;
    private int height;
    private ImageReader imageReader;
    Handler handler;

    @SuppressLint("WrongConstant")
    private void startCapture() {
        //todo change width and height if orientation changed
        //todo use screenconf class
        imageReader= ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 10);
        imageReader.setOnImageAvailableListener(listener, handler);
        vdisplay=projection.createVirtualDisplay("andshooter",
                width, height,
                getResources().getDisplayMetrics().densityDpi,
                VIRT_DISPLAY_FLAGS, imageReader.getSurface(), null, handler);
        Log.d(TAG, "startCapture: ok");
    }

    Bitmap latestBitmap=null;
    ImageReader.OnImageAvailableListener listener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Log.d(TAG, "run: loop millis: "+(System.currentTimeMillis()-millis));
            millis = System.currentTimeMillis();
            final Image image=imageReader.acquireLatestImage();
            if (image!=null) {
                Image.Plane[] planes=image.getPlanes();
                ByteBuffer buffer=planes[0].getBuffer();
                int pixelStride=planes[0].getPixelStride();
                int rowStride=planes[0].getRowStride();
                int rowPadding=rowStride - pixelStride * width;
                int bitmapWidth=width + rowPadding / pixelStride;
                if (latestBitmap == null ||
                        latestBitmap.getWidth() != bitmapWidth ||
                        latestBitmap.getHeight() != height) {
                    if (latestBitmap != null) {
                        latestBitmap.recycle();
                    }
                    latestBitmap=Bitmap.createBitmap(bitmapWidth,
                            height, Bitmap.Config.ARGB_8888);
                }
                latestBitmap.copyPixelsFromBuffer(buffer);
                image.close();
                Bitmap cropped=Bitmap.createBitmap(latestBitmap, 0, 0,
                        width, height);
                processImage(cropped);
                Log.d(TAG, "onImageAvailable: resim bulundu ve belleğe alındı");
            }
        }
    };

    private void foregroundify() {
        NotificationManager mgr= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O && mgr.getNotificationChannel(CHANNEL_WHATEVER)==null)
            mgr.createNotificationChannel(new NotificationChannel(CHANNEL_WHATEVER, "Whatever", NotificationManager.IMPORTANCE_DEFAULT));
        NotificationCompat.Builder b= new NotificationCompat.Builder(this, CHANNEL_WHATEVER);
        b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL);
        b.setSilent(true);
        b.setContentTitle(getString(R.string.app_name)).setSmallIcon(R.mipmap.ic_launcher).setTicker(getString(R.string.app_name));
        b.addAction(R.mipmap.ic_launcher, "Start", buildPendingIntent(ACTION_RECORD));
        b.addAction(R.mipmap.ic_launcher, "Stop", buildPendingIntent(ACTION_SHUTDOWN));
        b.addAction(R.mipmap.ic_launcher, "Exit", buildPendingIntent(ACTION_EXIT));
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

    private void runAction(String action){
        if (action.equals(ACTION_RECORD)) {
            if (resultData!=null) {
                beeper.startTone(ToneGenerator.TONE_PROP_ACK);
                startCapture();
            } else {
                Intent ui = new Intent(ScreenshotService.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(ui);
                stopForeground(true);
                stopSelf();
            }
        }
        if (action.equals(ACTION_SHUTDOWN)) {
            stopCapture();
        }
        if (action.equals(ACTION_EXIT)) {
            stopForeground(true);
            stopSelf();
        }
    }

}
