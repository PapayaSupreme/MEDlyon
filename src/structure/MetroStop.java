package structure;

public class MetroStop extends Node {

    public MetroStop(String id, String name, Coordinates coordinates) {
        super(id, name, coordinates);
    }

    @Override
    String getStopType() {
        return "MetroStop";
    }
}
