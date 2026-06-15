package parsers;

import structure.BusStop;
import structure.Coordinates;
import structure.Distance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple parser that reads a GTFS `stops.txt` and creates one BusStop instance per row.
 * It also links each stop to its nearest neighbor (undirected) using a Distance object.
 */
public class BusStopsParser {

    // Expected number of columns in the GTFS stops.txt used here
    private static final int COL_COUNT = 15;
    private static final int IDX_ID   = 0;
    private static final int IDX_NAME = 1;
    private static final int IDX_LAT  = 3;
    private static final int IDX_LON  = 4;

    public static List<BusStop> parse(String path) throws IOException {
        List<BusStop> stops = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean first = true;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (first) { first = false; continue; } // skip header
                if (line.trim().isEmpty()) continue;

                // keep empty trailing fields -> use split with -1
                String[] cols = line.split(",", -1);
                if (cols.length != COL_COUNT) {
                    System.err.println("Ignored line " + lineNo + " (cols=" + cols.length + "): " + line);
                    continue;
                }

                String sid = stripQuotes(cols[IDX_ID]);
                String name = stripQuotes(cols[IDX_NAME]);
                String sLat = stripQuotes(cols[IDX_LAT]);
                String sLon = stripQuotes(cols[IDX_LON]);

                if (sid.isEmpty() || sLat.isEmpty() || sLon.isEmpty()) {
                    // not enough data to build a node
                    continue;
                }

                try {
                    double lat = Double.parseDouble(sLat);
                    double lon = Double.parseDouble(sLon);
                    BusStop bs = new BusStop(sid, name, new Coordinates(lat, lon));
                    stops.add(bs);
                } catch (NumberFormatException e) {
                    System.err.println("Ignored line " + lineNo + " (bad numeric): " + sid + " / " + sLat + " / " + sLon);
                }
            }
        }

        // Build simple links: connect each stop with its nearest neighbor
        for (int i = 0; i < stops.size(); i++) {
            BusStop a = stops.get(i);
            double bestDist = Double.POSITIVE_INFINITY;
            int bestIdx = -1;
            for (int j = 0; j < stops.size(); j++) {
                if (i == j) continue;
                BusStop b = stops.get(j);
                double d = haversineMeters(a.getCoordinates().latitude(), a.getCoordinates().longitude(),
                        b.getCoordinates().latitude(), b.getCoordinates().longitude());
                if (d < bestDist) { bestDist = d; bestIdx = j; }
            }
            if (bestIdx != -1) {
                BusStop b = stops.get(bestIdx);
                // add bidirectional link if not already present
                a.addLink(b, new Distance(bestDist));
                b.addLink(a, new Distance(bestDist));
            }
        }

        return stops;
    }

    private static String stripQuotes(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    // Haversine formula -> distance in meters between two lat/lon points
    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth radius in m
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // Small main for quick manual test
    static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "raw_datasets/bus/lyon_tcl/stops.txt";
        List<BusStop> stops = parse(path);
        System.out.println("Loaded stops: " + stops.size());
        if (!stops.isEmpty()) System.out.println("First: " + stops.get(0) + " links=" + stops.get(0).getLinks().size());
    }
}
