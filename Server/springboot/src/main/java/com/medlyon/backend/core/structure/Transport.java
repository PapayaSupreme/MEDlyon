package com.medlyon.backend.core.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a transport run (e.g. a trip) and keeps an ordered set of stops.
 * Ordering is provided by a custom comparator that reads stop sequence numbers
 * from an internal map populated when stops are added. This lets you build
 * the order from GTFS `stop_times.txt` (stop_sequence) and then answer
 * questions like "what is the next stop after X".
 */
public abstract class Transport {
    private final String id;

    protected Transport(String id) {
        this.id = id;
    }

    public String getId() { return this.id; }

    // map Node -> stop_sequence (as provided by stop_times.txt)
    private final Map<Node, Integer> sequenceMap = new HashMap<>();

    // comparator that orders by sequence number, fall back to id to break ties
    private final Comparator<Node> seqComparator = (a, b) -> {
        int sa = sequenceMap.getOrDefault(a, Integer.MAX_VALUE);
        int sb = sequenceMap.getOrDefault(b, Integer.MAX_VALUE);
        if (sa != sb) return Integer.compare(sa, sb);
        return a.getId().compareTo(b.getId());
    };

    // tree set keeps nodes in the order defined by seqComparator
    private final Set<Node> stops = new TreeSet<>(seqComparator);

    // add or update a stop with its sequence number
    public void addStop(Node node, int sequence) {
        sequenceMap.put(node, sequence);
        // TreeSet will use the comparator; if comparator order changes due to
        // updating the map, we need to remove+re-add to re-balance the set
        stops.remove(node);
        stops.add(node);
    }

    // returns the stops in order as a list
    public List<Node> getStopsOrdered() {
        List<Node> list = new ArrayList<>(stops);
        list.sort(seqComparator);
        return list;
    }
}
