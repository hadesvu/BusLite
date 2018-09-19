package Modules;

import android.net.sip.SipSession;
import android.os.AsyncTask;
import android.util.Log;

import com.buslite.GtfsActivity;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RealtimeBusFinder {
    //http://api.511.org/transit/StopMonitoring?api_key=97fb8a6c-ffe1-4071-9614-8a5b31a7fc8a&agency=SC&stopCode=60464&format=JSON
    private static final String STOPMONITORING = "http://api.511.org/transit/StopMonitoring?";
    private static final String KEY = "97fb8a6c-ffe1-4071-9614-8a5b31a7fc8a";
    private static final String AGENCY = "SC";
    private static final String STOPCODE = "60464";
    private RealtimeBusFinderListener listener;
    

    public RealtimeBusFinder(RealtimeBusFinderListener listener) {
        this.listener = listener;

    }

    public void execute() throws UnsupportedEncodingException {
        listener.RealtimeBusFinderStart();
        new RealtimeBusFinder.DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
      

        return STOPMONITORING + "api_key=" + KEY + "&agency=" + AGENCY + "&stopCode=" + STOPCODE + "&format=JSON";
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;

        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonVehicleLocation = jsonData.getJSONObject("ServiceDelivery").getJSONObject("StopMonitoringDelivery")
                .getJSONArray("MonitoredStopVisit");
        for (int i = 0; i < jsonVehicleLocation.length(); i++) {
            JSONObject jsonVehicle = jsonVehicleLocation.getJSONObject(i).getJSONObject("MonitoredVehicleJourney")
                    .getJSONObject("VehicleLocation");
            Vehicle vehicle = new Vehicle();
            String Lat = ""+jsonVehicle.getDouble("Latitude");
            Log.d("TESTTTTTTTTTTTTTTTTT",   Lat );
            vehicle.location = new LatLng(jsonVehicle.getDouble("Latitude"),jsonVehicle.getDouble("Longitude"));
            vehicles.add(vehicle);
        }

        listener.RealtimeBusFinderSuccess(vehicles);
    }

}
