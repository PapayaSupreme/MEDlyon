package parsers;

import structure.MetroStop;
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
     * Lit le fichier des Stops de métro et crée les objets MetroStop.
     * Utilise le délimiteur ";" propre aux fichiers CSV tcl.
     */
    public static Map<String, MetroStop> parseStops(String path) throws IOException {
        Map<String, MetroStop> Stops = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String header = br.readLine();
            if (header == null) return Stops;
            
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
                    MetroStop ms = new MetroStop(id, name, new Coordinates(lat, lon));
                    Stops.put(id, ms); // On peut aussi indexer par "name" si besoin
                } catch (NumberFormatException e) {
                    // Ligne d'en-tête mal lue ou donnée corrompue ignorée
                }
            }
        }
        return Stops;
    }

    /**
     *Lit le fichier horaires_tcl.csv modifié et relie les Stops entre elles.
     */
    public static void parseAndLinkHoraires(String path, Map<String, MetroStop> StopsMap) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String header = br.readLine();
            if (header == null) return;
            
            String[] cols = header.split(";", -1);
            int idxStopCourante = -1;
            int idxStopSuivante = -1;
            
            // Détection automatique des colonnes adaptées à ta modification
            for (int i = 0; i < cols.length; i++) {
                String h = cols[i].trim().replace("\"", "");
                if (h.equalsIgnoreCase("Stop_courante") || h.equalsIgnoreCase("Stop_id")) {
                    idxStopCourante = i;
                }
                if (h.equalsIgnoreCase("Stop_suivante") || h.equalsIgnoreCase("next_Stop")) {
                    idxStopSuivante = i;
                }
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] r = line.split(";", -1);
                
                String currentId = idxStopCourante >= 0 && idxStopCourante < r.length ? stripQuotes(r[idxStopCourante]) : "";
                String nextId = idxStopSuivante >= 0 && idxStopSuivante < r.length ? stripQuotes(r[idxStopSuivante]) : "";
                
                // Si la ligne n'a pas de Stop suivante (ex: terminus), on passe à la suite
                if (currentId.isEmpty() || nextId.isEmpty() || nextId.equalsIgnoreCase("null") || nextId.equalsIgnoreCase("-")) {
                    continue;
                }
                
                MetroStop currentStop = StopsMap.get(currentId);
                MetroStop nextStop = StopsMap.get(nextId);
                
                // Si les deux Stops existent bien dans notre carte, on crée le lien
                if (currentStop != null && nextStop != null) {
                    double d = haversineMeters(
                        currentStop.getCoordinates().latitude(), currentStop.getCoordinates().longitude(),
                        nextStop.getCoordinates().latitude(), nextStop.getCoordinates().longitude()
                    );
                    
                    // Ajout du lien bidirectionnel (aller-retour)
                    currentStop.addLink(nextStop, new Distance(d));
                    nextStop.addLink(currentStop, new Distance(d));
                }
            }
        }
    }
}
