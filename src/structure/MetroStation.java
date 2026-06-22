package structure;
public class MetroStation extends Node {
    public MetroStation(String id, String name, Coordinates coordinates) {
        super(id, name, coordinates);
    }

    @Override
    String getStopType() {
        return "MetroStation";
    }
}