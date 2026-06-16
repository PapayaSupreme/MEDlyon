package parsers;

import structure.Bus;
import structure.BusStop;
import structure.Coordinates;
import structure.Distance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utilities.Tools.haversineMeters;
import static utilities.Tools.stripQuotes;

/**
 * Simple GTFS-based bus parser that builds Bus (trip) objects and links
 * consecutive stops based on stop_times stop_sequence.
 */
public class BusParser {

	/**
	 * Parse stops.txt and return a map stop_id -> BusStop.
	 * stop_id is kept as string keys because GTFS allows non-numeric ids.
	 * BusStop instances use integer ids; non-numeric stop_ids receive generated ids.
	 */
	public static Map<String, BusStop> parseStops(String path) throws IOException {
		Map<String, BusStop> stops = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String header = br.readLine();
			if (header == null) return stops;
			String[] cols = header.split(",", -1);
			// locate required columns
			int idxStopId = -1, idxName = -1, idxLat = -1, idxLon = -1;
			for (int i = 0; i < cols.length; i++) {
				String h = cols[i].trim().replace("\"", "");
				if (h.equalsIgnoreCase("stop_id")) idxStopId = i;
				if (h.equalsIgnoreCase("stop_name")) idxName = i;
				if (h.equalsIgnoreCase("stop_lat")) idxLat = i;
				if (h.equalsIgnoreCase("stop_lon")) idxLon = i;
			}
			String line;

			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) continue;
				String[] r = line.split(",", -1);
				String sid = idxStopId >= 0 && idxStopId < r.length ? stripQuotes(r[idxStopId]) : "";
				String name = idxName >= 0 && idxName < r.length ? stripQuotes(r[idxName]) : "";
				String sLat = idxLat >= 0 && idxLat < r.length ? stripQuotes(r[idxLat]) : "";
				String sLon = idxLon >= 0 && idxLon < r.length ? stripQuotes(r[idxLon]) : "";
				if (sid.isEmpty() || sLat.isEmpty() || sLon.isEmpty()) continue;
				try {
					double lat = Double.parseDouble(sLat);
					double lon = Double.parseDouble(sLon);
					BusStop bs = new BusStop(sid, name, new Coordinates(lat, lon));
					stops.put(sid, bs);
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
		return stops;
	}

	private static Map<String, String> parseTrips(String path) throws IOException {
		Map<String, String> tripToRoute = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String header = br.readLine();
			if (header == null) return tripToRoute;
			String[] cols = header.split(",", -1);
			int idxTrip = -1, idxRoute = -1;
			for (int i = 0; i < cols.length; i++) {
				String h = cols[i].trim().replace("\"", "");
				if (h.equalsIgnoreCase("trip_id")) idxTrip = i;
				if (h.equalsIgnoreCase("route_id")) idxRoute = i;
			}
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) continue;
				String[] r = line.split(",", -1);
				String tripId = idxTrip >= 0 && idxTrip < r.length ? stripQuotes(r[idxTrip]) : null;
				String routeId = idxRoute >= 0 && idxRoute < r.length ? stripQuotes(r[idxRoute]) : null;
				if (tripId != null && routeId != null) tripToRoute.put(tripId, routeId);
			}
		}
		return tripToRoute;
	}

	public record Result(
					Map<String,
					Bus> transports,
					Map<String,
					List<String>> stopToTrips,
					Map<String,
					List<String>> stopToRoutes
	) {}

	public static Result parseStopTimes(String stopTimesPath, String tripsPath, Map<String, BusStop> stopsById) throws IOException {
		// map trip_id -> Bus
		Map<String, Bus> transports = new HashMap<>();

		// map stop_id -> list of trip_ids
		Map<String, List<String>> stopToTrips = new HashMap<>();
		Map<String, List<String>> stopToRoutes = new HashMap<>();

		// load trip -> route mapping
		Map<String, String> tripToRoute = parseTrips(tripsPath);

		try (BufferedReader br = new BufferedReader(new FileReader(stopTimesPath))) {
			String header = br.readLine();
			if (header == null) return new Result(transports, stopToTrips, stopToRoutes);
			String[] cols = header.split(",", -1);
			int idxTrip = -1, idxStop = -1, idxSeq = -1;
			for (int i = 0; i < cols.length; i++) {
				String h = cols[i].trim().replace("\"", "");
				if (h.equalsIgnoreCase("trip_id")) idxTrip = i;
				if (h.equalsIgnoreCase("stop_id")) idxStop = i;
				if (h.equalsIgnoreCase("stop_sequence")) idxSeq = i;
			}

			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) continue;
				String[] r = line.split(",", -1);
				String tripId = idxTrip >= 0 && idxTrip < r.length ? stripQuotes(r[idxTrip]) : null;
				String stopId = idxStop >= 0 && idxStop < r.length ? stripQuotes(r[idxStop]) : null;
				String seqS = idxSeq >= 0 && idxSeq < r.length ? stripQuotes(r[idxSeq]) : null;
				if (tripId == null || stopId == null || seqS == null) continue;
				int seq;
				try { seq = Integer.parseInt(seqS); } catch (NumberFormatException e) { continue; }

				BusStop stop = stopsById.get(stopId);
				if (stop == null) continue;

				String routeId = tripToRoute.get(tripId);

				Bus bus = transports.get(tripId);
				if (bus == null) {
					bus = new Bus(tripId, routeId);
					transports.put(tripId, bus);
				}
				bus.addStop(stop, seq);

				// reverse mappings
				stopToTrips.computeIfAbsent(stopId, _ -> new java.util.ArrayList<>()).add(tripId);
				if (routeId != null) stopToRoutes.computeIfAbsent(stopId, _ -> new java.util.ArrayList<>()).add(routeId);
			}
		}

		// After building transports, link consecutive stops with Distance edges
		for (Bus bus : transports.values()) {
			List<structure.Node> ordered = bus.getStopsOrdered();
			for (int i = 0; i + 1 < ordered.size(); i++) {
				structure.Node a = ordered.get(i);
				structure.Node b = ordered.get(i + 1);
				double d = haversineMeters(a.getCoordinates().latitude(), a.getCoordinates().longitude(),
						b.getCoordinates().latitude(), b.getCoordinates().longitude());
				// add bidirectional link between stops representing service edge
				a.addLink(b, new Distance(d));
				b.addLink(a, new Distance(d));
			}
		}

		return new Result(transports, stopToTrips, stopToRoutes);
	}
}
