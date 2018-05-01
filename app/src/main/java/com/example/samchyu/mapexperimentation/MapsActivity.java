package com.example.samchyu.mapexperimentation;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "Main";

    private static RequestQueue requestQueue;
    public static String json;
    public static String json1;

    public static String[][] stopData;
    public static String[][] poiData;

    public static List<Marker> stopMarkers = new ArrayList<>();
    public static List<Marker> poiMarkers = new ArrayList<>();

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean locationPremission = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        requestQueue = Volley.newRequestQueue(this);

        getStops();
        getPOI();

        getLocationPermissions();
        getLocation();

        // Create spinner (dropdown menu) for the map type
        Spinner mapTypes = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.mapTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapTypes.setAdapter(adapter);
        mapTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setMapType(position);
                Toast toast = Toast.makeText(getApplicationContext(),
                        parent.getItemAtPosition(position) + " selected",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button location = findViewById(R.id.yourLocation);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();

            }
        });

        ToggleButton stopstb = findViewById(R.id.toggleButton);
        stopstb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < stopMarkers.size(); i++) {
                        stopMarkers.get(i).setVisible(true);
                    }
                } else {
                    for (int i = 0; i < stopMarkers.size(); i++) {
                        stopMarkers.get(i).setVisible(false);
                    }
                }
            }
        });

        ToggleButton poitb = findViewById(R.id.toggleButton2);
        poitb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < poiMarkers.size(); i++) {
                        poiMarkers.get(i).setVisible(true);
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(14f));
                    }
                } else {
                    for (int i = 0; i < poiMarkers.size();i++) {
                        poiMarkers.get(i).setVisible(false);
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(17f));
                    }
                }
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocation();
    }

    void getStops() {
        String url = "https://developer.cumtd.com/api/v2.2/json/getstops?key=a13800cdc807401b9fcc4abbda713477";
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            json = response.toString();
                            stopData = parseAllStops();
                            Log.d(TAG, Arrays.deepToString(stopData));
                            createStopMarkers();
                            Log.d(TAG, stopMarkers.toString());
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(final VolleyError error) {
                    Log.w(TAG, error.toString());
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void getPOI() {
        String url = "https://data.urbanaillinois.us/resource/9p4k-dr6i.json";
        try {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new com.android.volley.Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(final JSONArray response) {
                            json1 = response.toString();
                            poiData = parseAllPOI();
                            Log.d(TAG, Arrays.deepToString(poiData));
                            createPOIMarkers();
                            Log.d(TAG, poiMarkers.toString());

                        }
                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(final VolleyError error) {
                    Log.w(TAG, error.toString());
                }
            });
            requestQueue.add(jsonArrayRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    String[][] parseAllStops() {
        JsonParser parser = new JsonParser();
        JsonObject result = parser.parse(json).getAsJsonObject();
        JsonArray stops = result.get("stops").getAsJsonArray();
        int stopCounter = 0;
        for (int i = 0; i < stops.size(); i++) {
            JsonObject individualStop = stops.get(i).getAsJsonObject();
            JsonArray stop_points = individualStop.get("stop_points").getAsJsonArray();
            for (int j = 0; j < stop_points.size(); j++) {
                stopCounter++;
            }
        }
        String[][] output = new String[stopCounter][4];
        for (int i = 0; i < stops.size(); i++) {
            JsonObject individualStop = stops.get(i).getAsJsonObject();
            JsonArray stop_points = individualStop.get("stop_points").getAsJsonArray();
            for (int j = 0; j < stop_points.size(); j++) {
                JsonObject individualStopPoint = stop_points.get(j).getAsJsonObject();
                String individualStopPointStopName = individualStopPoint.get("stop_name").getAsString();
                double lat = individualStopPoint.get("stop_lat").getAsDouble();
                double lon = individualStopPoint.get("stop_lon").getAsDouble();
                String code = individualStopPoint.get("code").getAsString();
                output[i][0] = individualStopPointStopName;
                output[i][1] = String.valueOf(lat);
                output[i][2] = String.valueOf(lon);
                output[i][3] = code;
            }
        }
        return output;
    }
    String[][] parseAllPOI() {
        JsonParser parser = new JsonParser();
        JsonArray result = parser.parse(json1).getAsJsonArray();
        String[][] poi = new String[result.size()][4];
        for (int i =0; i < result.size(); i++) {
            JsonObject individualPOI = result.get(i).getAsJsonObject();
            String resourceName = individualPOI.get("resource_name").getAsString();
            String resourceType = individualPOI.get("resource_type").getAsString();
            JsonObject location = individualPOI.get("location_1").getAsJsonObject();
            JsonArray coordinates = location.get("coordinates").getAsJsonArray();
            double lat = coordinates.get(0).getAsDouble();
            double lon = coordinates.get(1).getAsDouble();
            poi[i][0] = resourceName;
            poi[i][2] = String.valueOf(lat);
            poi[i][1] = String.valueOf(lon);
            poi[i][3] = resourceType;
        }
        return poi;
    }
    void createStopMarkers(){
        for (String[] aStopData : stopData) {
            if (aStopData[1] == null || aStopData[2] == null) {
                continue;
            }
            double lat = Double.valueOf(aStopData[1]);
            double lon = Double.valueOf(aStopData[2]);
            LatLng individualLatLng = new LatLng(lat, lon);
            stopMarkers.add(mMap.addMarker(new MarkerOptions()
                    .position(individualLatLng)
                    .title(aStopData[0])
                    .snippet(aStopData[3])
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .visible(false)));
        }
    }
    void createPOIMarkers(){
        for (String[] aPoiData : poiData) {
            if (aPoiData[1] == null || aPoiData[2] == null) {
                continue;
            }
            double lat = Double.valueOf(aPoiData[1]);
            double lon = Double.valueOf(aPoiData[2]);
            LatLng individualLatLng = new LatLng(lat, lon);
            poiMarkers.add(mMap.addMarker(new MarkerOptions()
                    .position(individualLatLng)
                    .title(aPoiData[0])
                    .snippet(aPoiData[3])
                    .visible(false)));
        }

    }


    private void getLocationPermissions () {
        Log.d(TAG, "getting permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED)){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == (PackageManager.PERMISSION_GRANTED)) {
                locationPremission = true;
            } else {
                ActivityCompat.requestPermissions(this, permissions,1);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions,1);
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPremission = false;
        switch (requestCode) {
            case 1: {
                if(grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            locationPremission = false;
                            return;
                        }
                    }
                    locationPremission = true;

                }
            }
        }
    }

    private void getLocation () {
        Log.d(TAG, "Getting your location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (locationPremission) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Location found");
                            Location currentLocation = (Location) task.getResult();
                            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            mMap.setMyLocationEnabled(true);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f));
                        } else {
                            Log.d(TAG, "Location not found");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "Did not have permission");
        }
    }

    private void setMapType (int type) {
        if (type == 1) {
            mMap.setMapType(2);
        } else if (type == 2) {
            mMap.setMapType(3);
        } else {
            mMap.setMapType(1);
        }
    }
}
