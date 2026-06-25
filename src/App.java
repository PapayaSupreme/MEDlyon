package structure;

import parsers.MetroParser;
import structure.Metro;
import structure.MetroStation;
import structure.Node;
import structure.Graph;
import parsers.BusParser;
import structure.Bus;
import structure.BusStop;
import structure.Node;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

import Core.Dijkstra;
import Core.Dijkstra.ShortestPathsResult;
import structure.Graph;


public class App{
    static void main(String[] args) throws Exception{
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

        // Interactive menu for route searching
        interactiveRouteSearch(g, sortedStops);

    }

    private static void interactiveRouteSearch(Graph g, List<BusStop> allStops) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("INTERACTIVE ROUTE SEARCH MENU");
        System.out.println("=".repeat(60));

        while (true) {
            System.out.print("\nEnter departing stop (or 'q' to quit): ");
            Node departNode = selectStop(scanner, allStops, "departing");
            if (departNode == null) {
                break;
            }

            System.out.print("Enter arriving stop: ");
            Node arriveNode = selectStop(scanner, allStops, "arriving");
            if (arriveNode == null) {
                System.out.println("Cancelled. Returning to main menu.");
                continue;
            }

            if (departNode.equals(arriveNode)) {
                System.out.println("Departing and arriving stops cannot be the same.");
                continue;
            }

            // Measure computation time
            long startTime = System.nanoTime();

            ShortestPathsResult result = Dijkstra.computeShortestPaths(g, departNode);
            double distance = result.getDistance(arriveNode);
            List<Node> path = result.reconstructPath(arriveNode);

            long endTime = System.nanoTime();
            long durationNanos = endTime - startTime;
            double durationMillis = durationNanos / 1_000_000.0;

            // Display results
            System.out.println("\n" + "-".repeat(60));
            System.out.println("ROUTE SEARCH RESULTS");
            System.out.println("-".repeat(60));
            System.out.println("From: " + departNode.getName() + " (" + departNode.getId() + ")");
            System.out.println("To:   " + arriveNode.getName() + " (" + arriveNode.getId() + ")");

            if (path.isEmpty()) {
                System.out.println("\n✗ No route found between these stops.");
            } else {
                System.out.println("\n✓ Route found!");
                System.out.println("Total distance: " + String.format("%.2f", distance) + " meters");
                System.out.println("Number of stops: " + path.size());
                System.out.println("\nItinerary:");

                for (int i = 0; i < path.size(); i++) {
                    Node stop = path.get(i);
                    System.out.println("  " + (i + 1) + ". " + stop.getName() + " (" + stop.getId() + ")");
                }

                // Calculate and display segment distances
                System.out.println("\nDetailed segments:");
                double cumulativeDistance = 0.0;
                for (int i = 0; i < path.size() - 1; i++) {
                    Node currentStop = path.get(i);
                    Node nextStop = path.get(i + 1);
                    double segmentDistance = distance;
                    
                    // Try to calculate segment distance (if available in graph edges)
                    // This depends on how edges store distance information
                    System.out.println("  " + (i + 1) + " -> " + (i + 2) + ": " + currentStop.getName() + " to " + nextStop.getName());
                }
            }

            System.out.println("\nComputation time: " + String.format("%.4f", durationMillis) + " ms (" + durationNanos + " ns)");
            System.out.println("-".repeat(60));
        }

        scanner.close();
        System.out.println("\nGoodbye!");
    }

    private static Node selectStop(Scanner scanner, List<BusStop> allStops, String label) {
        while (true) {
            System.out.println("\nSelect mode:");
            System.out.println("  [1] Enter stop number");
            System.out.println("  [2] Search for a stop by name");
            System.out.print("Choose mode (1 or 2): ");
            
            String modeInput = scanner.nextLine().trim();
            
            if (modeInput.equals("1")) {
                return selectByNumber(scanner, allStops, label);
            } else if (modeInput.equals("2")) {
                return selectBySearch(scanner, allStops, label);
            } else if (modeInput.equalsIgnoreCase("q")) {
                return null;
            } else {
                System.out.println("Invalid input. Please enter 1 or 2.");
            }
        }
    }

    private static Node selectByNumber(Scanner scanner, List<BusStop> allStops, String label) {
        System.out.println("\nAvailable stops:");
        for (int i = 0; i < allStops.size(); i++) {
            BusStop stop = allStops.get(i);
            System.out.println("  [" + (i + 1) + "] " + stop.getId() + " - " + stop.getName());
        }

        while (true) {
            System.out.print("\nEnter stop number (or 'b' to go back): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("b")) {
                return selectStop(scanner, allStops, label);
            }

            try {
                int index = Integer.parseInt(input) - 1;
                if (index < 0 || index >= allStops.size()) {
                    System.out.println("Invalid index. Please try again.");
                    continue;
                }
                BusStop selected = allStops.get(index);
                System.out.println("✓ Selected: " + selected.getName() + " (" + selected.getId() + ")");
                return selected;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static Node selectBySearch(Scanner scanner, List<BusStop> allStops, String label) {
        System.out.print("Enter stop name to search (or 'b' to go back): ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        if (searchTerm.equals("b")) {
            return selectStop(scanner, allStops, label);
        }

        List<BusStop> matches = new ArrayList<>();
        for (BusStop stop : allStops) {
            if (stop.getName().toLowerCase().contains(searchTerm)) {
                matches.add(stop);
            }
        }

        if (matches.isEmpty()) {
            System.out.println("✗ No stops found matching '" + searchTerm + "'. Please try again.");
            return selectBySearch(scanner, allStops, label);
        }

        System.out.println("\nFound " + matches.size() + " matching stops:");
        for (int i = 0; i < matches.size(); i++) {
            BusStop stop = matches.get(i);
            System.out.println("  [" + (i + 1) + "] " + stop.getId() + " - " + stop.getName());
        }

        while (true) {
            System.out.print("\nSelect a stop (or 'b' to search again): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("b")) {
                return selectBySearch(scanner, allStops, label);
            }

            try {
                int index = Integer.parseInt(input) - 1;
                if (index < 0 || index >= matches.size()) {
                    System.out.println("Invalid index. Please try again.");
                    continue;
                }
                BusStop selected = matches.get(index);
                System.out.println("✓ Selected: " + selected.getName() + " (" + selected.getId() + ")");
                return selected;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}