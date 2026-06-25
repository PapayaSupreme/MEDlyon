import parsers.MetroParser;
import structure.Metro;
import structure.MetroStop;
import structure.Node;
import structure.Distance;
import utilities.Tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class App {

    public static void main(String[] args) throws IOException {
        String stationsFile = "stations-metro-reseau-transports-commun-lyonnais.csv";
        String lignesFile = "lignes-metro-funiculaire-reseau-transports-commun-lyonnais-v2 (1).csv";
        String horairesFile = "horaires_tcl.csv";

        // 1) Charger les MetroStop via MetroParser
        Map<String, MetroStop> stopsByIdOrName = MetroParser.parseStops(stationsFile);
        // MetroParser.parseStops indexe par id (String id). On veut aussi un index par nom pour le fichier horaires.
        Map<String, MetroStop> stopsByName = new HashMap<>();
        for (MetroStop ms : stopsByIdOrName.values()) {
            if (ms.getName() != null && !ms.getName().isEmpty()) {
                stopsByName.put(ms.getName().trim(), ms);
            }
            // aussi indexer par id (déjà présent dans stopsByIdOrName)
        }

        // 2) Lier les stops entre eux (ajoute les liens bidirectionnels avec Distance)
        MetroParser.parseAndLinkHoraires(horairesFile, stopsByIdOrName);

        // 3) Construire les objets Metro (une instance par couple ligne+sens)
        // On va lire le fichier horaires et, pour chaque (ligne,sens), collecter la liste d'arrêts
        Map<String, List<MetroStop>> orderMap = new LinkedHashMap<>(); // clé = ligne + "|" + sens

        try (BufferedReader br = new BufferedReader(new FileReader(horairesFile))) {
            String header = br.readLine(); // ignorer l'entête
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(";", -1);
                if (cols.length < 2) continue;
                String stationName = cols[0].trim();
                String ligne = cols.length > 1 ? cols[1].trim() : "";
                String sens = cols.length > 2 ? cols[2].trim() : "";

                if (ligne.isEmpty() || sens.isEmpty() || stationName.isEmpty()) continue;

                String key = ligne + "|" + sens;
                MetroStop ms = stopsByName.get(stationName);
                if (ms == null) {
                    // parfois le nom dans horaires peut contenir des guillemets ou espaces ; essayer un nettoyage simple
                    String cleaned = stationName.replace("\"", "").trim();
                    ms = stopsByName.get(cleaned);
                }

                orderMap.computeIfAbsent(key, k -> new ArrayList<>());
                List<MetroStop> list = orderMap.get(key);
                // éviter doublons si le fichier contient plusieurs lignes pour la même station
                if (list.isEmpty() || !list.get(list.size() - 1).equals(ms)) {
                    if (!list.contains(ms)) list.add(ms);
                }
            }
        }

        // 4) Créer les objets Metro et ajouter les stops avec leur séquence
        Map<String, Metro> metros = new HashMap<>(); // clé = ligne|sens
        for (Map.Entry<String, List<MetroStop>> e : orderMap.entrySet()) {
            String key = e.getKey();
            String[] parts = key.split("\\|", 2);
            String ligne = parts[0];
            String sens = parts.length > 1 ? parts[1] : "unknown";
            // id du Metro : ligne + "_" + sens
            String metroId = ligne + "_" + sens;
            Metro metro = new Metro(metroId, ligne);
            List<MetroStop> stopsList = e.getValue();
            int seq = 1;
            for (MetroStop ms : stopsList) {
                metro.addStop(ms, seq++);
            }
            metros.put(key, metro);
        }

        // 5) Affichage résumé
        System.out.println("=== Chargement terminé ===");
        System.out.println("Stations chargées (par id) : " + stopsByIdOrName.size());
        System.out.println("Stations indexées par nom : " + stopsByName.size());
        System.out.println("Nombre de services Metro construits (ligne|sens) : " + metros.size());
        System.out.println();

        // Afficher quelques lignes et leurs premiers arrêts
        for (Map.Entry<String, Metro> me : metros.entrySet()) {
            Metro m = me.getValue();
            List<Node> ordered = m.getStopsOrdered();
            System.out.printf("Metro %s (routeId=%s) : %d arrêts. Extrait : ",
                    m.getId(), m.getRouteId(), ordered.size());
            for (int i = 0; i < Math.min(5, ordered.size()); i++) {
                System.out.print(ordered.get(i).getName());
                if (i < Math.min(5, ordered.size()) - 1) System.out.print(" -> ");
            }
            System.out.println();
        }

        // 6) Exemple d'utilisation : trouver le prochain arrêt sur une ligne donnée
        // Exemple : pour la ligne A sens 'a', trouver le prochain arrêt après "Bellecour"
        String exampleKey = "A|a";
        Metro exampleMetro = metros.get(exampleKey);
        MetroStop bellecour = stopsByName.get("Bellecour");
        if (exampleMetro != null && bellecour != null) {
            Node next = exampleMetro.nextStop(bellecour);
            System.out.println();
            System.out.println("Exemple : sur " + exampleMetro.getId() + ", arrêt courant = " + bellecour);
            if (next != null) {
                System.out.println("Prochain arrêt : " + next + " (distance réelle si lien présent : "
                        + distanceBetween(bellecour, (MetroStop) next) + ")");
            } else {
                System.out.println("Aucun prochain arrêt trouvé (terminus ou séquence manquante).");
            }
        } else {
            System.out.println();
            System.out.println("Exemple non disponible (ligne A|a ou Bellecour introuvable).");
        }
    }

    // utilitaire pour afficher la distance entre deux MetroStop si un lien existe
    private static String distanceBetween(MetroStop a, MetroStop b) {
        if (a == null || b == null) return "n/a";
        Map<Node, Distance> links = a.getLinks();
        Distance d = links.get(b);
        if (d != null) return d.toString();
        // sinon calculer via Haversine (approx)
        double meters = Tools.haversineMeters(
                a.getCoordinates().latitude(), a.getCoordinates().longitude(),
                b.getCoordinates().latitude(), b.getCoordinates().longitude()
        );
        return String.format("%.1fm (haversine)", meters);
    }
}
