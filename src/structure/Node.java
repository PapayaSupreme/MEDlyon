package structure;

import java.util.HashMap;
import java.util.Map;

public class Node {
    private final int id;
    private final String name;
    private final Coordinates coordinates;
    private final Map<Node, Distance> links = new HashMap<>();
    public Node(int id, String name, Coordinates coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public Coordinates getCoordinates() { return this.coordinates; }
    public Map<Node, Distance> getLinks() { return Map.copyOf(this.links); }

    public void addLink(Node node, Distance distance) { this.links.put(node, distance); }
}
