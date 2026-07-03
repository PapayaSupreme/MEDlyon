package com.medlyon.backend.service;

import com.medlyon.backend.model.PathResponse;
import com.medlyon.backend.model.PointResponse;
import com.medlyon.backend.model.StopSummary;
import com.medlyon.backend.model.TransitNode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class TransitService {

	@Value("${medlyon.data-dir:}")
	private String configuredDataDir;

	private final Map<String, TransitNode> nodesById = new LinkedHashMap<>();
	private final Map<String, List<TripStop>> tripStops = new HashMap<>();

	@PostConstruct
	void init() throws IOException {
		Path dataDir = resolveDataDir();
		loadStops(dataDir.resolve("stops.txt"));
		loadTrips(dataDir.resolve("trips.txt"));
		loadStopTimes(dataDir.resolve("stop_times.txt"));
		linkTrips();
	}

	public PointResponse findByName(String nodeName) {
		if (nodeName == null || nodeName.isBlank()) {
			throw new IllegalArgumentException("Nodename is required");
		}

		String needle = nodeName.trim().toLowerCase(Locale.ROOT);
		Optional<TransitNode> exact = nodesById.values().stream()
				.filter(node -> node.id().equalsIgnoreCase(needle) || node.name().equalsIgnoreCase(nodeName.trim()))
				.findFirst();
		if (exact.isPresent()) {
			return exact.get().toResponse();
		}

		return nodesById.values().stream()
				.filter(node -> node.name().toLowerCase(Locale.ROOT).contains(needle) || node.id().toLowerCase(Locale.ROOT).contains(needle))
				.min(Comparator.comparing(node -> node.name().length()))
				.map(TransitNode::toResponse)
				.orElseThrow(() -> new IllegalArgumentException("Unknown node: " + nodeName));
	}

	public PointResponse findClosestNode(double lat, double lng) {
		TransitNode closest = nodesById.values().stream()
				.min(Comparator.comparingDouble(node -> haversineMeters(lat, lng, node.lat(), node.lng())))
				.orElseThrow(() -> new IllegalStateException("No transit nodes loaded"));
		return closest.toResponse();
	}

	public List<StopSummary> listStops() {
		return nodesById.values().stream()
				.sorted(Comparator.comparing(TransitNode::name).thenComparing(TransitNode::id))
				.map(TransitNode::toSummary)
				.toList();
	}

	public PathResponse computePath(double slat, double slng, double elat, double elng) {
		TransitNode start = nearestNode(slat, slng);
		TransitNode end = nearestNode(elat, elng);

		if (start.id().equals(end.id())) {
			return new PathResponse(List.of(start.toResponse()));
		}

		Map<String, Double> distance = new HashMap<>();
		Map<String, String> previous = new HashMap<>();
		Set<String> visited = new HashSet<>();
		for (String id : nodesById.keySet()) {
			distance.put(id, Double.POSITIVE_INFINITY);
		}
		distance.put(start.id(), 0.0);

		while (true) {
			String currentId = null;
			double bestDistance = Double.POSITIVE_INFINITY;
			for (Map.Entry<String, Double> entry : distance.entrySet()) {
				if (!visited.contains(entry.getKey()) && entry.getValue() < bestDistance) {
					bestDistance = entry.getValue();
					currentId = entry.getKey();
				}
			}

			if (currentId == null || Double.isInfinite(bestDistance)) {
				break;
			}
			if (currentId.equals(end.id())) {
				break;
			}

			visited.add(currentId);
			TransitNode current = nodesById.get(currentId);
			for (Map.Entry<String, Double> link : current.links().entrySet()) {
				double candidate = bestDistance + link.getValue();
				if (candidate < distance.getOrDefault(link.getKey(), Double.POSITIVE_INFINITY)) {
					distance.put(link.getKey(), candidate);
					previous.put(link.getKey(), currentId);
				}
			}
		}

		Deque<PointResponse> result = new ArrayDeque<>();
		String cursor = end.id();
		if (!previous.containsKey(cursor) && !cursor.equals(start.id())) {
			return new PathResponse(List.of(start.toResponse(), end.toResponse()));
		}

		result.addFirst(end.toResponse());
		while (previous.containsKey(cursor)) {
			cursor = previous.get(cursor);
			result.addFirst(nodesById.get(cursor).toResponse());
		}
		if (!result.isEmpty() && !result.peekFirst().name().equals(start.name())) {
			result.addFirst(start.toResponse());
		}
		return new PathResponse(new ArrayList<>(result));
	}

	private TransitNode nearestNode(double lat, double lng) {
		return nodesById.values().stream()
				.min(Comparator.comparingDouble(node -> haversineMeters(lat, lng, node.lat(), node.lng())))
				.orElseThrow(() -> new IllegalStateException("No transit nodes loaded"));
	}

	private void loadStops(Path stopsPath) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(stopsPath, StandardCharsets.UTF_8)) {
			CsvColumns columns = CsvColumns.fromHeader(reader.readLine());
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}
				List<String> cells = splitCsv(line);
				String id = columns.value(cells, "stop_id");
				String name = columns.value(cells, "stop_name");
				String latValue = columns.value(cells, "stop_lat");
				String lngValue = columns.value(cells, "stop_lon");
				if (id.isBlank() || latValue.isBlank() || lngValue.isBlank()) {
					continue;
				}
				try {
					double lat = Double.parseDouble(latValue);
					double lng = Double.parseDouble(lngValue);
					nodesById.put(id, new TransitNode(id, name.isBlank() ? id : name, lat, lng));
				} catch (NumberFormatException ignored) {
				}
			}
		}
	}

	private void loadTrips(Path tripsPath) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(tripsPath, StandardCharsets.UTF_8)) {
			CsvColumns columns = CsvColumns.fromHeader(reader.readLine());
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}
				List<String> cells = splitCsv(line);
				String tripId = columns.value(cells, "trip_id");
				if (!tripId.isBlank()) {
					tripStops.putIfAbsent(tripId, new ArrayList<>());
				}
			}
		}
	}

	private void loadStopTimes(Path stopTimesPath) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(stopTimesPath, StandardCharsets.UTF_8)) {
			CsvColumns columns = CsvColumns.fromHeader(reader.readLine());
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}
				List<String> cells = splitCsv(line);
				String tripId = columns.value(cells, "trip_id");
				String stopId = columns.value(cells, "stop_id");
				String sequenceValue = columns.value(cells, "stop_sequence");
				if (tripId.isBlank() || stopId.isBlank() || sequenceValue.isBlank() || !nodesById.containsKey(stopId)) {
					continue;
				}
				try {
					int sequence = Integer.parseInt(sequenceValue);
					tripStops.computeIfAbsent(tripId, ignored -> new ArrayList<>()).add(new TripStop(stopId, sequence));
				} catch (NumberFormatException ignored) {
				}
			}
		}
	}

	private void linkTrips() {
		for (List<TripStop> stops : tripStops.values()) {
			List<TripStop> ordered = stops.stream()
					.filter(stop -> !stop.stopId().isBlank())
					.sorted(Comparator.comparingInt(TripStop::sequence))
					.toList();
			for (int i = 0; i + 1 < ordered.size(); i++) {
				TransitNode a = nodesById.get(ordered.get(i).stopId());
				TransitNode b = nodesById.get(ordered.get(i + 1).stopId());
				if (a == null || b == null) {
					continue;
				}
				double distance = haversineMeters(a.lat(), a.lng(), b.lat(), b.lng());
				a.linkTo(b.id(), distance);
				b.linkTo(a.id(), distance);
			}
		}
	}

	private Path resolveDataDir() {
		List<Path> candidates = new ArrayList<>();
		if (configuredDataDir != null && !configuredDataDir.isBlank()) {
			candidates.add(Paths.get(configuredDataDir));
		}
		candidates.add(Paths.get("raw_datasets/bus/lyon_tcl"));
		candidates.add(Paths.get("../raw_datasets/bus/lyon_tcl"));
		candidates.add(Paths.get("../../raw_datasets/bus/lyon_tcl"));
		candidates.add(Paths.get("..", "..", "raw_datasets", "bus", "lyon_tcl"));

		return candidates.stream()
				.map(Path::toAbsolutePath)
				.filter(Files::isDirectory)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Unable to find raw_datasets/bus/lyon_tcl"));
	}

	private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
		final double earthRadius = 6_371_000.0;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return earthRadius * c;
	}

	private static List<String> splitCsv(String line) {
		List<String> values = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean quoted = false;
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (ch == '"') {
				quoted = !quoted;
				continue;
			}
			if (ch == ',' && !quoted) {
				values.add(current.toString().trim());
				current.setLength(0);
				continue;
			}
			current.append(ch);
		}
		values.add(current.toString().trim());
		return values;
	}

	private record TripStop(String stopId, int sequence) {
	}

	private static final class CsvColumns {
		private final Map<String, Integer> indexByName;

		private CsvColumns(Map<String, Integer> indexByName) {
			this.indexByName = indexByName;
		}

		static CsvColumns fromHeader(String header) {
			if (header == null) {
				return new CsvColumns(Collections.emptyMap());
			}
			List<String> columns = splitCsv(header);
			Map<String, Integer> indexByName = new HashMap<>();
			for (int i = 0; i < columns.size(); i++) {
				indexByName.put(columns.get(i).replace("\"", "").trim().toLowerCase(Locale.ROOT), i);
			}
			return new CsvColumns(indexByName);
		}

		String value(List<String> cells, String columnName) {
			Integer index = indexByName.get(columnName.toLowerCase(Locale.ROOT));
			if (index == null || index < 0 || index >= cells.size()) {
				return "";
			}
			return cells.get(index).replace("\"", "").trim();
		}
	}
}
