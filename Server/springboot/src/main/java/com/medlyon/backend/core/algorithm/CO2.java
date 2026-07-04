package Core;

import structure.BusStop;
import structure.Distance;
import structure.MetroStop;
import structure.Node;
import utilities.Costs;

import java.util.List;
import java.util.Map;

public class CO2 {
    private static final int busRatio = 132;
    private static final int metroRatio = 4;

    public static double computeRouteCO2(List<Node> itinerary) {
        if (itinerary == null || itinerary.size() < 2) {
            return 0.0;
        }

        double totalGrams = 0.0;
        for (int i = 0; i < itinerary.size() - 1; i++) {
            Node current = itinerary.get(i);
            Node next = itinerary.get(i + 1);
            int ratio = getSegmentRatio(current, next);

            if (ratio == 0) {
                continue;
            }

            double distanceKm = getSegmentDistanceMeters(current, next) / 1000.0;
            totalGrams += distanceKm * ratio;
        }

        return totalGrams;
    }

    private static int getSegmentRatio(Node current, Node next) {
        if (current instanceof MetroStop && next instanceof MetroStop) {
            return metroRatio;
        }
        if (current instanceof BusStop && next instanceof BusStop) {
            return busRatio;
        }
        return 0;
    }

    private static double getSegmentDistanceMeters(Node current, Node next) {
        Map<Node, Distance> links = current.getLinks();
        Distance distance = links.get(next);
        if (distance != null) {
            return distance.meters();
        }
        return Costs.computeCost(current, next, "Haversine");
    }
}
