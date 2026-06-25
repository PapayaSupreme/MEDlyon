package legacy;

import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import structure.Node;
import structure.Distance;
import structure.Route;
import structure.Node;
import structure.Aline;
import utilities.Costs;

//class Disjkstra {
    //static int idcount=0;

    /**
     * This function, will calculate the path between two or more given nodes using a provided preferred travel method (Default=None)
     * It can also receive multiple different parameters to change it's behavior (Use -h/-help to find all the usable parameters.)
     *
     * @param Typetransfermap The map contains all the connections from we we can go from one Transport type to another.
     * @param originNode The starting node from which the path should begin.
     * @param destinationNode The Node to which we need to go to from originNode 
     * (aka : The end node if there is only two points on the path and the second point on a path which uses multiple points.)
     * @param args This paramethers accepts any Node or String, any other type will throw an error. 
     * @return A route between the two or more given paths.
     * @version 0.0.1
     * @throws InvalidAlgorithmParameterException if the arguments given contain another type than a String or a Node.
     */
    /*public static <NorS extends Object> Route Astar(Map<Node, Node> Typetransfermap, Node originNode,Node destinationNode,NorS... args) throws InvalidAlgorithmParameterException{

        //Variables to parse the arguments
        String parameter = "";
        ArrayList<Node> stops = new ArrayList<>();
        stops.add(0,originNode);
        stops.add(1,destinationNode);
        Map<String, Boolean> options = new HashMap<>();
        Map<String, String> valueArgs = new HashMap<>();
        boolean Multiple_Stops = false;
        boolean NodeSearch=true; //First we parse for nodes than for Strings. They cannot be unordered.

        //Disjktra's algorithm variables
        Route path = new Route();
        Set<String> preferedTtype;
        ArrayList<Aline> explored;
        Map<Node,Distance> sucessors;
        Map<Integer,Aline> way;
        Double cost;
        Node snode,dnode,nextNode,prevNode;

        //Now we parse the arguments given to the function
        for (NorS arg : args) {
            if (arg instanceof Node){
                if (!NodeSearch){
                    throw new InvalidAlgorithmParameterException("A node was found in the arguments after the first String.");
                }
                Node nnode = (Node) arg;
                //In this case this means that we will be doing dijkstra for multiple paths :

                 // We have originNode that is the first, destinationNode which is the second.
                 //And all the other that we find in order will be

                //if (!Multiple_Stops){// This is the first Node that we find. Not needed.
                Multiple_Stops = true;
                stops.add(nnode);
            } else if (arg instanceof String){
                if (NodeSearch){
                    NodeSearch=false;
                }
                String param =(String) arg;
                if (param.charAt(0)=='-'){//This is a parameter
                    switch (param) {
                        case "-h","-he","-hel","-help":
                            System.out.println("Not Yet Implemented ...");
                            return path;
                        case "-v","-version":
                            System.out.println("A* MEDLyon Algorithm, v-0.0.1");
                            return path;
                        case "-":
                            //Possible throw of an error if we only use paired values
                            //if (!param.isEmpty()){
                            //    throw new InvalidAlgorithmParameterException("The parameter "+param+" needs a value, but "+arg+" was found.");
                            //}
                            //Else we accept multiple values for a single parameter.

                            //We need to get the next value to continue (the values are in a pair)
                            parameter=param;
                        default:
                            System.out.println("Unknown parameter : "+param);
                            System.out.println("Use -h to find all the possible String arguments.");
                            break;
                    }
                } else if (!parameter.isEmpty()){//Sends the String that goes with the previous parameter. (Ex : gcc -o a.exe, here we would send a.exe the name to which we want to name the file to be with the parameter -o)
                    //Deal with the parameter.
                    parameter="";
                } else {
                    //Default use of the parameters.
                    //Here we try to put it as a prefered travel_type
                    //Or just the trip name?
                }
            } else {
                throw new InvalidAlgorithmParameterException("The given parameter "+arg.toString()+" is neither a Node nor a String.");
            }
        }

        Iterator<Node> nodes = stops.iterator();
        //The stops variable represents all the points at which the user wishes to go by. So we search for the path from one start to the stop until there is no more.
        snode = nodes.next();
        while (nodes.hasNext()){
            dnode = nodes.next();

            //Start the disjkstra algorithm
            explored = new ArrayList<>();
            //Go one first time with a fixed number of steps to populate explored variable.
            sucessors = snode.getLinks();
            for (var sucessor : sucessors.entrySet()){
                cost = Costs.computeCost(snode, sucessor.getKey(),sucessor.getValue());//Need to add the arguments while constructing the function in utilities.cost
                explored.add(new Aline(snode,sucessor.getKey(),cost));
            }
            nextNode = explored.stream().filter((n)-> n.getresearch()).min(Comparator.comparing(Aline::getCost)).get().getNode(snode);
            //Now the main loop.
            while (nextNode != dnode) {
                sucessors = nextNode.getLinks();
                for (var sucessor : sucessors.entrySet()){
                    cost = Costs.computeCost(nextNode, sucessor.getKey(),sucessor.getValue());//Need to add the arguments while constructing the function in utilities.cost
                    explored.add(new Aline(nextNode,sucessor.getKey(),cost));
                }
                //Also add the links between different transport types. to search through
                //WE NEED TO TEST IF AN ALINE ALREADY EXITS FOR THE TWO NODES AND REMOVE THE ONE WITH THE WORST (higher) SCORE.
                //explored = explored.stream().filter().collect(List::ARRAYLIST)

                //Reiterate to find the lowest cost for all of the explored nodes.
                nextNode = explored.stream().filter((n)-> n.getresearch()).min(Comparator.comparing(Aline::getCost)).get().getNode(snode);
            }

            //Rebuild the path backwards.
            prevNode = dnode;
            way = new HashMap<>();
            while (prevNode != snode) {

            }
            path.addToRoute(way);

            //Prepare the next loop.
            snode=dnode;
        }

        return path;
    }
}
*/