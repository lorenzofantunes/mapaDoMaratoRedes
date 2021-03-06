package com.example.vplentz.maraudersmapclient;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String NAME = "NAME";
    private static final String PASSW = "PASSW";

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    private ArrayList<String> mPassWords;
    private String mMyself = "";
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private GoogleMap mMap;
    private MarkerOptions mMyMarker;

    private FloatingActionButton mFab;
    private Spinner mSpinner;
    private ArrayAdapter<String> mSpinnerArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        mFab = findViewById(R.id.fab);
        mSpinner = findViewById(R.id.spinner);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60);//request location every 10 minutes
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
//                    Toast.makeText(MapsActivity.this, "location " + location.getLatitude(),Toast.LENGTH_SHORT).show();
                    // Update UI with location data
                    LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    if (mMyMarker == null) {
                        mMyMarker = new MarkerOptions().position(myLoc).title("Myself, wearing a invisible cloak");
                        mMap.addMarker(mMyMarker).setIcon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(R.drawable.cloak)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 16));
                    } else {
                        mMyMarker.position(myLoc);
                    }

                    //create json
                    JSONObject json = new JSONObject();
                    if (mPassWords.size() > 0) {
                        try {
                            mMyself = mMyself.replaceAll(" ", "");
                            mMyself = mMyself.toLowerCase();

//                            json.put("palavras", mPassWords);
                            for (String pass : mPassWords) {
                                json.accumulate("palavras", pass);
                            }
                            json.put("onde", "Pair{" + myLoc.latitude + ", " + myLoc.longitude + "}");
                            json.put("nome", mMyself);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//
                        Log.d(TAG, json.toString());
                        Log.d(TAG, "lat:" + myLoc.latitude + "long" + myLoc.longitude);
                        Log.d(TAG, "lengh" + json.toString().getBytes().length);
                        new SendAsync(MapsActivity.this).execute(json);


                    }


                    // ...
                }
            }


        };
        mPassWords = new ArrayList<>();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (sharedPref.getString(NAME, null) != null)
            mMyself = sharedPref.getString(NAME, null);
        else
            showNameDialog();

        if (sharedPref.getStringSet(PASSW, null) != null){
            mPassWords.addAll(sharedPref.getStringSet(PASSW, null));
            startLocationUpdates();
        }
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeLangDialog();
            }
        });
        spinnerAdapter();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    public Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(MapsActivity.this, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    public void showNameDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog2, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = dialogView.findViewById(R.id.nameET);

        dialogBuilder.setTitle("Quem sou eu:");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                if (!edt.getText().toString().equals("")) {
                    mMyself = edt.getText().toString();
                    SharedPreferences sharedPref = MapsActivity.this.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(NAME, mMyself);
                    editor.apply();
                }
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
    public void showChangeLangDialog() {
        if(mMyself.equals("")){
            showNameDialog();
        }else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
            dialogBuilder.setView(dialogView);

            final EditText edt = dialogView.findViewById(R.id.passET);

            dialogBuilder.setTitle("Juro solenemente não fazer nada de bom");
            dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do something with edt.getText().toString();
                    if (!edt.getText().toString().equals("")) {
                        mPassWords.add(edt.getText().toString());
                        mSpinnerArrayAdapter.notifyDataSetChanged();
                        mSpinner.setSelection(mPassWords.size() - 1);
                    }
                    SharedPreferences sharedPref = MapsActivity.this.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putStringSet(PASSW, new HashSet<String>(mPassWords));
                    editor.apply();
                    startLocationUpdates();
                    //TODO SHOULD REQUEST DATA FROM SOCKET
                }
            });
            dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                }
            });
            AlertDialog b = dialogBuilder.create();
            b.show();
        }
    }

    private void spinnerAdapter() {
        mSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        mPassWords); //selected item will look like a spinner set from XML
        mSpinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerArrayAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MapsActivity.this, "selected " + mSpinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                    return;
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    public void showLocations(JSONArray jsonArray){
        mMap.clear();
        mMap.addMarker(mMyMarker).setIcon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(R.drawable.cloak)));
        for(int i = 0; i < jsonArray.length(); i++){
            try {

                jsonArray.get(i);
                Log.d(TAG, jsonArray.get(i).toString());
                String lat = ((JSONObject)jsonArray.get(i)).get("lat").toString();
                lat = lat.substring(lat.lastIndexOf(":\"")+2, lat.lastIndexOf("\"}")-1);
                String lon = ((JSONObject)jsonArray.get(i)).get("long").toString();
                lon = lon.substring(lon.lastIndexOf(":\"")+2, lon.lastIndexOf("\"}")-1);
                LatLng myLoc = new LatLng(Double.parseDouble(lat),
                        Double.parseDouble(lon));

                MarkerOptions markerOptions = new MarkerOptions().position(myLoc).title(((JSONObject)jsonArray.get(i)).get("_id").toString());

                mMap.addMarker(markerOptions).setIcon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(R.drawable.wand)));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
