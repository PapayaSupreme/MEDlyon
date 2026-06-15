import parsers.BusParser;
import structure.Bus;
import structure.BusStop;
import structure.Node;

import java.util.List;
import java.util.Map;

public class App{
    public static void main(String[] args) throws Exception{
        String base = args.length > 0 ? args[0] : "raw_datasets/bus/lyon_tcl";
        String stopsPath = base + "/stops.txt";
        String tripsPath = base + "/trips.txt";
        String stopTimesPath = base + "/stop_times.txt";

        System.out.println("Parsing stops from: " + stopsPath);
        Map<String, BusStop> stops = BusParser.parseStops(stopsPath);
        System.out.println("Parsed stops: " + stops.size());
        int shown = 0;
        for (Map.Entry<String, BusStop> e : stops.entrySet()) {
            if (shown++ >= 5) break;
            System.out.println(" stop_id=" + e.getKey() + " -> " + e.getValue());
        }

        System.out.println("\nParsing stop_times from: " + stopTimesPath);
        BusParser.Result result = BusParser.parseStopTimes(stopTimesPath, tripsPath, stops);
        Map<String, Bus> buses = result.transports;
        System.out.println("Parsed trips/buses: " + buses.size());
        shown = 0;
        for (Map.Entry<String, Bus> e : buses.entrySet()) {
            if (shown++ >= 5) break;
            List<Node> ordered = e.getValue().getStopsOrdered();
            System.out.println(" trip_id=" + e.getKey() + " route=" + e.getValue().getRouteId() + " stops=" + ordered.size());
            for (int i = 0; i < Math.min(5, ordered.size()); i++) {
                System.out.println("   " + ordered.get(i));
            }
        }

        // show reverse mapping example: pick first stop_id and list serving trips/routes
        if (!stops.isEmpty()) {
            String anyStopId = stops.keySet().iterator().next();
            System.out.println("\nExample stop_id=" + anyStopId + " served by trips=" + result.stopToTrips.getOrDefault(anyStopId, java.util.List.of()).size()
                    + " routes=" + result.stopToRoutes.getOrDefault(anyStopId, java.util.List.of()).size());
        }
        System.out.println("Ordered stops of a bus line: ");
        for (Map.Entry<String, Bus> e : buses.entrySet()) {
            System.out.println("Trip: " + e.getKey() + " route=" + e.getValue().getRouteId());
            for (Node node : e.getValue().getStopsOrdered()) {
                System.out.println(node.getName());
            }
            break;
        }
    }
}