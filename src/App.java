package structure;

import parsers.MetroParser;
import structure.Metro;
import structure.MetroStation;
import structure.Node;
import structure.Graph;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;

import Core.Dijkstra;
import Core.Dijkstra.ShortestPathsResult;

public class App {
    public static void main(String[] args) throws Exception {
        // Chemins par défaut pour les fichiers de données du métro
        String base = args.length > 0 ? args[0] : "raw_datasets/metro";
        String stopsPath = base + "/stations-metro-reseau-transports-commun-lyonnais.csv";
        String horairesPath = base + "/horaires_tcl.csv";

        System.out.println("Parsing stations from: " + stopsPath);
        Map<String, MetroStation> stations = MetroParser.parseStations(stopsPath);
        System.out.println("Parsed stations: " + stations.size());
        
        int shown = 0;
        for (Map.Entry<String, MetroStation> e : stations.entrySet()) {
            if (shown++ >= 5) break;
            System.out.println(" station_id=" + e.getKey() + " -> " + e.getValue());
        }

        System.out.println("\nParsing horaires from: " + horairesPath);
        // On suppose ici que ton MetroParser retourne un objet Result contenant tes "Metro", comme le BusParser
        MetroParser.Result result = MetroParser.parseHoraires(horairesPath, stations);
        Map<String, Metro> metros = result.transports();
        
        System.out.println("Parsed metro lines: " + metros.size());
        shown = 0;
        for (Map.Entry<String, Metro> e : metros.entrySet()) {
            if (shown++ >= 5) break;
            List<Node> ordered = e.getValue().getStopsOrdered();
            System.out.println(" metro_id=" + e.getKey() + " ligne=" + e.getValue().getLigneId() + " stops=" + ordered.size());
            for (int i = 0; i < Math.min(5, ordered.size()); i++) {
                System.out.println("   " + ordered.get(i));
            }
        }

        // reverse mapping example
        if (!stations.isEmpty()) {
            String anyStopId = stations.keySet().iterator().next();
            System.out.println("\nExample station_id=" + anyStopId 
                + " served by lines=" + result.stopToRoutes().getOrDefault(anyStopId, java.util.List.of()).size());
        }

        System.out.println("Ordered stops of a metro line: ");
        for (Map.Entry<String, Metro> e : metros.entrySet()) {
            System.out.println("Metro: " + e.getKey() + " ligne=" + e.getValue().getLigneId());
            for (Node node : e.getValue().getStopsOrdered()) {
                System.out.println(node.getName());
            }
            break;
        }

        // build graph and add all parsed stops as nodes
        Graph g = new Graph();
        for (MetroStation ms : stations.values()) {
            g.addNode(ms);
        }

        // pick source/target from up to 5 candidate stops (configurable by CLI indexes)
        Node source = null;
        Node target = null;
        List<MetroStation> sortedStops = new ArrayList<>(stations.values());
        sortedStops.sort(Comparator.comparing(MetroStation::getId));

        int candidatesCount = Math.min(5, sortedStops.size());
        if (candidatesCount > 0) {
            System.out.println("\nStart/target candidates (1-based index):");
            for (int i = 0; i < candidatesCount; i++) {
                MetroStation c = sortedStops.get(i);
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
            System.out.println("No stations parsed, skipping shortest-path example.");
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