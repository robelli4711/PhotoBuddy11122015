package com.robellistudios.photobuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class GeneralHelpers {

    public ImageView RotateImage(Context context, ImageView img, Uri path) throws IOException {

        InputStream is = context.getContentResolver().openInputStream(path);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        assert is != null;
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, path);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        //------------

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int MAX_IMAGE_DIMENSION = dm.heightPixels;
        //------------


        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(path);
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }

        assert is != null;
        is.close();

    /*
     * if the orientation is not 0 (or -1, which means we don't know), we
     * have to do a rotation.
     */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        img.setImageBitmap(srcBitmap);
        return img;

    }


    public static int getOrientation(Context context, Uri selectedImage)
    {
        int orientation = -1;
        Cursor cursor = context.getContentResolver().query(selectedImage,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        assert cursor != null;
        if (cursor.getCount() != 1)
            return orientation;

        cursor.moveToFirst();
        orientation = cursor.getInt(0);
        cursor.close(); // ADD THIS LINE
        return orientation;
    }


    /**
     * Get the last know Position from Shared Preferences
     * The ArrayList will return
     * 0. Latitude
     * 1. Longitude
     * 2. Altitude
     * 3. Bearing
     * 4. Accuracy
     */
    public ArrayList getLastLocation(Context context) {

        ArrayList<Float> al = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        al.add(prefs.getFloat("geo_maps_lat", 0));   // Latitude
        al.add(prefs.getFloat("geo_maps_lon",0));   // Longitude
        al.add(prefs.getFloat("geo_maps_alt",0));   // Altitude
        al.add(prefs.getFloat("geo_maps_bearing",0));   // Bearing
        al.add(prefs.getFloat("geo_maps_accuracy",0));   // Accuracy

        return al;
   }
}
