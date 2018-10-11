package Modules;

import java.util.List;

public interface DirectionFinderListener {
    void onDirectionFinderStart();

    void onDirectionFinderSuccess(List<Route> route, List<Stop> stops);

    void onShowVehicleSuccess(List<Vehicle> vehicles);
}
