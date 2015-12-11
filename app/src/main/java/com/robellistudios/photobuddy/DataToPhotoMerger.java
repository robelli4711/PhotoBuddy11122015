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

        int left = 500;
        int top = 200;
        int right = 600;
        int bottom = 500;

        // get Settings for Size etc.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        switch (prefs.getString("map_location", "")) {

            case "TL":
                left = 50;
                top = 50;
                break;
            case "TR":
                break;
            case "BL":
                break;
            case "BR":
                break;
        }

        // Merge Images
        Canvas c = new Canvas(layout);
        BitmapDrawable drawable2 = new BitmapDrawable(context.getResources(), layout);
        drawable2.setBounds(left, top, right, bottom);    // TODO dynamically change for the setting values

        // Merge Background with Layout
        Bitmap workingBitmap = Bitmap.createBitmap(background);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas c1 = new Canvas(mutableBitmap);
        drawable2.draw(c1);

        mBitmap = mutableBitmap;
    }
}
