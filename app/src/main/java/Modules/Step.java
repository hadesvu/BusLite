package Modules;

public class Step {
    public Distance distance;
    public Duration duration;
    public String travelMode;
    public String instruction;

    public Step(Distance distance, Duration duration, String travelMode, String instruction) {
        this.distance = distance;
        this.duration = duration;
        this.travelMode = travelMode;
        this.instruction = instruction;
    }

    public Step() {
    }

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

}
