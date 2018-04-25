package com.example.samchyu.mapexperimentation;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        requestQueue = Volley.newRequestQueue(this);

        getStops();
        getPOI();

        Button location = findViewById(R.id.yourLocation);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng me = new LatLng(40.116, -88.2073);
                poiMarkers.add(mMap.addMarker(new MarkerOptions().position(me).title("YOU ARE HERE")));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(me, 15));

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
                    }
                } else {
                    for (int i = 0; i < poiMarkers.size();i++) {
                        poiMarkers.get(i).setVisible(false);
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

        // Add a marker in Sydney and move the camera
        LatLng urbana = new LatLng(40.116, -88.2073);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(urbana, 15));
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
            stopMarkers.add(mMap.addMarker(new MarkerOptions().position(individualLatLng).title(aStopData[0]).snippet(aStopData[3]).visible(false)));
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
            poiMarkers.add(mMap.addMarker(new MarkerOptions().position(individualLatLng).title(aPoiData[0]).snippet(aPoiData[3]).visible(false)));
        }

    }




}
