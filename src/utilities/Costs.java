package utilities;

import structure.Distance;
import structure.Node;

public class Costs {
    
    public static double computeCost(Node n1, Node n2, Distance dist, String... args){
        return dist.meters();
    }
}
