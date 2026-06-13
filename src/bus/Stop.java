/**
 * Représente un arrêt GTFS (une ligne de stops.txt).
 * Dans le projet Smart Mobility, un Stop est destiné à devenir un NŒUD du graphe :
 * stopId = identifiant du nœud, lat/lon = position (utile pour distances et heuristique A*).
 */
public class Stop {

    // Champs 'final' = définis une fois dans le constructeur, jamais modifiés ensuite.
    // C'est de l'immutabilité : un Stop chargé ne peut plus être altéré par erreur ailleurs.
    private final String stopId;
    private final String stopName;
    private final double lat;
    private final double lon;

    public Stop(String stopId, String stopName, double lat, double lon) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.lat = lat;
        this.lon = lon;
    }

    public String getStopId()   { return stopId; }
    public String getStopName() { return stopName; }
    public double getLat()      { return lat; }
    public double getLon()      { return lon; }

    @Override
    public String toString() {
        return String.format("Stop{id=%s, name='%s', lat=%.5f, lon=%.5f}",
                stopId, stopName, lat, lon);
    }
}
