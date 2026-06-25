package structure;

public class BusStop extends Node {

    public BusStop(String id, String name, Coordinates coordinates) {
        super(id, name, coordinates);
    }

    @Override
    String getStopType() {
        return "BusStop";
    }
}
