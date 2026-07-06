# Requirements: 
- Java SDK 26.0.1+
- Node.JS 16.13.1+
- Maven 3.8.4+
- Spring Boot 2.7.1+
- React 18.2.0+
Requirements: Java SDK 26+

Entry point: [./src/App.java](/src/App.java)

# Running the Application:

You can download the jar CLI application built through the GitHub releases.
To use it, use the command :
```bash
java -jar CLIApp.jar argument_1 argument_2 argument_3 argument_4 ...
```

- `Argument_1` : The path to the bus files (named: Stop_times.txt, stops.txt, trips.txt) 
- `Argument_2` : The path to the metro files (named: horaires_tcl.csv, stations-metro-reseau-transports-commun-lyonnais.csv)
- `Argument_3` : The ID of the stop from which dijkstra should start its search
- `Argument_4` : The ID of the stop where the dijkstra algorithm should search a path to.
- if `Argument 3 and 4` are not specified, There exists another third possible argument: "-noCLI", to directly exit upon finishing the tests.

# Open Source Data:

##### Disclaimer: You can find the different licenses for the content inside the folder in which they should be downloaded.
(`raw_datasets/(bus|metro|co2)`)

https://transport.data.gouv.fr/
------------
We Use the following data sets:
- [Bus stops (`bus`)](https://gtech-transit-prod.apigee.net/v1/google/gtfs/odbl/lyon_tcl.zip?apikey=BasyG6OFZXgXnzWdQLTwJFGcGmeOs204&secret=gNo6F5PhQpsGRBCK)
- [Environmental factors (`co2`)](https://data.grandlyon.com/portail/fr/jeux-de-donnees/elements-calcul-facteur-environnemental-itineraires-proposes-reseau-transports-commun-lyonnais/telechargements)
- [Metro Station to Roads (modified) (`metro`)](https://data.grandlyon.com/portail/fr/jeux-de-donnees/lignes-bus-reseau-transports-commun-lyonnais-v2/info)
------------

# How it works: architecture

- `/structure/`:
  - `Node` is an abstract stop, `BusStop` & `MetroStop` are its instanciations
  - `Transport` is the abstract father of `Bus` & `Metro`
  - `Distance` & `Coordinates` are location storages. They are record classes.
  - `Graph` is a gathering of every Node that exists.

- `/parsers/` :
  - `BusParser` parses every bus itinerary, their stops and locations. It runs ONE time at startup
  - `MetroParser` parses every metro itinerary, their stops and locations. It runs only once at startup.

- `/utilities/` :
  - `Tools` : contains distance calculation from coordinates and string dequoting utilities.
  - `Costs` : contains the cost function for dijkstra and A* algorithms

- `/legacy/` :
  This folder contains all the previously used scripts that have now been deprecated. 

- `/Autre/` : 
  - `AppAlt` : A previous entrypoint for testing the metro part of the application. It is now deprecated.
  - `horairetocsv` : Is a Python script that was used to modify the metro's `horaire` dataset.

`App` : Entrypoint

# Launch GUI :

1. Create `Server/roulyon/.env`. 

2. Start the Spring Boot backend from `Server/springboot` :
   mvn spring-boot:run

3. Start the React app from `Server/roulyon` :
   npm run dev

# Contributors :

- [<img src="https://avatars.githubusercontent.com/u/71288368" alt="Github Avatar" style="height: 16px; width:16px;border-radius:50%;clip-path:circle();"/> Pablo Ferreira](https://github.com/PapayaSupreme) 
- [<img src="https://avatars.githubusercontent.com/u/233638907?s=96&v=4" alt="Github Avatar" style="height: 16px; width:16px;border-radius:50%;clip-path:circle();"/> Nathanael Lucette]() 
- [<img src="https://avatars.githubusercontent.com/u/150516817" alt="Github Avatar" style="height: 16px; width:16px;border-radius:50%;clip-path:circle();"/> Alexis Lafargue](https://github.com/AlexisLaf) 
- [<img src="https://avatars.githubusercontent.com/u/151126331" alt="Github Avatar" style="height: 16px; width:16px;border-radius:50%;clip-path:circle();"/> Alexis Launay]() 
- [<img src="https://avatars.githubusercontent.com/u/293032157" alt="Github Avatar" style="height: 16px; width:16px;border-radius:50%;clip-path:circle();"/> Elias Hafsia](https://github.com/EHFS21) 

# Questions / inquiries :
[Email](mailto:pablo.ferreiraa10@gmail.com?subject=MEDLyon%20Project%20Questions&body=Body%20text)
