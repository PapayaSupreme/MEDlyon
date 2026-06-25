package Core;

import structure.Graph;
import structure.Node;
import structure.Distance;
import utilities.Costs;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Optional;

/**
 * Simple Dijkstra implementation adapted to the project's Node/Distance model.
 * <p>
 * It uses the precomputed Distance (meters) on each link when available and
 * falls back to Haversine via {@link utilities.Costs} when needed.
 */
public class Dijkstra {

    public static class ShortestPathsResult {
        private final Map<Node, Double> distances;
        private final Map<Node, Node> previous;

        public ShortestPathsResult(Map<Node, Double> distances, Map<Node, Node> previous) {
            this.distances = distances;
            this.previous = previous;
        }

        public double getDistance(Node n) { return distances.getOrDefault(n, Double.POSITIVE_INFINITY); }
        public Optional<Node> getPrevious(Node n) { return Optional.ofNullable(previous.get(n)); }
        public Map<Node, Double> getDistances() { return Collections.unmodifiableMap(distances); }
        public Map<Node, Node> getPreviousMap() { return Collections.unmodifiableMap(previous); }

        public List<Node> reconstructPath(Node target) {
            List<Node> path = new ArrayList<>();
            Node cur = target;
            while (cur != null && previous.containsKey(cur)) {
                path.addFirst(cur);
                cur = previous.get(cur);
            }
            if (cur != null && distances.containsKey(cur) && distances.get(cur) == 0.0) {
                path.addFirst(cur);
            }
            return path;
        }
    }

    /**
     * Compute the shortest paths from source to all nodes in graph using link weights.
     * Uses Distance.meters when present, otherwise falls back to Costs.computeCost(...,"Haversine").
     */
    public static ShortestPathsResult computeShortestPaths(Graph graph, Node source) {
        Map<Node, Double> dist = new HashMap<>();
        Map<Node, Node> prev = new HashMap<>();

        for (Node n : graph.getNodes()) {
            dist.put(n, Double.POSITIVE_INFINITY);
        }
        if (!dist.containsKey(source)) {
            // include source even if not in graph
            dist.put(source, Double.POSITIVE_INFINITY);
        }
        dist.put(source, 0.0);

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.addAll(dist.keySet());

        while (!pq.isEmpty()) {
            Node u = pq.poll();
            double du = dist.getOrDefault(u, Double.POSITIVE_INFINITY);
            if (Double.isInfinite(du)) break; // remaining nodes unreachable

            // iterate neighbors
            for (Map.Entry<Node, Distance> e : u.getLinks().entrySet()) {
                Node v = e.getKey();
                Distance link = e.getValue();
                double w;
                if (link != null) {
                    w = link.meters();
                } else {
                    // fallback to Haversine between coordinates
                    w = Costs.computeCost(u, v, "Haversine");
                }
                double alt = du + w;
                double dv = dist.getOrDefault(v, Double.POSITIVE_INFINITY);
                if (alt < dv) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    // decrease-key: remove and re-add to update ordering
                    pq.remove(v);
                    pq.add(v);
                }
            }
        }

        return new ShortestPathsResult(dist, prev);
    }
}
