package com.gns.opencvtest.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageStorageManager {
    public static void saveToInternalStorage(Context context, Bitmap bitmap, String fileName){
        try {
            FileOutputStream stream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static Bitmap getImageFromInternalStorage(Context context, String fileName){
        File file = context.getFilesDir();
        File file1 = new File(file,fileName);
        try {
            return BitmapFactory.decodeStream(new FileInputStream(file1));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
