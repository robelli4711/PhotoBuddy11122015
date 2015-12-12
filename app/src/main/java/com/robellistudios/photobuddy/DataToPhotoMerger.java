package com.robellistudios.photobuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.widget.TableLayout;
import android.widget.Toast;

public class DataToPhotoMerger extends BitmapDrawable {

    public Bitmap mBitmap;

    DataToPhotoMerger(Context context, Bitmap background, TableLayout layout, int width, int height, Matrix matrix) {

        // get Layout and produce a Bitmap
        layout.measure(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        Bitmap b = Bitmap.createBitmap(layout.getMeasuredWidth(), layout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(b);
        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());
        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        layout.draw(c);
        BitmapDrawable drawable2 = new BitmapDrawable(context.getResources(), b);
        drawable2.setBounds(150, 150, 500, 500);

        // Merge Background with Layout
        Bitmap workingBitmap = Bitmap.createBitmap(background);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas c1 = new Canvas(mutableBitmap);
        drawable2.draw(c1);

        mBitmap = mutableBitmap;
    }


    DataToPhotoMerger(final Context context, Bitmap background, Bitmap layout) {

        // get Settings for Size etc.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int left = 500;
        int top = 200;
        int right = 0;
        int bottom = 0;
        int diff = 0;

        switch(prefs.getString("map_size", "")) {
            case "big":
                right = 900;
                bottom = 900;
                diff = 650;
                break;
            case "medium":
                right = 600;
                bottom = 600;
                diff = 450;
                break;
            case "small":
                right = 300;
                bottom = 300;
                diff = 250;
                break;
        }

        switch (prefs.getString("map_location", "")) {

            case "TL":
                left = 50;
                top = 50;
                break;
            case "TR":
                left = background.getScaledWidth(context.getResources().getDisplayMetrics().densityDpi) - diff;
                top = 50;
                right = left + diff;
                break;
            case "BL":
                left = 50;
                top = background.getScaledHeight(context.getResources().getDisplayMetrics().densityDpi) - diff;
                bottom = (background.getScaledHeight(context.getResources().getDisplayMetrics().densityDpi) - 50);
                break;
            case "BR":
                top = background.getScaledHeight(context.getResources().getDisplayMetrics().densityDpi) - diff;
                left = background.getScaledWidth(context.getResources().getDisplayMetrics().densityDpi) - diff;
                bottom = (background.getScaledHeight(context.getResources().getDisplayMetrics().densityDpi) - 50);
                right = left + 450;
                break;
        }

        // Merge Images
        if(layout == null) {
            Toast.makeText(context, "no Map Snapshot available", Toast.LENGTH_LONG).show();
            mBitmap = background;
            return;
        }

        BitmapDrawable drawable2 = new BitmapDrawable(context.getResources(), layout);
        drawable2.setBounds(left, top, right, bottom);

        int i = (int)prefs.getFloat("map_settings_opaque", 100);        // apply Alpha value
        drawable2.setAlpha((int) (i*2.55));

        // Merge Background with Layout
        Bitmap workingBitmap = Bitmap.createBitmap(background);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas c1 = new Canvas(mutableBitmap);
        drawable2.draw(c1);

        mBitmap = mutableBitmap;
    }
}
