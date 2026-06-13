import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse un fichier GTFS stops.txt en une liste d'objets Stop.
 *
 * Format du fichier :
 *   - CSV à 15 colonnes, séparateur virgule
 *   - 1re ligne = en-têtes (à ignorer)
 *   - chaque champ est entouré de guillemets : "1","24 Août",...
 *   - de nombreux champs sont vides, y compris en fin de ligne (,,,,,)
 *
 * Colonnes utiles ici (index 0-based) :
 *   0 = stop_id, 1 = stop_name, 3 = stop_lat, 4 = stop_lon
 */
public class StopParser {

    private static final int COL_COUNT = 15; // nombre de colonnes attendu
    private static final int IDX_ID   = 0;
    private static final int IDX_NAME = 1;
    private static final int IDX_LAT  = 3;
    private static final int IDX_LON  = 4;

    public static List<Stop> parse(String cheminFichier) throws IOException {
        List<Stop> stops = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            boolean premiereLigne = true;   // sert à sauter l'en-tête
            int numLigne = 0;

            while ((ligne = br.readLine()) != null) {
                numLigne++;

                // 1) Sauter la ligne d'en-tête
                if (premiereLigne) {
                    premiereLigne = false;
                    continue;
                }

                // 2) Ignorer les lignes vides éventuelles
                if (ligne.trim().isEmpty()) continue;

                // 3) Découper en gardant les champs vides terminaux (le -1 !)
                String[] champs = ligne.split(",", -1);

                // 4) Vérifier la structure
                if (champs.length != COL_COUNT) {
                    System.err.println("Ligne " + numLigne + " ignorée (" + champs.length
                            + " colonnes au lieu de " + COL_COUNT + ") : " + ligne);
                    continue;
                }

                // 5) Nettoyer les guillemets de chaque champ utile
                String id   = sansGuillemets(champs[IDX_ID]);
                String nom  = sansGuillemets(champs[IDX_NAME]);
                String sLat = sansGuillemets(champs[IDX_LAT]);
                String sLon = sansGuillemets(champs[IDX_LON]);

                // 6) Un arrêt sans id ou sans coordonnées n'est pas exploitable comme nœud
                if (id.isEmpty() || sLat.isEmpty() || sLon.isEmpty()) {
                    System.err.println("Ligne " + numLigne + " ignorée (id ou coordonnées manquants)");
                    continue;
                }

                // 7) Convertir les coordonnées en double
                try {
                    double lat = Double.parseDouble(sLat);
                    double lon = Double.parseDouble(sLon);
                    stops.add(new Stop(id, nom, lat, lon));
                } catch (NumberFormatException e) {
                    System.err.println("Ligne " + numLigne + " ignorée (coordonnée non numérique) : "
                            + sLat + " / " + sLon);
                }
            }
        }

        return stops; // APRÈS la boucle : on renvoie tous les arrêts, pas seulement le premier
    }

    /**
     * Retire les guillemets entourant un champ : "24 Août" -> 24 Août
     * Gère aussi le cas d'un champ vide ou sans guillemets.
     */
    private static String sansGuillemets(String champ) {
        champ = champ.trim();
        if (champ.length() >= 2 && champ.startsWith("\"") && champ.endsWith("\"")) {
            return champ.substring(1, champ.length() - 1);
        }
        return champ;
    }

    // Petit main de démonstration / test manuel
    public static void main(String[] args) throws IOException {
        List<Stop> stops = parse("stops.txt");

        System.out.println("Nombre d'arrêts chargés : " + stops.size());
        System.out.println("--- 5 premiers ---");
        for (int i = 0; i < Math.min(5, stops.size()); i++) {
            System.out.println(stops.get(i));
        }
    }
}
