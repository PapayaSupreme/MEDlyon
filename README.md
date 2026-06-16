Requirements: Java SDK 26.0.1+

Entry point : [./src/App.java](/src/App.java)

# Open Source Data:

https://transport.data.gouv.fr/
------------
We Use the following data sets:
 - [Bus stops](https://data.grandlyon.com/portail/fr/jeux-de-donnees/entrees-sorties-stations-metro-reseau-transports-commun-lyonnais/info)
 - [Environmental factors](https://data.grandlyon.com/portail/fr/jeux-de-donnees/elements-calcul-facteur-environnemental-itineraires-proposes-reseau-transports-commun-lyonnais/telechargements)
 - [Metro Station to Roads](https://data.grandlyon.com/portail/fr/jeux-de-donnees/lignes-bus-reseau-transports-commun-lyonnais-v2/info)
------------

# How it works : architecture

- `/structure/`: 
  - `Node` is an abstract stop, `BusStop` & `MetroStop` are its instanciations
  - `Transport` is the abstract father of `Bus` & `Metro`
  - `Distance` & `Coordinates` are location storages. They are record classes.

- `/parsers` :
  - `BusParser` parses every bus itinerary, their stops and locations. It runs ONE time at startup

- `/utilities/` :
  - `Tools` : contains distance calculation from coordinates and string dequoting utilities.

`App` : Entrypoint


# Contributors :
