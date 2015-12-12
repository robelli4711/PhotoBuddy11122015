package com.robellistudios.photobuddy;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class helper_SaveLoadImage {

    public void saveImage(Context context, ImageView imageview)  {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());

        OutputStream fOut = null;

        String path = Environment.getExternalStorageDirectory().toString() + "/PhotoBuddy";
        File mediaDir = new File(path);
        if (!mediaDir.exists()){
            mediaDir.mkdir();
        }

        File file = new File(path, "/PhotoBuddy"+"_"+ currentDateandTime + ".jpg"); // the File to save to
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        imageview.buildDrawingCache();
        Bitmap pictureBitmap = imageview.getDrawingCache();
        pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), "Photo Buddy");
            new SingleMediaScanner(context, file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Toast.makeText(context, "Image saved in " + file.getAbsoluteFile(), Toast.LENGTH_LONG).show();
    }


//==============
    public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

        private MediaScannerConnection mMs;
        private File mFile;

        public SingleMediaScanner(Context context, File f) {
            mFile = f;
            mMs = new MediaScannerConnection(context, this);
            mMs.connect();
        }

        @Override
        public void onMediaScannerConnected() {
            mMs.scanFile(mFile.getAbsolutePath(), null);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            mMs.disconnect();
        }

    }
}
