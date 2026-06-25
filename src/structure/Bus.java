package structure;

public class Bus extends Transport{
    private final String routeId;

    public Bus(String id, String routeId) { super(id); this.routeId = routeId; }

    public String getRouteId() { return this.routeId; }

    @Override
    public String getTransportType() {
        return "Bus";
    }
}
