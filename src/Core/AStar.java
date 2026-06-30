package Core;

import structure.Distance;
import structure.Graph;
import structure.Node;
import utilities.Costs;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A* pathfinding implementation for the project's Node/Distance model.
 * <p>
 * The algorithm uses link distances when present and falls back to Haversine
 * via {@link utilities.Costs} for both edge weights and the heuristic.
 */
public class AStar {

    public static class PathResult {
        private final Map<Node, Double> distances;
        private final Map<Node, Node> previous;

        public PathResult(Map<Node, Double> distances, Map<Node, Node> previous) {
            this.distances = distances;
            this.previous = previous;
        }

        public double getDistance(Node n) {
            return distances.getOrDefault(n, Double.POSITIVE_INFINITY);
        }

        public Optional<Node> getPrevious(Node n) {
            return Optional.ofNullable(previous.get(n));
        }

        public Map<Node, Double> getDistances() {
            return Map.copyOf(distances);
        }

        public Map<Node, Node> getPreviousMap() {
            return Map.copyOf(previous);
        }

        public List<Node> reconstructPath(Node target) {
            return new Dijkstra.ShortestPathsResult(distances, previous).reconstructPath(target);
        }
    }

    /**
     * Computes the shortest path from source to target using A*.
     * The returned result contains the explored distances and predecessor map,
     * and can reconstruct the final path to the target.
     */
    public static PathResult computeShortestPath(Graph graph, Node source, Node target) {
        Map<Node, Double> gScore = new HashMap<>();
        Map<Node, Double> fScore = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        Set<Node> closedSet = new HashSet<>();

        for (Node n : graph.getNodes()) {
            gScore.put(n, Double.POSITIVE_INFINITY);
            fScore.put(n, Double.POSITIVE_INFINITY);
        }
        if (!gScore.containsKey(source)) {
            gScore.put(source, Double.POSITIVE_INFINITY);
            fScore.put(source, Double.POSITIVE_INFINITY);
        }
        if (!gScore.containsKey(target)) {
            gScore.put(target, Double.POSITIVE_INFINITY);
            fScore.put(target, Double.POSITIVE_INFINITY);
        }

        gScore.put(source, 0.0);
        fScore.put(source, heuristic(source, target));

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> fScore.getOrDefault(n, Double.POSITIVE_INFINITY)));
        openSet.add(source);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.equals(target)) {
                break;
            }
            if (!closedSet.add(current)) {
                continue;
            }

            double currentG = gScore.getOrDefault(current, Double.POSITIVE_INFINITY);
            if (Double.isInfinite(currentG)) {
                continue;
            }

            for (Map.Entry<Node, Distance> entry : current.getLinks().entrySet()) {
                Node neighbor = entry.getKey();
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeG = currentG + edgeCost(current, neighbor, entry.getValue());
                double knownG = gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY);
                if (tentativeG < knownG) {
                    previous.put(neighbor, current);
                    gScore.put(neighbor, tentativeG);
                    fScore.put(neighbor, tentativeG + heuristic(neighbor, target));
                    openSet.remove(neighbor);
                    openSet.add(neighbor);
                }
            }
        }

        return new PathResult(gScore, previous);
    }

    private static double edgeCost(Node from, Node to, Distance link) {
        if (link != null) {
            return link.meters();
        }
        return Costs.computeCost(from, to, "Haversine");
    }

    private static double heuristic(Node from, Node to) {
        return Costs.computeCost(from, to, "Haversine");
    }
}
