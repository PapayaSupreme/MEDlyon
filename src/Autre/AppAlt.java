package Autre;

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

/**
 * App principal complet et corrigé.
 * - Parse les stops via MetroParser
 * - Construit des index normalisés (id, name, alias)
 * - Lie les stops via parseAndLinkHoraires
 * - Construit l'ordre des arrêts par (ligne|sens) depuis horaires_tcl.csv
 * - Propose des suggestions fuzzy pour les noms introuvables et peut ajouter des alias à la volée
 */
public class AppAlt {

    public static void main(String[] args) throws IOException {
        String stationsFile = "raw_datasets/metro/stations-metro-reseau-transports-commun-lyonnais.csv";
        String lignesFile = "raw_datasets/metro/lignes-metro-funiculaire-reseau-transports-commun-lyonnais-v2 (1).csv";
        String horairesFile = "raw_datasets/metro/horaires_tcl.csv";

        // 1) Charger les MetroStop via MetroParser (index par id tel quel)
        Map<String, MetroStop> stopsByIdRaw = MetroParser.parseStops(stationsFile);

        // 2) Construire index normalisés : par id normalisé et par nom normalisé
        Map<String, MetroStop> stopsById = new HashMap<>();     // idNorm -> MetroStop
        Map<String, MetroStop> stopsByName = new HashMap<>();   // nameNorm -> MetroStop
        for (MetroStop ms : stopsByIdRaw.values()) {
            if (ms == null) continue;
            String idNorm = normalizeId(ms.getId());
            if (!idNorm.isEmpty()) stopsById.put(idNorm, ms);

            String name = ms.getName();
            if (name != null && !name.isBlank()) {
                String nameNorm = normalizeName(name);
                if (!nameNorm.isEmpty()) stopsByName.put(nameNorm, ms);
            }
        }

        // 2b) Construire alias map (plusieurs variantes par station) pour fuzzy matching
        Map<String, MetroStop> aliasMap = buildAliasMap(stopsByIdRaw.values());

        // 3) Lier les stops entre eux (ajoute les liens bidirectionnels avec Distance)
        MetroParser.parseAndLinkHoraires(horairesFile, stopsByIdRaw);

        // 4) Lire horaires pour construire l'ordre des arrêts par (ligne|sens)
        Map<String, List<MetroStop>> orderMap = new LinkedHashMap<>(); // clé = ligne|sens

        try (BufferedReader br = new BufferedReader(new FileReader(horairesFile))) {
            String header = br.readLine();
            if (header != null) {
                header = header.stripLeading();
                if (header.startsWith("#")) header = header.substring(1);
                if (header.startsWith("\uFEFF")) header = header.substring(1);
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(";", -1);

                String stationRaw = safeGet(cols, 0);
                String ligne = safeGet(cols, 1);
                String sens = safeGet(cols, 2);

                if (stationRaw.isBlank() || ligne.isBlank() || sens.isBlank()) continue;

                String key = ligne.trim() + "|" + sens.trim();

                // Normaliser la valeur lue pour la recherche
                String normalizedStation = normalizeName(stationRaw);

                // Chercher par nom normalisé
                MetroStop ms = stopsByName.get(normalizedStation);

                // Si non trouvé par nom, essayer par id (parfois le fichier horaires contient des ids)
                if (ms == null) {
                    String maybeId = normalizeId(stationRaw);
                    ms = stopsById.get(maybeId);
                }

                // Dernier essai : nettoyer guillemets simples/autres et retenter
                if (ms == null) {
                    String cleaned = stationRaw.replace("\"", "").replace("'", "").trim();
                    ms = stopsByName.get(normalizeName(cleaned));
                    if (ms == null) ms = stopsById.get(normalizeId(cleaned));
                }

                // Si toujours introuvable, tenter une suggestion fuzzy via aliasMap
                if (ms == null) {
                    MetroStop suggestion = findClosestStop(normalizedStation, aliasMap);
                    if (suggestion != null) {
                        System.err.println("Warning: station introuvable raw='" + stationRaw + "'; suggestion: '" + suggestion.getName() + "' id=" + suggestion.getId());
                        // Ajouter l'alias pour les prochaines lignes et utiliser la suggestion
                        String aliasKey = normalizeName(stationRaw);
                        aliasMap.put(aliasKey, suggestion);
                        stopsByName.putIfAbsent(aliasKey, suggestion);
                        ms = suggestion;
                    } else {
                        System.err.println("Warning: station introuvable dans stops pour ligne '" + ligne + "', sens '" + sens + "': raw='" + stationRaw + "'");
                        continue;
                    }
                }

                List<MetroStop> list = orderMap.computeIfAbsent(key, k -> new ArrayList<>());
                list.add(ms);
            }
        }

        // 5) Créer les objets Metro et ajouter les stops avec leur séquence
        Map<String, Metro> metros = new LinkedHashMap<>(); // clé = ligne|sens
        for (Map.Entry<String, List<MetroStop>> e : orderMap.entrySet()) {
            String key = e.getKey();
            String[] parts = key.split("\\|", 2);
            String ligne = parts[0];
            String sens = parts.length > 1 ? parts[1] : "unknown";

            // id du Metro : ligne + "_" + sens (sanitized)
            String metroId = sanitizeIdForObject(ligne + "_" + sens);
            Metro metro = new Metro(metroId, ligne);

            List<MetroStop> stopsList = e.getValue();
            int seq = 1;
            for (MetroStop ms : stopsList) {
                metro.addStop(ms, seq++);
            }
            metros.put(key, metro);
        }

        // 6) Affichage résumé
        System.out.println("=== Chargement terminé ===");
        System.out.println("Stations chargées (par id raw) : " + stopsByIdRaw.size());
        System.out.println("Stations indexées par id normalisé : " + stopsById.size());
        System.out.println("Stations indexées par nom normalisé : " + stopsByName.size());
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

        // 7) Exemple d'utilisation : trouver le prochain arrêt sur une ligne donnée
        // Exemple : pour la ligne A sens 'a', trouver le prochain arrêt après "Bellecour"
        String exampleKey = "A|a";
        Metro exampleMetro = metros.get(exampleKey);
        MetroStop bellecour = stopsByName.get(normalizeName("Bellecour"));
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
            System.out.println("Exemple non disponible (clé " + exampleKey + " ou Bellecour introuvable).");
        }
    }

    // ---------- Helpers ----------

    private static String safeGet(String[] cols, int idx) {
        if (cols == null || idx < 0 || idx >= cols.length) return "";
        return cols[idx];
    }

    // Normalise un nom de station pour les recherches (trim, enlever guillemets, réduire espaces, toLowerCase, enlever accents, remplacer ponctuation)
    private static String normalizeName(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "";
        // remove BOM
        if (s.charAt(0) == '\uFEFF') s = s.substring(1);
        // remove surrounding quotes
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        // replace hyphens/slashes by spaces, remove common punctuation
        s = s.replaceAll("[-/]", " ");
        s = s.replaceAll("[.,()]", " ");
        // collapse whitespace
        s = s.replaceAll("\\s+", " ").trim();
        // remove accents
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s.toLowerCase(Locale.ROOT);
    }

    // Normalise un id (trim + toLowerCase)
    private static String normalizeId(String id) {
        if (id == null) return "";
        String s = id.trim();
        if (s.isEmpty()) return "";
        if (s.charAt(0) == '\uFEFF') s = s.substring(1);
        return s.toLowerCase(Locale.ROOT);
    }

    // Sanitize simple id for object naming (remplace espaces et caractères problématiques)
    private static String sanitizeIdForObject(String raw) {
        if (raw == null) return "unknown";
        String s = raw.trim().replaceAll("\\s+", "_");
        s = s.replaceAll("[^A-Za-z0-9_\\-]", "_");
        return s;
    }

    // Build alias map from stops: for each MetroStop add several normalized keys
    private static Map<String, MetroStop> buildAliasMap(Collection<MetroStop> stops) {
        Map<String, MetroStop> alias = new HashMap<>();
        for (MetroStop ms : stops) {
            if (ms == null) continue;
            String idNorm = normalizeId(ms.getId());
            if (!idNorm.isEmpty()) alias.put(idNorm, ms);

            String nameNorm = normalizeName(ms.getName());
            if (!nameNorm.isEmpty()) alias.put(nameNorm, ms);

            // additional variants: remove spaces, remove dots
            alias.putIfAbsent(nameNorm.replaceAll("\\s+", ""), ms);
            alias.putIfAbsent(nameNorm.replace(".", ""), ms);
        }
        return alias;
    }

    // Simple Levenshtein distance (iterative DP)
    private static int levenshtein(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        int n = a.length(), m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = cur; cur = tmp;
        }
        return prev[m];
    }

    // Find closest stop by normalized name; returns null if no candidate under threshold
    private static MetroStop findClosestStop(String normalized, Map<String, MetroStop> aliasMap) {
        if (normalized == null || normalized.isEmpty()) return null;
        Map<MetroStop, Integer> best = new HashMap<>();
        for (Map.Entry<String, MetroStop> e : aliasMap.entrySet()) {
            String key = e.getKey();
            MetroStop ms = e.getValue();
            int dist = levenshtein(normalized, key);
            best.merge(ms, dist, Math::min);
        }
        return best.entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .filter(en -> en.getValue() <= Math.max(2, normalized.length() / 6)) // seuil adaptatif
                .map(Map.Entry::getKey)
                .orElse(null);
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
