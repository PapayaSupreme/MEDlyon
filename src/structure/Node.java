package structure;

import java.util.HashMap;
import java.util.Map;

public abstract class Node {
    private final String id;
    private final String name;
    private final Coordinates coordinates;
    private final Map<Node, Distance> links = new HashMap<>();
    public Node(String id, String name, Coordinates coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    public String getId() { return this.id; }
    public String getName() { return this.name; }
    public Coordinates getCoordinates() { return this.coordinates; }
    public Map<Node, Distance> getLinks() { return Map.copyOf(this.links); }

    public void addLink(Node node, Distance distance) { this.links.put(node, distance); }

    abstract String getStopType();
    public String toString() {
        return this.name + "(" + this.id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return this.id.equals(node.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
