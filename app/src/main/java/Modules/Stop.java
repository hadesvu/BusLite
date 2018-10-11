package Modules;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;



public class Stop {
    public int stop_id;
    public int stop_code;
    public String stop_name;
    public String stop_desc;
    public LatLng stop_point;
    public int zone_id;
    public Time depart_time;
    public String route_name;
    public String depart_stop_name;
    public LatLng depart_point;
    public String arrival_stop_name;
    public LatLng arrival_point;
    public String route_id;

    public Stop() {
    }

    public Stop(int stop_id, int stop_code, String stop_name, String stop_desc, LatLng stop_point, int zone_id) {
        this.stop_id = stop_id;
        this.stop_code = stop_code;
        this.stop_name = stop_name;
        this.stop_desc = stop_desc;
        this.stop_point = stop_point;
        this.zone_id = zone_id;
    }

    public Stop(Time depart_time, String route_name, String depart_stop_name, LatLng depart_point, String arrival_stop_name, LatLng arrival_point, String route_id) {
        this.depart_time = depart_time;
        this.route_name = route_name;
        this.depart_stop_name = depart_stop_name;
        this.depart_point = depart_point;
        this.arrival_stop_name = arrival_stop_name;
        this.arrival_point = arrival_point;
        this.route_id = route_id;
    }
}
