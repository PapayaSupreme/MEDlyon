package com.medlyon.backend.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TransitNode {
	private final String id;
	private final String name;
	private final double lat;
	private final double lng;
	private final Map<String, Double> links = new HashMap<>();

	public TransitNode(String id, String name, double lat, double lng) {
		this.id = id;
		this.name = name;
		this.lat = lat;
		this.lng = lng;
	}

	public String id() {
		return id;
	}

	public String name() {
		return name;
	}

	public double lat() {
		return lat;
	}

	public double lng() {
		return lng;
	}

	public Map<String, Double> links() {
		return Map.copyOf(links);
	}

	public void linkTo(String nodeId, double distanceMeters) {
		links.put(nodeId, distanceMeters);
	}

	public PointResponse toResponse() {
		return new PointResponse(lat, lng, name, List.of("stop_id=" + id));
	}

	public StopSummary toSummary() {
		return new StopSummary(id, lat, lng, name);
	}
}
