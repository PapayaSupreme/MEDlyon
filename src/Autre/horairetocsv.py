import csv
import os
import re
import pdfplumber

def extraire_horaires_tcl(chemin_pdf):
    liste_horaires = []
    
    with pdfplumber.open(chemin_pdf) as pdf:
        page = pdf.pages[0]
        
        # On force la détection des colonnes grâce à l'espacement visuel du texte
        # (indispensable quand le tableau n'a pas de bordures visibles)
        reglages_tableau = {
            "vertical_strategy": "text", 
            "horizontal_strategy": "text"
        }
        
        # On applique les réglages lors de l'extraction
        tableau = page.extract_table(table_settings=reglages_tableau)
        
        if not tableau:
            print("Aucun tableau détecté. Le PDF est peut-être une image scannée.")
            return []

        index_ligne_heures = -1
        heures_par_colonne = {}
        
        # 1. Recherche de la ligne contenant les heures (ex: "5h", "6h")
        for index_ligne, ligne in enumerate(tableau):
            # On vérifie si au moins une cellule de la ligne contient un format d'heure
            if any(cellule and re.search(r'\d{1,2}h', str(cellule), re.IGNORECASE) for cellule in ligne):
                index_ligne_heures = index_ligne
                
                # On mémorise quelle colonne correspond à quelle heure
                for index_colonne, valeur_cellule in enumerate(ligne):
                    if valeur_cellule and re.search(r'\d+', str(valeur_cellule)):
                        heure = int(re.search(r'\d+', str(valeur_cellule)).group())
                        heures_par_colonne[index_colonne] = heure
                break 
                
        if index_ligne_heures == -1:
            print("Ligne des heures introuvable. Le PDF est formaté différemment.")
            return []

        # 2. Récupération des minutes situées sous la ligne des heures
        for ligne in tableau[index_ligne_heures + 1:]:
            for index_colonne, heure in heures_par_colonne.items():
                if index_colonne < len(ligne) and ligne[index_colonne]:
                    # On nettoie les espaces ou sauts de ligne accidentels
                    minute_str = str(ligne[index_colonne]).strip().replace(" ", "")
                    
                    # CORRECTION ICI : On vérifie que la chaîne contient STRICTEMENT 1 ou 2 chiffres
                    if re.fullmatch(r'\d{1,2}', minute_str):
                        minute = int(minute_str)
                        
                        # Sécurité supplémentaire : une minute ne peut pas dépasser 59
                        if minute < 60:
                            horaire_formate = f"{heure:02d}h{minute:02d}"
                            liste_horaires.append((heure, minute, horaire_formate))

    # 3. Tri chronologique (par heure, puis par minute)
    liste_horaires.sort(key=lambda x: (x[0], x[1]))
    
    # On retourne uniquement la liste des chaînes formatées (ex: "05h01")
    return [item[2] for item in liste_horaires]


def traiter_dossier(pdf_folder, csv_output):
    lignes_valides = {"A", "B", "C", "D"}
    sens_valides = {"a", "r"}

    # Regex pour extraire station, ligne et sens
    # Exemple : Bellecour_A(a).pdf
    pattern = re.compile(r"^(?P<station>.+)_(?P<ligne>[ABCD])\((?P<sens>[ar])\)$")

    with open(csv_output, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow(["station", "ligne", "sens", "horaires"])

        for filename in os.listdir(pdf_folder):
            if not filename.lower().endswith(".pdf"):
                continue

            name = filename[:-4]  # retire .pdf

            match = pattern.match(name)
            if not match:
                print(f"Nom de fichier ignoré (format incorrect) : {filename}")
                continue

            station = match.group("station")
            ligne = match.group("ligne")
            sens = match.group("sens")

            if ligne not in lignes_valides or sens not in sens_valides:
                print(f"Nom de fichier ignoré (ligne/sens invalide) : {filename}")
                continue

            pdf_path = os.path.join(pdf_folder, filename)

            # Appel de TA fonction
            horaires = extraire_horaires_tcl(pdf_path)

            # Convertit la liste en chaîne "05:12,05:34,06:02"
            horaires_str = ",".join(horaires)

            writer.writerow([station, ligne, sens, horaires_str])

    print("CSV généré :", csv_output)
traiter_dossier("..\\raw_dataset\\horaire theorique", "horaires_tcl.csv")
    
