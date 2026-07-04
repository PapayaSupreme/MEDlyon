package com.medlyon.backend.service;

import com.medlyon.backend.core.algorithm.AStar;
import com.medlyon.backend.core.algorithm.CO2;
import com.medlyon.backend.core.algorithm.Dijkstra;
import com.medlyon.backend.core.structure.Bus;
import com.medlyon.backend.core.structure.BusStop;
import com.medlyon.backend.core.structure.Coordinates;
import com.medlyon.backend.core.structure.Distance;
import com.medlyon.backend.core.structure.Graph;
import com.medlyon.backend.core.structure.Node;
import com.medlyon.backend.core.utilities.Tools;
import com.medlyon.backend.model.PathResponse;
import com.medlyon.backend.model.PointResponse;
import com.medlyon.backend.model.RouteComparisonResponse;
import com.medlyon.backend.model.StopSummary;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TransitService {

	@Value("${medlyon.data-dir:}")
	private String configuredDataDir;

	private final Graph graph = new Graph();
	private final Map<String, BusStop> stopsById = new LinkedHashMap<>();
	private final Map<String, Bus> tripsById = new LinkedHashMap<>();
	private final Map<String, String> tripToRoute = new HashMap<>();
	private final Map<String, List<String>> stopToTrips = new HashMap<>();
	private final Map<String, List<String>> stopToRoutes = new HashMap<>();

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
		BusStop exact = stopsById.values().stream()
				.filter(stop -> stop.getId().equalsIgnoreCase(nodeName.trim()) || stop.getName().equalsIgnoreCase(nodeName.trim()))
				.findFirst()
				.orElse(null);
		if (exact != null) {
			return toPointResponse(exact);
		}

		BusStop partial = stopsById.values().stream()
				.filter(stop -> stop.getName().toLowerCase(Locale.ROOT).contains(needle)
						|| stop.getId().toLowerCase(Locale.ROOT).contains(needle))
				.min(Comparator.comparingInt(stop -> stop.getName().length()))
				.orElseThrow(() -> new IllegalArgumentException("Unknown node: " + nodeName));
		return toPointResponse(partial);
	}

	public PointResponse findClosestNode(double lat, double lng) {
		return toPointResponse(nearestNode(lat, lng));
	}

	public List<StopSummary> listStops() {
		return stopsById.values().stream()
				.sorted(Comparator.comparing(BusStop::getName).thenComparing(BusStop::getId))
				.map(stop -> new StopSummary(
						stop.getId(),
						stop.getCoordinates().latitude(),
						stop.getCoordinates().longitude(),
						stop.getName()
				))
				.toList();
	}

	public PathResponse computePath(double slat, double slng, double elat, double elng) {
		BusStop start = nearestNode(slat, slng);
		BusStop end = nearestNode(elat, elng);
		Dijkstra.ShortestPathsResult result = Dijkstra.computeShortestPaths(graph, start);
		return new PathResponse(toPointResponses(result.reconstructPath(end)));
	}

	public RouteComparisonResponse compareAlgorithms(double slat, double slng, double elat, double elng) {
		BusStop start = nearestNode(slat, slng);
		BusStop end = nearestNode(elat, elng);

		long dijkstraStart = System.nanoTime();
		List<Node> dijkstraNodes = Dijkstra.computeShortestPaths(graph, start).reconstructPath(end);
		List<PointResponse> dijkstraPath = toPointResponses(dijkstraNodes);
		long dijkstraTime = System.nanoTime() - dijkstraStart;
		double dijkstraCo2 = CO2.computeRouteCO2(dijkstraNodes);

		long aStarStart = System.nanoTime();
		List<Node> aStarNodes = AStar.computeShortestPath(graph, start, end).reconstructPath(end);
		List<PointResponse> aStarPath = toPointResponses(aStarNodes);
		long aStarTime = System.nanoTime() - aStarStart;
		double aStarCo2 = CO2.computeRouteCO2(aStarNodes);

		return new RouteComparisonResponse(
				new PathResponse(dijkstraPath),
				new PathResponse(aStarPath),
				dijkstraTime,
				aStarTime,
				dijkstraCo2,
				aStarCo2
		);
	}

	private BusStop nearestNode(double lat, double lng) {
		return stopsById.values().stream()
				.min(Comparator.comparingDouble(stop -> Tools.haversineMeters(lat, lng, stop.getCoordinates().latitude(), stop.getCoordinates().longitude())))
				.orElseThrow(() -> new IllegalStateException("No transit nodes loaded"));
	}

	private List<PointResponse> toPointResponses(List<Node> nodes) {
		if (nodes == null || nodes.isEmpty()) {
			return List.of();
		}
		List<PointResponse> responses = new ArrayList<>(nodes.size());
		for (Node node : nodes) {
			responses.add(toPointResponse(node));
		}
		return responses;
	}

	private PointResponse toPointResponse(Node node) {
		List<String> additionalInformation = new ArrayList<>();
		additionalInformation.add("stop_id=" + node.getId());

		long tripCount = stopToTrips.getOrDefault(node.getId(), List.of()).stream().distinct().count();
		long routeCount = stopToRoutes.getOrDefault(node.getId(), List.of()).stream().distinct().count();
		if (tripCount > 0) {
			additionalInformation.add("trip_count=" + tripCount);
		}
		if (routeCount > 0) {
			additionalInformation.add("route_count=" + routeCount);
		}

		return new PointResponse(
				node.getCoordinates().latitude(),
				node.getCoordinates().longitude(),
				node.getName(),
				additionalInformation
		);
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
					BusStop stop = new BusStop(id, name.isBlank() ? id : name, new Coordinates(lat, lng));
					stopsById.put(id, stop);
					graph.addNode(stop);
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
				String routeId = columns.value(cells, "route_id");
				if (tripId.isBlank()) {
					continue;
				}
				tripToRoute.put(tripId, routeId);
				tripsById.putIfAbsent(tripId, new Bus(tripId));
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
				if (tripId.isBlank() || stopId.isBlank() || sequenceValue.isBlank()) {
					continue;
				}

				BusStop stop = stopsById.get(stopId);
				if (stop == null) {
					continue;
				}

				try {
					int sequence = Integer.parseInt(sequenceValue);
					Bus bus = tripsById.computeIfAbsent(tripId, id -> new Bus(id));
					bus.addStop(stop, sequence);
					stopToTrips.computeIfAbsent(stopId, ignored -> new ArrayList<>()).add(tripId);
					String routeId = tripToRoute.get(tripId);
					if (routeId != null && !routeId.isBlank()) {
						stopToRoutes.computeIfAbsent(stopId, ignored -> new ArrayList<>()).add(routeId);
					}
				} catch (NumberFormatException ignored) {
				}
			}
		}
	}

	private void linkTrips() {
		for (Bus bus : tripsById.values()) {
			List<Node> ordered = bus.getStopsOrdered();
			for (int i = 0; i + 1 < ordered.size(); i++) {
				Node a = ordered.get(i);
				Node b = ordered.get(i + 1);
				double distance = Tools.haversineMeters(
						a.getCoordinates().latitude(),
						a.getCoordinates().longitude(),
						b.getCoordinates().latitude(),
						b.getCoordinates().longitude()
				);
				a.addLink(b, new Distance(distance));
				b.addLink(a, new Distance(distance));
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
