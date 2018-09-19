package com.buslite;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Modules.RealtimeBusFinder;
import Modules.RealtimeBusFinderListener;
import Modules.Stop;
import Modules.Vehicle;

public class GtfsActivity extends FragmentActivity implements OnMapReadyCallback, RealtimeBusFinderListener {
    private GoogleMap mMap;
    private Button btnFindPath;
    private Button btnGTFS;
    private EditText etOrigin;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gtfs_realtime);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this,30000);
                sendRequest();
            }
        },0);
    }

    private void draw_stop() throws UnsupportedEncodingException {
        List<Stop> stops = new ArrayList<>();
        int stop_id;
        int stop_code;
        String stop_name;
        String stop_desc;
        LatLng stop_point;
        int zone_id;
        Resources res = getResources();
        InputStream raw = res.openRawResource(R.raw.stops);
        BufferedReader reader = new BufferedReader(new InputStreamReader(raw,"UTF8"));
        String line = null;
        try {
            line = reader.readLine();
            while((line = reader.readLine())  != null ){
                 String[] value = line.replaceAll("\"","").split(",");
                stop_id = Integer.parseInt(value[0]);
                stop_code = Integer.parseInt(value[1]);
                stop_name = value[2];
                stop_desc = value[3];
                stop_point = new LatLng(Double.parseDouble(value[4]),Double.parseDouble(value[5]));
                zone_id = Integer.parseInt(value[6]);
                stops.add(new Stop(stop_id,stop_code,stop_name,stop_desc,stop_point,zone_id));
        }
            for(Stop stop : stops){
                mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_stop))
                        .title(stop.stop_name)
                        .position(stop.stop_point)
                );
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void draw_route() throws UnsupportedEncodingException {
        List<Stop> stops = new ArrayList<>();
        int stop_id;
        int stop_code;
        String stop_name;
        String stop_desc;
        LatLng stop_point;
        int zone_id;
        Resources res = getResources();
        InputStream raw = res.openRawResource(R.raw.stops);
        BufferedReader reader = new BufferedReader(new InputStreamReader(raw,"UTF8"));
        String line = null;
        try {
            line = reader.readLine();
            while((line = reader.readLine())  != null ){
                String[] value = line.replaceAll("\"","").split(",");
                stop_id = Integer.parseInt(value[0]);
                stop_code = Integer.parseInt(value[1]);
                stop_name = value[2];
                stop_desc = value[3];
                stop_point = new LatLng(Double.parseDouble(value[4]),Double.parseDouble(value[5]));
                zone_id = Integer.parseInt(value[6]);
                stops.add(new Stop(stop_id,stop_code,stop_name,stop_desc,stop_point,zone_id));
            }
            for(Stop stop : stops){
                 mMap.addCircle(new CircleOptions().fillColor(Color.RED)
                                .radius(10)
                                .center(stop.stop_point)

                );
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendRequest() {
            try {
                    new RealtimeBusFinder(this).execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng stop60464 = new LatLng(37.348568659999998, -121.948044);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stop60464, 18));
        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title("Stop 60464")
                .position(stop60464)));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        try {
            draw_stop();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void RealtimeBusFinderStart() {
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }
        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }
        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void RealtimeBusFinderSuccess(List<Vehicle> vehicles) {
        for (Vehicle vehicle : vehicles) {
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus))
                    .position(vehicle.location)
            ));
        }
    }
}
