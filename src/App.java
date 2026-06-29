import parsers.MetroParser;
import structure.Metro;
import structure.MetroStop;
import structure.MetroStop;
import structure.Node;
import structure.Graph;
import parsers.BusParser;
import structure.Bus;
import structure.BusStop;
import structure.Distance;
import structure.Coordinates;
import structure.Distance;
import structure.Coordinates;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.Normalizer;
import java.util.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

import Core.Dijkstra;
import Core.Dijkstra.ShortestPathsResult;
import utilities.Tools;

import static utilities.Tools.haversineMeters;


public class App{
    // Transfer distance threshold
    private static final double TRANSFER_DISTANCE_THRESHOLD = 120.0;

    static void main(String[] args) throws Exception{
        String baseBus = args.length > 0 ? args[0] : "raw_datasets/bus/lyon_tcl";
        String baseMetro = args.length > 1 ? args[1] : "raw_datasets/metro";

        boolean cli = (args.length > 2) && (args[2].equals("-noCli")) ? false : true;

        String stopsPath = baseBus + "/stops.txt";
        String tripsPath = baseBus + "/trips.txt";
        String stopTimesPath = baseBus + "/stop_times.txt";

        // Parse Bus Stops and Trips
        System.out.println("=".repeat(60));
        System.out.println("PARSING BUS NETWORK");
        System.out.println("=".repeat(60));

        System.out.println("Parsing bus stops from: " + stopsPath);
        Map<String, BusStop> busStops = BusParser.parseStops(stopsPath);
        System.out.println("✓ Parsed " + busStops.size() + " bus stops");
        int shown = 0;
        for (Map.Entry<String, BusStop> e : busStops.entrySet()) {
            if (shown++ >= 5) break;
            System.out.println(" stop_id=" + e.getKey() + " -> " + e.getValue());
        }

        System.out.println("\nParsing bus stop_times from: " + stopTimesPath);
        BusParser.Result result = BusParser.parseStopTimes(stopTimesPath, tripsPath, busStops);
        Map<String, Bus> buses = result.transports();
        System.out.println("✓ Parsed " + buses.size() + " bus routes");
        shown = 0;
        for (Map.Entry<String, Bus> e : buses.entrySet()) {
            if (shown++ >= 5) break;
            List<Node> ordered = e.getValue().getStopsOrdered();
            System.out.println(" trip_id=" + e.getKey() + " route=" + e.getValue().getRouteId() + " stops=" + ordered.size());
            for (int i = 0; i < Math.min(5, ordered.size()); i++) {
                System.out.println("   " + ordered.get(i));
            }
        }

        if (!busStops.isEmpty()) {
            String anyStopId = busStops.keySet().iterator().next();
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

        // Parse Metro Stops and Network
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PARSING METRO NETWORK");
        System.out.println("=".repeat(60));

        Map<String, MetroStop> metroStops = new java.util.HashMap<>();
        Map<String, Metro> metros = new java.util.HashMap<>();

        try {
            String metroStationsPath = baseMetro + "/stations-metro-reseau-transports-commun-lyonnais.csv";
            String metroHorairesPath = baseMetro + "/horaires_tcl.csv";
            File stationsFile = new File(metroStationsPath);
            File horairesFile = new File(metroHorairesPath);

            if (stationsFile.exists() && horairesFile.exists()) {
                // Load metro stops by id
                System.out.println("Parsing metro stops from: " + metroStationsPath);
                metroStops = MetroParser.parseStops(metroStationsPath);
                System.out.println("✓ Parsed " + metroStops.size() + " metro stops");

                // Build index by name for horaires matching
                Map<String, MetroStop> stopsByName = new java.util.HashMap<>();
                Map<String, MetroStop> stopsById = new java.util.HashMap<>();
                for (MetroStop ms : metroStops.values()) {
                    System.out.println(ms.getName());
                    if (ms == null) continue;
                    String idNorm = normalizeId(ms.getId());
                    if (!idNorm.isEmpty()) stopsById.put(idNorm, ms);

                    String name = ms.getName();
                    if (name != null && !name.isBlank()) {
                        String nameNorm = normalizeName(name);
                        if (!nameNorm.isEmpty()) stopsByName.put(nameNorm, ms);
                    }
                }
                Map<String, MetroStop> aliasMap = buildAliasMap(metroStops.values());

                // Link metro stops together
                System.out.println("Linking metro stops from: " + metroHorairesPath);
                MetroParser.parseAndLinkHoraires(metroHorairesPath, metroStops);
                System.out.println("✓ Metro stops linked");

                // Build Metro objects (one instance per line+direction)
                Map<String, List<MetroStop>> orderMap = new java.util.LinkedHashMap<>();

                try (BufferedReader br = new BufferedReader(new FileReader(metroHorairesPath))) {
                    String header = br.readLine();
                    if (header != null) {
                        header = header.stripLeading();
                        if (header.startsWith("#")) header = header.substring(1);
                        if (header.startsWith("\uFEFF")) header = header.substring(1);
                    }

                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] cols = line.split(";", -1);

                        String stationRaw = safeGet(cols, 0);
                        String ligne = safeGet(cols, 1);
                        String sens = safeGet(cols, 2);

                        if (stationRaw.isBlank() || ligne.isBlank() || sens.isBlank()) continue;

                        String key = ligne.trim() + "|" + sens.trim();

                        // Normaliser la valeur lue pour la recherche
                        String normalizedStation = normalizeName(stationRaw);

                        // Chercher par nom normalisé
                        MetroStop ms = stopsByName.get(normalizedStation);

                        // Si non trouvé par nom, essayer par id (parfois le fichier horaires contient des ids)
                        if (ms == null) {
                            String maybeId = normalizeId(stationRaw);
                            ms = stopsById.get(maybeId);
                        }

                        // Dernier essai : nettoyer guillemets simples/autres et retenter
                        if (ms == null) {
                            String cleaned = stationRaw.replace("\"", "").replace("'", "").trim();
                            ms = stopsByName.get(normalizeName(cleaned));
                            if (ms == null) ms = stopsById.get(normalizeId(cleaned));
                        }

                        // Si toujours introuvable, tenter une suggestion fuzzy via aliasMap
                        if (ms == null) {
                            MetroStop suggestion = findClosestStop(normalizedStation, aliasMap);
                            if (suggestion != null) {
                                System.err.println("Warning: station introuvable raw='" + stationRaw + "'; suggestion: '" + suggestion.getName() + "' id=" + suggestion.getId());
                                // Ajouter l'alias pour les prochaines lignes et utiliser la suggestion
                                String aliasKey = normalizeName(stationRaw);
                                aliasMap.put(aliasKey, suggestion);
                                stopsByName.putIfAbsent(aliasKey, suggestion);
                                ms = suggestion;
                            } else {
                                System.err.println("Warning: station introuvable dans stops pour ligne '" + ligne + "', sens '" + sens + "': raw='" + stationRaw + "'");
                                continue;
                            }
                        }

                        List<MetroStop> list = orderMap.computeIfAbsent(key, k -> new ArrayList<>());
                        list.add(ms);
                    }
                }

                // Create Metro objects
                for (Map.Entry<String, List<MetroStop>> e : orderMap.entrySet()) {
                    String key = e.getKey();
                    String[] parts = key.split("\\|", 2);
                    String ligne = parts[0];
                    String sens = parts.length > 1 ? parts[1] : "unknown";

                    // id du Metro : ligne + "_" + sens (sanitized)
                    String metroId = sanitizeIdForObject(ligne + "_" + sens);
                    Metro metro = new Metro(metroId, ligne);

                    List<MetroStop> stopsList = e.getValue();
                    int seq = 1;
                    for (MetroStop ms : stopsList) {
                        metro.addStop(ms, seq++);
                    }
                    metros.put(key, metro);
                }

                System.out.println("✓ Created " + metros.size() + " metro lines (line|direction)");
                shown = 0;
                for (Map.Entry<String, Metro> e : metros.entrySet()) {
                    if (shown++ >= 5) break;
                    Metro m = e.getValue();
                    List<Node> ordered = m.getStopsOrdered();
                    System.out.println(" line=" + m.getRouteId() + " direction=" + e.getKey().split("\\|")[1] + " stops=" + ordered.size());
                }
            } else {
                System.out.println("⚠ Metro dataset files not found at: " + metroStationsPath + " or " + metroHorairesPath);
            }
        } catch (Exception e) {
            System.out.println("⚠ Error parsing metro data: " + e.getMessage());
            e.printStackTrace();
        }

        // Build integrated graph with bus and metro stops
        System.out.println("\n" + "=".repeat(60));
        System.out.println("BUILDING INTEGRATED NETWORK");
        System.out.println("=".repeat(60));

        Graph g = new Graph();

        // Add all bus stops
        for (BusStop bs : busStops.values()) {
            g.addNode(bs);
        }
        System.out.println("✓ Added " + busStops.size() + " bus stops to graph");

        // Add all metro stops
        for (MetroStop ms : metroStops.values()) {
            g.addNode(ms);
        }
        System.out.println("✓ Added " + metroStops.size() + " metro stops to graph");

        // Create transfer links between nearby bus and metro stops
        if (!metroStops.isEmpty() && !busStops.isEmpty()) {
            createTransferLinks(busStops, metroStops);
            System.out.println("✓ Transfer links created between bus and metro stops");
        }

        System.out.println("✓ Total nodes in graph: " + g.getNodes().size());

        // Combine all stops for selection and sorting
        List<Node> allStops = new ArrayList<>();
        allStops.addAll(busStops.values());
        allStops.addAll(metroStops.values());
        allStops.sort(Comparator.comparing(Node::getId));

        // pick source/target from up to 5 candidate stops (configurable by CLI indexes)
        Node source = null;
        Node target = null;

        int candidatesCount = Math.min(5, allStops.size());
        if (candidatesCount > 0) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("START/TARGET CANDIDATES");
            System.out.println("=".repeat(60));
            for (int i = 0; i < candidatesCount; i++) {
                Node c = allStops.get(i);
                String type = c instanceof MetroStop ? "🚇 METRO" : "🚌 BUS";
                System.out.println(" [" + (i + 1) + "] " + type + " " + c.getId() + " - " + c.getName());
            }

            int defaultStartIndex = 1;
            int defaultTargetIndex = Math.min(2, candidatesCount);

            int startIndex = defaultStartIndex;
            int targetIndex = defaultTargetIndex;

            if (args.length > 2) {
                try { startIndex = Integer.parseInt(args[2]); } catch (NumberFormatException ignored) {}
            }
            if (args.length > 3) {
                try { targetIndex = Integer.parseInt(args[3]); } catch (NumberFormatException ignored) {}
            }

            if (startIndex < 1 || startIndex > candidatesCount) startIndex = defaultStartIndex;
            if (targetIndex < 1 || targetIndex > candidatesCount) targetIndex = defaultTargetIndex;

            source = allStops.get(startIndex - 1);
            target = allStops.get(targetIndex - 1);

            System.out.println("Selected start: [" + startIndex + "] " + source);
            System.out.println("Selected target: [" + targetIndex + "] " + target);
        }

        if (source == null) {
            System.out.println("No stops parsed, skipping shortest-path example.");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("DIJKSTRA SHORTEST PATH COMPUTATION");
        System.out.println("=".repeat(60));

        ShortestPathsResult res = Dijkstra.computeShortestPaths(g, source);

        // distance in meters from source to target
        double meters = res.getDistance(target);

        // get predecessor map or reconstruct the path
        List<Node> path = res.reconstructPath(target);
        if (path.isEmpty()) {
            System.out.println("✗ No path found");
        } else {
            System.out.println("✓ Path found!");
            System.out.println("Distance (m): " + meters);
            System.out.println("Number of stops: " + path.size());
            System.out.println("\nItinerary:");
            for (int i = 0; i < path.size(); i++) {
                Node n = path.get(i);
                String type = n instanceof MetroStop ? "🚇" : "🚌";
                System.out.println("  " + (i + 1) + ". " + type + " " + n);
            }
        }

        // Interactive menu for route searching if the user did not purposefully refuse it.
        if (cli){
            interactiveRouteSearch(g, allStops);
        }

    }

    /**
     * Creates bidirectional transfer links between nearby bus and metro stops.
     * Stops within TRANSFER_DISTANCE_THRESHOLD meters are connected.
     */
    private static void createTransferLinks(Map<String, BusStop> busStops, Map<String, MetroStop> metroStops) {
        int transferCount = 0;
        for (BusStop busStop : busStops.values()) {
            for (MetroStop metroStop : metroStops.values()) {
                double distance = haversineMeters(
                        busStop.getCoordinates().latitude(), busStop.getCoordinates().longitude(),
                        metroStop.getCoordinates().latitude(), metroStop.getCoordinates().longitude()
                );

                if (distance <= TRANSFER_DISTANCE_THRESHOLD) {
                    busStop.addLink(metroStop, new Distance(distance));
                    metroStop.addLink(busStop, new Distance(distance));
                    transferCount++;
                }
            }
        }
        System.out.println("  Created " + transferCount + " transfer connections (threshold=" + TRANSFER_DISTANCE_THRESHOLD + "m)");
    }

    private static void interactiveRouteSearch(Graph g, List<Node> allStops) {
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
            String departType = departNode instanceof MetroStop ? "🚇 METRO" : "🚌 BUS";
            String arriveType = arriveNode instanceof MetroStop ? "🚇 METRO" : "🚌 BUS";
            System.out.println("From: " + departType + " " + departNode.getName() + " (" + departNode.getId() + ")");
            System.out.println("To:   " + arriveType + " " + arriveNode.getName() + " (" + arriveNode.getId() + ")");

            if (path.isEmpty()) {
                System.out.println("\n✗ No route found between these stops.");
            } else {
                System.out.println("\n✓ Route found!");
                System.out.println("Total distance: " + String.format("%.2f", distance) + " meters");
                System.out.println("CO2: " + String.format("%.2f", CO2.computeRouteCO2(path)) + " g/voyager");
                System.out.println("Number of stops: " + path.size());
                System.out.println("\nItinerary:");

                for (int i = 0; i < path.size(); i++) {
                    Node stop = path.get(i);
                    String type = stop instanceof MetroStop ? "🚇" : "🚌";
                    System.out.println("  " + (i + 1) + ". " + type + " " + stop.getName() + " (" + stop.getId() + ")");
                }

                // Calculate and display segment distances
                System.out.println("\nDetailed segments:");
                for (int i = 0; i < path.size() - 1; i++) {
                    Node currentStop = path.get(i);
                    Node nextStop = path.get(i + 1);
                    Map<Node, Distance> links = currentStop.getLinks();

                    if (links.containsKey(nextStop)) {
                        double segmentDistance = links.get(nextStop).meters();
                        String currentType = currentStop instanceof MetroStop ? "🚇" : "🚌";
                        String nextType = nextStop instanceof MetroStop ? "🚇" : "🚌";
                        System.out.println("  " + (i + 1) + " → " + (i + 2) + ": " + currentType + " " + currentStop.getName()
                                + " to " + nextType + " " + nextStop.getName() + " (" + String.format("%.0f", segmentDistance) + "m)");
                    }
                }
            }

            System.out.println("\nComputation time: " + String.format("%.4f", durationMillis) + " ms (" + durationNanos + " ns)");
            System.out.println("-".repeat(60));
        }

        scanner.close();
        System.out.println("\nGoodbye!");
    }

    private static Node selectStop(Scanner scanner, List<Node> allStops, String label) {
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

    private static Node selectByNumber(Scanner scanner, List<Node> allStops, String label) {
        System.out.println("\nAvailable stops:");
        for (int i = 0; i < allStops.size(); i++) {
            Node stop = allStops.get(i);
            String type = stop instanceof MetroStop ? "🚇" : "🚌";
            System.out.println("  [" + (i + 1) + "] " + type + " " + stop.getId() + " - " + stop.getName());
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
                Node selected = allStops.get(index);
                String type = selected instanceof MetroStop ? "🚇 METRO" : "🚌 BUS";
                System.out.println("✓ Selected: " + type + " " + selected.getName() + " (" + selected.getId() + ")");
                return selected;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
    private static String safeGet(String[] cols, int idx) {
        if (cols == null || idx < 0 || idx >= cols.length) return "";
        return cols[idx];
    }
        private static String normalizeName(String s) {
            if (s == null) return "";
            s = s.trim();
            if (s.isEmpty()) return "";
            // remove BOM
            if (s.charAt(0) == '\uFEFF') s = s.substring(1);
            // remove surrounding quotes
            if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
                s = s.substring(1, s.length() - 1);
            }
            // replace hyphens/slashes by spaces, remove common punctuation
            s = s.replaceAll("[-/]", " ");
            s = s.replaceAll("[.,()]", " ");
            // collapse whitespace
            s = s.replaceAll("\\s+", " ").trim();
            // remove accents
            s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
            return s.toLowerCase(Locale.ROOT);
        }

        // Normalise un id (trim + toLowerCase)
        private static String normalizeId(String id) {
            if (id == null) return "";
            String s = id.trim();
            if (s.isEmpty()) return "";
            if (s.charAt(0) == '\uFEFF') s = s.substring(1);
            return s.toLowerCase(Locale.ROOT);
        }

        // Sanitize simple id for object naming (remplace espaces et caractères problématiques)
        private static String sanitizeIdForObject(String raw) {
            if (raw == null) return "unknown";
            String s = raw.trim().replaceAll("\\s+", "_");
            s = s.replaceAll("[^A-Za-z0-9_\\-]", "_");
            return s;
        }

        // Build alias map from stops: for each MetroStop add several normalized keys
        private static Map<String, MetroStop> buildAliasMap(Collection<MetroStop> stops) {
            Map<String, MetroStop> alias = new HashMap<>();
            for (MetroStop ms : stops) {
                if (ms == null) continue;
                String idNorm = normalizeId(ms.getId());
                if (!idNorm.isEmpty()) alias.put(idNorm, ms);

                String nameNorm = normalizeName(ms.getName());
                if (!nameNorm.isEmpty()) alias.put(nameNorm, ms);

                // additional variants: remove spaces, remove dots
                alias.putIfAbsent(nameNorm.replaceAll("\\s+", ""), ms);
                alias.putIfAbsent(nameNorm.replace(".", ""), ms);
            }
            return alias;
        }

        // Simple Levenshtein distance (iterative DP)
        private static int levenshtein(String a, String b) {
            if (a == null) a = "";
            if (b == null) b = "";
            int n = a.length(), m = b.length();
            if (n == 0) return m;
            if (m == 0) return n;
            int[] prev = new int[m + 1];
            int[] cur = new int[m + 1];
            for (int j = 0; j <= m; j++) prev[j] = j;
            for (int i = 1; i <= n; i++) {
                cur[0] = i;
                for (int j = 1; j <= m; j++) {
                    int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                    cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
                }
                int[] tmp = prev; prev = cur; cur = tmp;
            }
            return prev[m];
        }

        // Find closest stop by normalized name; returns null if no candidate under threshold
        private static MetroStop findClosestStop(String normalized, Map<String, MetroStop> aliasMap) {
            if (normalized == null || normalized.isEmpty()) return null;
            Map<MetroStop, Integer> best = new HashMap<>();
            for (Map.Entry<String, MetroStop> e : aliasMap.entrySet()) {
                String key = e.getKey();
                MetroStop ms = e.getValue();
                int dist = levenshtein(normalized, key);
                best.merge(ms, dist, Math::min);
            }
            return best.entrySet().stream()
                    .min(Comparator.comparingInt(Map.Entry::getValue))
                    .filter(en -> en.getValue() <= Math.max(2, normalized.length() / 6)) // seuil adaptatif
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }

        // utilitaire pour afficher la distance entre deux MetroStop si un lien existe
        private static String distanceBetween(MetroStop a, MetroStop b) {
            if (a == null || b == null) return "n/a";
            Map<Node, Distance> links = a.getLinks();
            Distance d = links.get(b);
            if (d != null) return d.toString();
            // sinon calculer via Haversine (approx)
            double meters = Tools.haversineMeters(
                    a.getCoordinates().latitude(), a.getCoordinates().longitude(),
                    b.getCoordinates().latitude(), b.getCoordinates().longitude()
            );
            return String.format("%.1fm (haversine)", meters);
        }
    private static Node selectBySearch(Scanner scanner, List<Node> allStops, String label) {
        System.out.print("Enter stop name to search (or 'b' to go back): ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        if (searchTerm.equals("b")) {
            return selectStop(scanner, allStops, label);
        }

        List<Node> matches = new ArrayList<>();
        for (Node stop : allStops) {
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
            Node stop = matches.get(i);
            String type = stop instanceof MetroStop ? "🚇" : "🚌";
            System.out.println("  [" + (i + 1) + "] " + type + " " + stop.getId() + " - " + stop.getName());
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
                Node selected = matches.get(index);
                String type = selected instanceof MetroStop ? "🚇 METRO" : "🚌 BUS";
                System.out.println("✓ Selected: " + type + " " + selected.getName() + " (" + selected.getId() + ")");
                return selected;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}
