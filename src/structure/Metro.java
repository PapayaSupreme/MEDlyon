package structure;

public class Metro extends Transport{
    private String routeId;

    public Metro(String id, String routeId) { super(id); this.routeId = routeId; }

    public String getRouteId() { return this.routeId; }

    @Override
    public String getTransportType() {
        return "Metro";
    }
}
