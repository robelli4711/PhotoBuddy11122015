package com.robellistudios.photobuddy;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Toast;


public class MoveMapSettings extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View inf;
    private ImageView mMainImageView;
    private SeekBar seekbar;

    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsedit;

    private OnFragmentInteractionListener mListener;

    RadioButton radioButton_left_up;
    RadioButton radioButton_right_up;
    RadioButton radioButton_right_down;
    RadioButton radioButton_left_down;

    public MoveMapSettings() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static MoveMapSettings newInstance(String param1, String param2) {
        MoveMapSettings fragment = new MoveMapSettings();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        inf = inflater.inflate(R.layout.fragment_move_map_settings, container, false);
        seekbar = (SeekBar)inf.findViewById(R.id.seekBar);
        mMainImageView = (ImageView) inf.findViewById(R.id.imageView4);

        // get Controls
        radioButton_left_up = (RadioButton) inf.findViewById(R.id.radioButton_left_up);
        radioButton_right_up = (RadioButton)inf.findViewById(R.id.radioButton_right_up);
        radioButton_right_down = (RadioButton)inf.findViewById(R.id.radioButton_right_down);
        radioButton_left_down = (RadioButton)inf.findViewById(R.id.radioButton_left_down);

        // get Shared Preferences ready for Read/Write
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        prefsedit = prefs.edit();

        // Setup Dialog
        getDialog().setTitle("Map settings");
        seekbar.setProgress((int) prefs.getFloat("map_settings_opaque", 100));
        mMainImageView.setAlpha((float) prefs.getFloat("map_settings_opaque", 100) / 100);

        switch (prefs.getString("map_location", "")) {
            case "TL":
                radioButton_left_up.setChecked(true);
                break;
            case "TR":
                radioButton_right_up.setChecked(true);
                break;
            case "BL":
                radioButton_left_down.setChecked(true);
                break;
            case "BR":
                radioButton_right_down.setChecked(true);
                break;
        }

        FloatingActionButton fab = (FloatingActionButton) inf.findViewById(R.id.ok_from_move_map_settings);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Map Location on the Image
                if(radioButton_left_up.isChecked())
                    prefsedit.putString("map_location", "TL");

                if(radioButton_right_up.isChecked())
                    prefsedit.putString("map_location", "TR");


                if(radioButton_right_down.isChecked())
                    prefsedit.putString("map_location", "BR");

                if(radioButton_left_down.isChecked())
                    prefs.edit().putString("map_location", "BL");

                prefsedit.commit();

                dismiss();
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mMainImageView.setAlpha((float) seekBar.getProgress() / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                prefsedit.putFloat("map_settings_opaque", (float) seekBar.getProgress());
                prefsedit.commit();
            }
        });

        return inf;
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                new Intent("MMS_FINISHED"));

        mListener = null;
    }

    public ImageView getmMainImageView() {
        return mMainImageView;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
