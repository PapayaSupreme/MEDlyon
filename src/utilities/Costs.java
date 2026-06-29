package utilities;

import structure.Node;

import java.util.Objects;

public class Costs {
    
    public static double computeCost(Node n1, Node n2, String mode){
        if (Objects.equals(mode, "Haversine")){
            return Tools.haversineMeters(
                    n1.getCoordinates().latitude(),
                    n1.getCoordinates().longitude(),
                    n2.getCoordinates().latitude(),
                    n2.getCoordinates().longitude()
            );

        } else {
            System.out.println(mode + "is not supported yet");
            return 0;
        }
    }
}
