package com.robellistudios.photobuddy;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.preference.PreferenceManager;
import android.widget.CheckBox;

import java.lang.reflect.Array;

public class EnrichmentChooser extends Dialog implements
        android.view.View.OnClickListener {

    public static final String PREFS_NAME = "com.robellistudios.photobuddy.app";

    public Activity c;
    public Dialog d;
    public Button yes, no;

    private Button mOkButton;

    public EnrichmentChooser(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.enrichment_chooser_layout);

        // get the choices from Shared Preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        {
            // set Geo Map checked?
            CheckBox cb = (CheckBox) findViewById(R.id.checkBox_GeoMap);
            cb.setChecked(prefs.getBoolean("geo_maps",false));

            // set Weather Map checked?
            cb = (CheckBox) findViewById(R.id.checkBox_Weather);
            cb.setChecked(prefs.getBoolean("weather_maps",false));
        }

        // Setup button listener
        mOkButton = (Button) findViewById(R.id.ok_button);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // save the choice in Shared Preferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                {
                    // is Geo Map checked?
                    CheckBox cb = (CheckBox) findViewById(R.id.checkBox_GeoMap);
                    if (cb.isChecked())
                        prefs.edit().putBoolean("geo_maps", true).apply();
                    else
                        prefs.edit().putBoolean("geo_maps", false).apply();

                    // is Weather Map checked?
                    cb = (CheckBox) findViewById(R.id.checkBox_Weather);
                    if (cb.isChecked())
                        prefs.edit().putBoolean("weather_maps", true).apply();
                    else
                        prefs.edit().putBoolean("weather_maps", false).apply();
                }

                // close the dialog
                dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }


    public void setOnDismissListener() {    }
}
