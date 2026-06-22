package parsers;

import structure.MetroStation;
import structure.Coordinates;
import structure.Distance;
import structure.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static utilities.Tools.haversineMeters;
import static utilities.Tools.stripQuotes;

public class MetroParser {

    /**
     * Lit le fichier des stations de métro et crée les objets MetroStation.
     * Utilise le délimiteur ";" propre aux fichiers CSV tcl.
     */
    public static Map<String, MetroStation> parseStations(String path) throws IOException {
        Map<String, MetroStation> stations = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String header = br.readLine();
            if (header == null) return stations;
            
            String[] cols = header.split(";", -1);
            int idxId = -1, idxName = -1, idxLat = -1, idxLon = -1;
            
            for (int i = 0; i < cols.length; i++) {
                String h = cols[i].trim().replace("\"", "");
                if (h.equalsIgnoreCase("id") || h.contains("stop_id")) idxId = i;
                if (h.equalsIgnoreCase("nom") || h.contains("stop_name")) idxName = i;
                if (h.equalsIgnoreCase("lat") || h.contains("stop_lat")) idxLat = i;
                if (h.equalsIgnoreCase("lon") || h.contains("stop_lon")) idxLon = i;
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] r = line.split(";", -1);
                
                String id = idxId >= 0 && idxId < r.length ? stripQuotes(r[idxId]) : "";
                String name = idxName >= 0 && idxName < r.length ? stripQuotes(r[idxName]) : "";
                String sLat = idxLat >= 0 && idxLat < r.length ? stripQuotes(r[idxLat]) : "";
                String sLon = idxLon >= 0 && idxLon < r.length ? stripQuotes(r[idxLon]) : "";
                
                if (id.isEmpty() || sLat.isEmpty() || sLon.isEmpty()) continue;
                
                try {
                    double lat = Double.parseDouble(sLat);
                    double lon = Double.parseDouble(sLon);
                    MetroStation ms = new MetroStation(id, name, new Coordinates(lat, lon));
                    stations.put(id, ms); // si ca ne marche pas, faut remplacer (id, ms) par (name, ms)
                } catch (NumberFormatException e) {
                    // Ligne d'en-tête mal lue ou donnée corrompue ignorée
                }
            }
        }
        return stations;
    }

    /**
     *Lit le fichier horaires_tcl.csv modifié et relie les stations entre elles.
     */
    public static void parseAndLinkHoraires(String path, Map<String, MetroStation> stationsMap) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String header = br.readLine();
            if (header == null) return;
            
            String[] cols = header.split(";", -1);
            int idxStationCourante = -1;
            int idxStationSuivante = -1;
            
            // Détection automatique des colonnes adaptées à la modification
            for (int i = 0; i < cols.length; i++) {
                String h = cols[i].trim().replace("\"", "");
                if (h.equalsIgnoreCase("station_courante") || h.equalsIgnoreCase("station_id")) {
                    idxStationCourante = i;
                }
                if (h.equalsIgnoreCase("station_suivante") || h.equalsIgnoreCase("next_station")) {
                    idxStationSuivante = i;
                }
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] r = line.split(";", -1);
                
                String currentId = idxStationCourante >= 0 && idxStationCourante < r.length ? stripQuotes(r[idxStationCourante]) : "";
                String nextId = idxStationSuivante >= 0 && idxStationSuivante < r.length ? stripQuotes(r[idxStationSuivante]) : "";
                
                // Si la ligne n'a pas de station suivante, on passe à la suite
                if (currentId.isEmpty() || nextId.isEmpty() || nextId.equalsIgnoreCase("null") || nextId.equalsIgnoreCase("-")) {
                    continue;
                }
                
                MetroStation currentStation = stationsMap.get(currentId);
                MetroStation nextStation = stationsMap.get(nextId);
                
                // Si les deux stations existent bien dans notre carte, on crée le lien
                if (currentStation != null && nextStation != null) {
                    double d = haversineMeters(
                        currentStation.getCoordinates().latitude(), currentStation.getCoordinates().longitude(),
                        nextStation.getCoordinates().latitude(), nextStation.getCoordinates().longitude()
                    );
                    
                    // Ajout du lien bidirectionnel (aller-retour)
                    currentStation.addLink(nextStation, new Distance(d));
                    nextStation.addLink(currentStation, new Distance(d));
                }
            }
        }
    }
}