package com.robellistudios.photobuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import com.robellistudios.photobuddy.R;
import java.util.zip.Inflater;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeatherLayoutChooser.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WeatherLayoutChooser#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeatherLayoutChooser extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private CheckBox cb2;
    private CheckBox cb3;
    private CheckBox cb4;
    private FrameLayout me;

    SharedPreferences prefs;

    private OnFragmentInteractionListener mListener;

    public WeatherLayoutChooser() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WeatherLayoutChooser.
     */
    // TODO: Rename and change types and number of parameters
    public static WeatherLayoutChooser newInstance(String param1, String param2) {
        WeatherLayoutChooser fragment = new WeatherLayoutChooser();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        final SharedPreferences.Editor prefsedit = prefs.edit();

        // Inflate the layout for this fragment
        View inf = inflater.inflate(R.layout.fragment_weather_layout_chooser, container, false);

        cb2 = (CheckBox) inf.findViewById(R.id.checkBox2);
        cb3 = (CheckBox) inf.findViewById(R.id.checkBox3);
        cb4 = (CheckBox) inf.findViewById(R.id.checkBox4);
        me = (FrameLayout) inf.findViewById(R.id.weatherlayoutchooser_content);


        FloatingActionButton fab = (FloatingActionButton) inf.findViewById(R.id.weatherlayoutchooser_ok);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                me.setVisibility(View.INVISIBLE);

                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                new Intent("WEATHERCHOOSER_FINISHED"));

            }
        });


        cb2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setCheckboxes(true, false, false);
                prefsedit.putString("weather_layout", "layout2");
                prefsedit.commit();
            }

        });

        cb3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setCheckboxes(false, true, false);
                prefsedit.putString("weather_layout", "layout3");
                prefsedit.commit();
            }
        });

        cb4.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setCheckboxes(false, false, true);
                prefsedit.putString("weather_layout", "layout4");
                prefsedit.commit();
            }
        });

        return inf;
    }

    // TODO: Rename method, update argument and hook method into UI event
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
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setCheckboxes(boolean b2, boolean b3, boolean b4) {

        cb2.setChecked(b2);
        cb3.setChecked(b3);
        cb4.setChecked(b4);
    }
}
