package com.gns.opencvtest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.gns.opencvtest.matching.TemplateMatching;
import com.gns.opencvtest.screenshot.ScreenshotService;
import com.gns.opencvtest.utils.ImageStorageManager;
import com.google.android.material.snackbar.Snackbar;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.IOException;


public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "MainActivity";

    ImageView imageView;
    CheckBox save,beep,click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        Button button1 = findViewById(R.id.button2);
        Button button2 = findViewById(R.id.button3);
        save = findViewById(R.id.checkBox);
        beep = findViewById(R.id.checkBox2);
        click = findViewById(R.id.checkBox3);
        imageView = findViewById(R.id.imageView);
        OpenCVLoader.initDebug();

        button.setOnClickListener(view -> {
            mediaProjectionManagerLauncher.launch(getSystemService(MediaProjectionManager.class).createScreenCaptureIntent());
        } );

        button1.setOnClickListener(view -> {
            Bitmap bitmap = ImageStorageManager.getImageFromInternalStorage(this,"deneme");
            showImage(bitmap);
        });

        button2.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Need Permission",Snackbar.LENGTH_INDEFINITE).setAction("Ok",view1 -> {
                        readExternalStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }).show();
                }else {
                    readExternalStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }else {
                Intent intentToGalery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galeryLauncher.launch(intentToGalery);
            }
        });

        Bitmap bitmap = ImageStorageManager.getImageFromInternalStorage(this,"template");
        if (bitmap != null){
            if (bitmap.getWidth() > 0 && bitmap.getHeight() > 0){
                imageView.setImageBitmap(bitmap);
            }
        }

        click.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                if (!ClickService.isServiceEnable){
                    goAccess();
                }
            }
        });
    }

    private void goAccess(){
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        Bundle bundle = new Bundle();
        String showArgs = getApplicationInfo().packageName + "/" + ClickService.class.getName();
        String EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";
        bundle.putString(EXTRA_FRAGMENT_ARG_KEY, showArgs);
        intent.putExtra(EXTRA_FRAGMENT_ARG_KEY, showArgs);
        String EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args";
        intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle);
        access.launch(intent);
    }

    ActivityResultLauncher<Intent> access = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (!ClickService.isServiceEnable){
            click.setChecked(false);
        }
    });

    ActivityResultLauncher<Intent> galeryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK){
                        Intent intentFromResult = result.getData();
                        if (intentFromResult != null){
                            Uri imageUri = intentFromResult.getData();
                            try {
                                ImageDecoder.Source source;
                                Bitmap bitmap;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                    source = ImageDecoder.createSource(getContentResolver(),imageUri);
                                    bitmap = ImageDecoder.decodeBitmap(source);
                                }else {
                                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                }
                                ImageStorageManager
                                        .saveToInternalStorage(MainActivity.this,bitmap,"template");
                                imageView.setImageBitmap(ImageStorageManager
                                        .getImageFromInternalStorage(MainActivity.this,"template"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
    );

    ActivityResultLauncher<String> readExternalStorageLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result){
                        Intent intentToGalery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galeryLauncher.launch(intentToGalery);
                    }else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    ActivityResultLauncher<Intent> mediaProjectionManagerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode()==RESULT_OK){
            Intent i=
                    new Intent(MainActivity.this, ScreenshotService.class)
                            .putExtra(ScreenshotService.EXTRA_KEY_BEEP,beep.isChecked())
                            .putExtra(ScreenshotService.EXTRA_SAVE_PIC,save.isChecked())
                            .putExtra(ScreenshotService.EXTRA_CLICK,click.isChecked())
                            .putExtra(ScreenshotService.EXTRA_RESULT_CODE, result.getResultCode())
                            .putExtra(ScreenshotService.EXTRA_RESULT_INTENT, result.getData());

            startService(i);
            Log.d(TAG, "Service çalıştırldı ok: ");
        }
    });

    private void showImage(Bitmap bitmap){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setNeutralButton("Exit",null);
        ImageView iv = new ImageView(this);
        ConstraintLayout cl = new ConstraintLayout(this);
        iv.setAdjustViewBounds(true);
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        iv.setImageBitmap(bitmap);
        cl.addView(iv);
        adb.setView(cl);
        adb.show();
    }
}