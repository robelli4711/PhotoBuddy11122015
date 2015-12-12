package com.robellistudios.photobuddy;

import android.Manifest;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewOverlay;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
                            implements NavigationView.OnNavigationItemSelectedListener,
                                        WeatherLayoutChooser.OnFragmentInteractionListener,
                                        MoveMapSettings.OnFragmentInteractionListener {

    public static final String APP_TAG = "com.robellistudios.photobuddy"; // Application Tag
    public static final int RESULT_GALLERY = 0; // Result from open the Album (MediaStore)
    public static final int RESULT_CAMERA = 1; // Result from open the Camera
    public static final int LOCATION_REFRESH_TIME = 1000; // refresh time GPS
    public static final int LOCATION_REFRESH_DISTANCE = 10; // refresh distance GPS

    private static final int CONTENT_VIEW_ID = 10101010;

    public static android.support.v4.app.FragmentManager fragmentManager;
    private BroadcastReceiver localBroadcastReceiver;

    LocationManager mLocationManager;
    String city = "Athens,GR";
    private static double mLat;
    private static double mLon;

    private String mTemperature;
    private String mCondition;
    private String mHumidity;
    private String mWind;
    private String mBearing;

    private ImageView mMainImageView;                // Main ImageView
    private ImageView mMainImageView_Save;            // save of the Original Image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup Broadcast Receiver
        localBroadcastReceiver = new LocalBroadcastReceiver();
        IntentFilter filterMMS = new IntentFilter("MMS_FINISHED");
        IntentFilter filterWLC = new IntentFilter("WEATHERCHOOSER_FINISHED");
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver,filterMMS);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver,filterWLC);

        // initialising the object of the FragmentManager.
        fragmentManager = getSupportFragmentManager();

        // Setup GUI
        setContentView(com.robellistudios.photobuddy.R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(com.robellistudios.photobuddy.R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.fragment_geo).setVisibility(View.INVISIBLE);
        findViewById(R.id.start_map).setVisibility(View.INVISIBLE);
        findViewById(R.id.start_weather).setVisibility(View.INVISIBLE);
        findViewById(R.id.fragment_weatherchooser).setVisibility(View.INVISIBLE);

        // get the BackgroudnImage
        mMainImageView = (ImageView) findViewById(com.robellistudios.photobuddy.R.id.imageView2);
        mMainImageView_Save = mMainImageView;       // make a copy of the Original Image
        scaleImage(mMainImageView, 2048);

        // startup GPS to be ready
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);


        // tap start Map Handling
        FloatingActionButton map = (FloatingActionButton) findViewById(R.id.start_map);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setControls(false, false, true, false, false);
            }
        });


        // tap make photo
        FloatingActionButton fab = (FloatingActionButton) findViewById(com.robellistudios.photobuddy.R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "create a photo", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(intent, RESULT_CAMERA);
            }
        });


        // tap get from album action
        FloatingActionButton alb = (FloatingActionButton) findViewById(com.robellistudios.photobuddy.R.id.alb);
        alb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "choose from album", Snackbar.LENGTH_SHORT)
                        .setAction("Action_alb", null).show();

                Intent galleryIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_GALLERY);
            }
        });


        // tap get Weather Chooser
        final FloatingActionButton fragmentWeather = (FloatingActionButton) findViewById(R.id.start_weather);
        fragmentWeather.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                findViewById(R.id.fragment_weatherchooser).setVisibility(View.VISIBLE);
                setControls(false, false, false, false, false);
            }
        });


        // tap get Moving Settings
        final FloatingActionButton settings_geo_fragment = (FloatingActionButton) findViewById(com.robellistudios.photobuddy.R.id.settings_geo_fragment);
        settings_geo_fragment.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                findViewById(R.id.fragment_geo).setVisibility(View.INVISIBLE);
                setControls(true, true, false, true, true);
            }
        });


        // tap Overtake Map and Settings
        FloatingActionButton ok_geo_fragment = (FloatingActionButton) findViewById(com.robellistudios.photobuddy.R.id.ok_geo_fragment);
        ok_geo_fragment.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                GeoMapFragment gfm = (GeoMapFragment)fragmentManager.findFragmentById(R.id.fragment_geo);
                gfm.takeSnapshot();
                DataToPhotoMerger dtpm = new DataToPhotoMerger(getApplicationContext(), ((BitmapDrawable) mMainImageView.getDrawable()).getBitmap(), gfm.mMapSnapshot, mMainImageView_Save.getWidth(), mMainImageView_Save.getHeight(), mMainImageView_Save.getMatrix());
                mMainImageView.setImageBitmap(dtpm.mBitmap);

                setControls(true, true, false, true, true);
            }
        });



        // tap get Map Settings
        FloatingActionButton movegeo = (FloatingActionButton) findViewById(com.robellistudios.photobuddy.R.id.move_geo_fragment);
        movegeo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
                    Bitmap bitmap;

                    @Override
                    public void onSnapshotReady(Bitmap snapshot) {
                        // TODO Auto-generated method stub
                        bitmap = snapshot;
                    }
                };

                MoveMapSettings mms = new MoveMapSettings();
                mms.show(getFragmentManager(), "Maps settings");
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(com.robellistudios.photobuddy.R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, com.robellistudios.photobuddy.R.string.navigation_drawer_open, com.robellistudios.photobuddy.R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    // Location Listener
    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(android.location.Location location) {

            mLat = location.getLatitude();
            mLon = location.getLongitude();

            JSONWeatherTask task = new JSONWeatherTask();
            task.execute(city);

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefs.edit().putFloat("geo_maps_lat", (float) location.getLatitude()).apply();
                prefs.edit().putFloat("geo_maps_lon", (float) location.getLongitude()).apply();
                prefs.edit().putFloat("geo_maps_alt", (float) location.getAltitude()).apply();
                prefs.edit().putFloat("geo_maps_bearing", (float) location.getBearing()).apply();
                prefs.edit().putFloat("geo_maps_accuracy", (float) location.getAccuracy()).apply();

                // Stop Location Manager to save Power and Resources
                mLocationManager.removeUpdates(mLocationListener);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }


        @Override
        public void onProviderDisabled(String provider) {

            Log.e("PB", "Povider disabled");
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {

                // try to get last known position
                android.location.Location loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                // if no LKP -> take the values from Shared Preferences
                if (loc == null) {
                    Toast.makeText(MainActivity.this, "no GPS - last known position will be used", Toast.LENGTH_SHORT).show();

                    GeneralHelpers gh = new GeneralHelpers();
                    ArrayList al = gh.getLastLocation(getApplicationContext()); // get the LKP

                    // Convert
                    Float f = (Float) al.get(0); mLat = f.doubleValue();
                    f= (Float) al.get(1);        mLon = f.doubleValue();

                    // Execute the Weather Task
                    JSONWeatherTask task = new JSONWeatherTask();
                    task.execute(city);
                }
            }
        }
    };

    // Activity Result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        GeneralHelpers gh = new GeneralHelpers();

        switch (requestCode) {
            case RESULT_GALLERY:               // from Album (MediaStore)
                if (null != data) {
                    try {
                        ImageView iv = mMainImageView;
                        mMainImageView_Save = mMainImageView;       // save the Original Image
                        iv.setImageURI(data.getData());
                        mMainImageView = gh.RotateImage(getApplicationContext(), iv, data.getData().normalizeScheme());

                        findViewById(R.id.start_map).setVisibility(View.VISIBLE);
                        findViewById(R.id.start_weather).setVisibility(View.VISIBLE);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case RESULT_CAMERA:               // from Camera)
                if (null != data) {
                    ImageView iv = mMainImageView;
                    iv.setImageURI(data.getData());
                    mMainImageView = scaleImage(iv, 2048);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(APP_TAG, "PAUSED");

        // Unregister the Broadcast Listener
//        if(localBroadcastReceiver)
//            unregisterReceiver(localBroadcastReceiver);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("PB ***", "RESUMED");

        // Reregister the Brodcast Listener
//        localBroadcastReceiver = new LocalBroadcastReceiver();
//        IntentFilter filterMMS = new IntentFilter("MMS_FINISHED");
//        IntentFilter filterWLC = new IntentFilter("WEATHERCHOOSER_FINISHED");
//        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filterMMS);
//        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filterWLC);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(com.robellistudios.photobuddy.R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.robellistudios.photobuddy.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.robellistudios.photobuddy.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == com.robellistudios.photobuddy.R.id.nav_camera) {
            // Handle the camera action
        } else if (id == com.robellistudios.photobuddy.R.id.nav_gallery) {

        } else if (id == com.robellistudios.photobuddy.R.id.nav_slideshow) {

        } else if (id == com.robellistudios.photobuddy.R.id.nav_manage) {

        } else if (id == com.robellistudios.photobuddy.R.id.nav_share) {

        } else if (id == com.robellistudios.photobuddy.R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(com.robellistudios.photobuddy.R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * Scale Image - set the correct Scaling for the Image
     * @param view
     * @param boundBoxInDp
     * @return Scaled ImageView
     */
    private ImageView scaleImage(ImageView view, int boundBoxInDp)
    {
        // Get the ImageView and its bitmap
        Drawable drawing = view.getDrawable();
        Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

        // Get current dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) boundBoxInDp) / width;
        float yScale = ((float) boundBoxInDp) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        width = scaledBitmap.getWidth();
        height = scaledBitmap.getHeight();

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);

        return view;
    }

    /**
     * @param dp
     * @return Pixels in int
     */
    private int dpToPx(int dp)
    {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * (density));
    }

    public void setMapAfterSettings() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        View fragment_geo = findViewById(R.id.fragment_geo);
        fragment_geo.setAlpha(prefs.getFloat("map_settings_opaque", 100));

        GeoMapFragment gfm = (GeoMapFragment)fragmentManager.findFragmentById(R.id.fragment_geo);
        DataToPhotoMerger dtpm = new DataToPhotoMerger(getApplicationContext(), ((BitmapDrawable) mMainImageView_Save.getDrawable()).getBitmap(), gfm.mMapSnapshot, mMainImageView_Save.getWidth(), mMainImageView_Save.getHeight(), mMainImageView_Save.getMatrix());
        mMainImageView.setImageBitmap(dtpm.mBitmap);

        findViewById(R.id.fragment_geo).setVisibility(View.INVISIBLE);
        setControls(true, true, false, true, true);
   }


    private void setControls(boolean album, boolean camera, boolean geoframgment, boolean map, boolean weather) {

//        View mapView = findViewById(R.id.fragment_geo);
//        View startmap = findViewById(R.id.start_map);
//        View startweather = findViewById(R.id.start_weather);
//        View albumView = findViewById(R.id.alb);
//        View cameraView = findViewById(R.id.fab);

        if(album) {
            findViewById(R.id.alb).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.alb).setVisibility(View.INVISIBLE);
        }

        if(camera) {
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.fab).setVisibility(View.INVISIBLE);
        }

        if(geoframgment) {
            findViewById(R.id.fragment_geo).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.fragment_geo).setVisibility(View.INVISIBLE);
        }

        if (map) {
            findViewById(R.id.start_map).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.start_map).setVisibility(View.INVISIBLE);
        }

        if (weather) {
            findViewById(R.id.start_weather).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.start_weather).setVisibility(View.INVISIBLE);
        }
    }



    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ((new WeatherHttpClient()).getWeatherData(mLat, mLon));

            try {
                weather = JSONWeatherParser.getWeather(data);

                // Let's retrieve the icon
                weather.iconData = ((new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;

        }


        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            if (weather.iconData != null && weather.iconData.length > 0) {
            }

            // put the Weatherdatas into globals
            if(weather.temperature != null) {
                mTemperature = String.format("%.2f\u2103", weather.temperature.getTemp() -273.15);
                mCondition = weather.currentCondition.getCondition();
                mHumidity = String.format("%.2f %% Humidity", weather.currentCondition.getHumidity());
                mWind = String.format("%.2f km/h", weather.wind.getSpeed());
                mBearing = String.format("%.2f\u00B0", weather.wind.getDeg());
            }

            // get the Weather Output Layout and fill it
            LayoutInflater inflater;
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableLayout layout = (TableLayout) inflater.inflate(R.layout.temperature_output_1, null);

            TextView temp_layout = (TextView) layout.findViewById(R.id.temperature);
            temp_layout.setText(mTemperature);
            TextView condition_layout = (TextView) layout.findViewById(R.id.condition);
            condition_layout.setText(mCondition);
            TextView humidity_layout = (TextView) layout.findViewById(R.id.humidity);
            humidity_layout.setText(mHumidity);
            TextView wind_layout = (TextView) layout.findViewById(R.id.wind);
            wind_layout.setText(mWind);
            TextView bearing_layout = (TextView) layout.findViewById(R.id.bearing);
            bearing_layout.setText(mBearing);

//   temp remove         DataToPhotoMerger dtpm = new DataToPhotoMerger(getApplicationContext(), ((BitmapDrawable) mMainImageView_Save.getDrawable()).getBitmap(), layout, mMainImageView_Save.getWidth(), mMainImageView_Save.getHeight(), mMainImageView_Save.getMatrix());
//   temp remove         mMainImageView.setImageBitmap(dtpm.mBitmap);

            // Save Image automatically - TODO save after user input (OK Button)
//   temp remove         helper_SaveLoadImage save = new helper_SaveLoadImage();
//   temp remove         save.saveImage(getApplicationContext(), mMainImageView);
        }
    }


    private class LocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("PB ***", "RECEIVED Listener");

            // safety check
            if (intent == null || intent.getAction() == null) {
                return;
            }

            if (intent.getAction().equals("MMS_FINISHED")) {
                setMapAfterSettings();
            }

            if (intent.getAction().equals("WEATHERCHOOSER_FINISHED")) {
                setControls(true, true, false, true, true);
            }

        }
    }
}
