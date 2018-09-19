package Modules;

import com.google.android.gms.maps.model.LatLng;

public class Stop {
    public int stop_id;
    public int stop_code;
    public String stop_name;
    public String stop_desc;
    public LatLng stop_point;
    public int zone_id;

    public Stop(int stop_id, int stop_code, String stop_name, String stop_desc, LatLng stop_point, int zone_id) {
        this.stop_id = stop_id;
        this.stop_code = stop_code;
        this.stop_name = stop_name;
        this.stop_desc = stop_desc;
        this.stop_point = stop_point;
        this.zone_id = zone_id;
    }
}
