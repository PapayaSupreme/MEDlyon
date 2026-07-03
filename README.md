Requirements: Java SDK 21+

Entry point : [./src/App.java](/src/App.java)

# Open Source Data:

https://transport.data.gouv.fr/
------------
We Use the following data sets:
- [Bus stops](https://gtech-transit-prod.apigee.net/v1/google/gtfs/odbl/lyon_tcl.zip?apikey=BasyG6OFZXgXnzWdQLTwJFGcGmeOs204&secret=gNo6F5PhQpsGRBCK)
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

# Launch GUI :

1. Create `Server/roulyon/.env`. 

2. Start the Spring Boot backend from `Server/springboot` :
   mvn spring-boot:run

3. Start the React app from `Server/roulyon` :
   npm run dev

# Contributors :

- Pablo Ferreira
- Nathanael Lucette
- Alexis Lafargue
- Alexis Launay
- Elias Hafsia

# Questions / inquiries :
[Email](mailto:pablo.ferreiraa10@gmail.com?subject=MEDLyon%20Project%20Questions&body=Body%20text)
