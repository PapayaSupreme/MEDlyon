import parsers.BusParser;
import structure.Bus;
import structure.BusStop;
import structure.Node;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;

import Core.Dijkstra;
import Core.Dijkstra.ShortestPathsResult;
import structure.Graph;


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
        Map<String, Bus> buses = result.transports();
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
            System.out.println("\nExample stop_id=" + anyStopId + " served by trips=" + result.stopToTrips().getOrDefault(anyStopId, java.util.List.of()).size()
                    + " routes=" + result.stopToRoutes().getOrDefault(anyStopId, java.util.List.of()).size());
        }
        System.out.println("Ordered stops of a bus line: ");
        for (Map.Entry<String, Bus> e : buses.entrySet()) {
            System.out.println("Trip: " + e.getKey() + " route=" + e.getValue().getRouteId());
            for (Node node : e.getValue().getStopsOrdered()) {
                System.out.println(node.getName());
            }
            break;
        }



        // build graph and add all parsed stops as nodes
        Graph g = new Graph();
        for (BusStop bs : stops.values()) {
            g.addNode(bs);
        }

        // pick source/target from up to 5 candidate stops (configurable by CLI indexes)
        Node source = null;
        Node target = null;
        List<BusStop> sortedStops = new ArrayList<>(stops.values());
        sortedStops.sort(Comparator.comparing(BusStop::getId));

        int candidatesCount = Math.min(5, sortedStops.size());
        if (candidatesCount > 0) {
            System.out.println("\nStart/target candidates (1-based index):");
            for (int i = 0; i < candidatesCount; i++) {
                BusStop c = sortedStops.get(i);
                System.out.println(" [" + (i + 1) + "] " + c.getId() + " - " + c.getName());
            }

            int defaultStartIndex = 1;
            int defaultTargetIndex = Math.min(2, candidatesCount);

            int startIndex = defaultStartIndex;
            int targetIndex = defaultTargetIndex;

            if (args.length > 1) {
                try { startIndex = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
            }
            if (args.length > 2) {
                try { targetIndex = Integer.parseInt(args[2]); } catch (NumberFormatException ignored) {}
            }

            if (startIndex < 1 || startIndex > candidatesCount) startIndex = defaultStartIndex;
            if (targetIndex < 1 || targetIndex > candidatesCount) targetIndex = defaultTargetIndex;

            source = sortedStops.get(startIndex - 1);
            target = sortedStops.get(targetIndex - 1);

            System.out.println("Selected start: [" + startIndex + "] " + source);
            System.out.println("Selected target: [" + targetIndex + "] " + target);
        }

        if (source == null) {
            System.out.println("No stops parsed, skipping shortest-path example.");
            return;
        }

        ShortestPathsResult res = Dijkstra.computeShortestPaths(g, source);

        // distance in meters from source to target
        double meters = res.getDistance(target);

        // get predecessor map or reconstruct the path
        List<Node> path = res.reconstructPath(target);
        if (path.isEmpty()) {
            System.out.println("No path found");
        } else {
            System.out.println("Distance (m): " + meters);
            System.out.println("Path:");
            for (Node n : path) {
                System.out.println("  " + n);
            }
        }

    }
}