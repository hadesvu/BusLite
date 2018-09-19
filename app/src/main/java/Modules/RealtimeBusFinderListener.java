package Modules;

import java.util.List;

public interface RealtimeBusFinderListener {

        void RealtimeBusFinderStart();
        void RealtimeBusFinderSuccess(List<Vehicle> vehicle);
}
