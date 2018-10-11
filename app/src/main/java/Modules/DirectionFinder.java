package Modules;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.transit.realtime.GtfsRealtime;

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
import java.net.URLEncoder;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DirectionFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBFjK8UInAeNGfhx8attCH8UNY6xzNjuwU";
    private static final String MODE = "transit";
    private static final String GTFS_URL_API = "http://developers.cata.org/gtfsdownload.ashx?key=";
    private static final String GTFS_API_KEY = "97d1ac8b-e6c5-4f06-8dbf-4bd9e1361302";
    private static final String GTFS_MODE = "vehicle";

    private String URL2 = GTFS_URL_API + GTFS_API_KEY + "&data=" + GTFS_MODE;

    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private Context context;
    private HashSet<Integer> tripID = new HashSet<>();

    public DirectionFinder(Context c, DirectionFinderListener listener, String origin, String destination) {
        this.context = c;
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        new DownloadGoogleData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, createUrl());
        new DownloadVehicleData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");
        return DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + "&mode=" + MODE + "&key=" + GOOGLE_API_KEY;
    }

    private void Google(String data) throws JSONException {
        if (data == null)
            return;

        List<Route> routes = new ArrayList<>();
        List<Stop> stops = new ArrayList<>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.endAddress = jsonLeg.getString("end_address");
            route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = PolyUtil.decode(overview_polylineJson.getString("points"));

            List<Step> steps = new ArrayList<>();
            JSONArray jsonSteps = jsonLeg.getJSONArray("steps");
            for (int j=0; j<jsonSteps.length(); j++){
                JSONObject jsonStep = jsonSteps.getJSONObject(j);
                Step step = new Step();
                step.distance = new Distance(jsonStep.getJSONObject("distance").getString("text")
                        ,jsonStep.getJSONObject("distance").getInt("value"));
                step.duration = new Duration(jsonStep.getJSONObject("duration").getString("text")
                        ,jsonStep.getJSONObject("duration").getInt("value"));
                step.travelMode = jsonStep.getString("travel_mode");
                Log.d("testtttt", step.travelMode);
                step.instruction = jsonStep.getString("html_instructions");
                Log.d("testtttt", step.instruction);
                steps.add(step);
                route.steps = steps;
                Stop stop = new Stop();
                if (step.travelMode.equals("TRANSIT")) {
                    stop.depart_stop_name = jsonStep.getJSONObject("transit_details").getJSONObject("departure_stop").getString("name");
                    stop.depart_point = new LatLng(jsonStep.getJSONObject("transit_details").getJSONObject("departure_stop").getJSONObject("location").getDouble("lat"),
                            jsonStep.getJSONObject("transit_details").getJSONObject("departure_stop").getJSONObject("location").getDouble("lng"));
                    stop.route_name = jsonStep.getJSONObject("transit_details").getString("headsign");
                    Long epoch = jsonStep.getJSONObject("transit_details").getJSONObject("departure_time").getLong("value");
                    stop.depart_time = new Time(epoch * 1000);
                    stop.arrival_stop_name = jsonStep.getJSONObject("transit_details").getJSONObject("arrival_stop").getString("name");
                    stop.arrival_point = new LatLng(jsonStep.getJSONObject("transit_details").getJSONObject("arrival_stop").getJSONObject("location").getDouble("lat"),
                            jsonStep.getJSONObject("transit_details").getJSONObject("arrival_stop").getJSONObject("location").getDouble("lng"));
                    stop.route_id = jsonStep.getJSONObject("transit_details").getJSONObject("line").getString("short_name");
                    Log.d("stop.route_id", "stop.route_id: " + stop.route_id);
                    stops.add(stop);
                    try {
                        Trips_ids(stop.route_id);
                        tripID = Stop_times(Stops(stop), stop.depart_time);
                        Log.d("IDD", "IDDDDDDDDDDDDDDDDDDDDDDDDDDD: " + tripID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
            routes.add(route);
        }

        listener.onDirectionFinderSuccess(routes, stops);
    }

    private int Stops(Stop stop) throws IOException {
        int stop_id;
        int stop_code;
        String stop_name;
        String stop_desc;
        LatLng stop_point;
        //int zone_id;

        InputStream raw = context.getAssets().open("stops.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(raw, "UTF8"));
        String line = null;
        try {
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] value = line.replaceAll("\"", "").split(",");
                stop_id = Integer.parseInt(value[10]);
                stop_code = Integer.parseInt(value[2]);
                stop_name = value[8];
                stop_desc = value[7];
                stop_point = new LatLng(Double.parseDouble(value[0]), Double.parseDouble(value[3]));
                //zone_id = Integer.parseInt(value[6]);

                if (stop.route_name.equals(stop_name) || stop.depart_point.equals(stop_point)) {
                    Log.d("IDDDDDDDDDDDDDDDDDDDDD", "stop_name: " + stop_name);
                    Log.d("IDDDDDDDDDDDDDDDDDDDDD", "stop_point: " + stop_point);
                    Log.d("IDDDDDDDDDDDDDDDDDDDDD", "GetStopID: " + stop_id);
                    return stop_id;
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private HashSet<Integer> Stop_times(int stopID, Time depart_time) throws IOException {
        int trip_id;
        Time arrival_time;
        Time departure_time;
        int stop_id;
        int stop_seq;
        HashSet<Integer> trips = new HashSet<>();
        InputStream raw = context.getAssets().open("stop_times.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(raw, "UTF8"));
        String line = null;
        try {
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] value = line.replaceAll("\"", "").split(",");
                trip_id = Integer.parseInt(value[0]);
                arrival_time = Time.valueOf(value[1]);
                departure_time = Time.valueOf(value[2]);
                stop_id = Integer.parseInt(value[3]);
                stop_seq = Integer.parseInt(value[4]);
                //Log.d("IDDDDDDDDDDDDDDDDDDDDD", "depart_time: "+depart_time.before(departure_time));
                // Log.d("IDDDDDDDDDDDDDDDDDDDDD", "departure_time: "+departure_time+"depart_time"+depart_time);
                if (stop_id == stopID && depart_time.after(departure_time)) {
                    trips.add(trip_id);
                    return trips;
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return trips;
    }

    private HashSet<Integer> Trips_ids(String route_id) throws IOException {
        int trip_id;
        String routeID;
        HashSet<Integer> trips = new HashSet<>();
        InputStream raw = context.getAssets().open("trips.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(raw, "UTF8"));
        String line = null;
        try {
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] value = line.replaceAll("\"", "").split(",");
                trip_id = Integer.parseInt(value[8]);
                routeID = value[2];
                //Log.d("IDDDDDDDDDDDDDDDDDDDDD", "depart_time: "+depart_time.before(departure_time));
                // Log.d("IDDDDDDDDDDDDDDDDDDDDD", "departure_time: "+departure_time+"depart_time"+depart_time);
                if (routeID.equals(route_id)) {
                    trips.add(trip_id);
                    //return trips;
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int trip : trips) Log.d("TRIPPPP", "Trips_ids: " + trip);
        return trips;
    }

    private class DownloadGoogleData extends AsyncTask<String, Void, String> {

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
                Google(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class DownloadVehicleData extends AsyncTask<String, Void, String> {
        List<Vehicle> vehicles = new ArrayList<>();

        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            try {
                url = new URL(URL2);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            GtfsRealtime.FeedMessage feed = null;
            try {
                feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }


            Log.d("TRIPPPPPPPPPPPPPP", "TRIPIDDDDDDDDDDDDDDD" + tripID);

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasVehicle()) {
                    Vehicle vehicle = new Vehicle();
                    vehicle.tripID = Integer.parseInt(entity.getVehicle().getTrip().getTripId());
                    if (tripID.contains(vehicle.tripID)) {
                        vehicle.location = new LatLng(entity.getVehicle().getPosition().getLatitude(), entity.getVehicle().getPosition().getLongitude());
                        vehicles.add(vehicle);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            listener.onShowVehicleSuccess(vehicles);
        }

    }


}
