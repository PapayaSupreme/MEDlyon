package com.medlyon.backend.core.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 */
public class Route {
    Map<Integer, Aline> Line;

    public Route(){
        this.Line= new HashMap<>();
    }

    public Map<Integer,Aline> getTrip(){
        return this.Line;
    }

    public void setRoute(Map<Integer, Aline> route){
        this.Line = route;
    }

    public void add(Aline line){
        this.Line.put(this.Line.size(), line);
    }

    public void addToRoute(Map<Integer, Aline> routeMap){
        Integer nextId = this.Line.size();
        SortedSet<Integer> sortedkeyset = new TreeSet<>(routeMap.keySet());
        for (Integer key : sortedkeyset){
            this.Line.put(nextId+key,routeMap.get(key));
        }
    }
    
}
