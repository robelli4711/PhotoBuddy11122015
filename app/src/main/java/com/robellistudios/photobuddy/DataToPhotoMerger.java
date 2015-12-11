package com.robellistudios.photobuddy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

public class DataToPhotoMerger extends BitmapDrawable {

    public Bitmap mBitmap;


    DataToPhotoMerger() {
    }

    DataToPhotoMerger(Bitmap background, String text, int height, int width, Matrix matrix) {

        Paint paint = new Paint();
        paint.setTextSize(48);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        width = (int) (paint.measureText(text) + 0.5f); // round
        height = (int) (baseline + paint.descent() + 0.5f);

        Canvas canvas = new Canvas(background);
        canvas.drawText(text, 0, baseline, paint);
        mBitmap = background;
    }


    DataToPhotoMerger(Context context, Bitmap background, TableLayout layout, int height, int width, Matrix matrix) {

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


    DataToPhotoMerger(final Context context, Bitmap background, Bitmap layout, int height, int width, Matrix matrix) {

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
        Canvas c = new Canvas(layout);
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
