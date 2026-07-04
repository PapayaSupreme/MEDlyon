package com.medlyon.backend.core.structure;

public class Aline {
    final Node n1;
    final Node n2;
    final double cost;
    Aline prev;
    boolean researched;

    public Aline(Node node1, Node node2, double cost){
        this.n1= node1;
        this.n2= node2;
        this.cost = cost;
        this.researched = false;
    }
    public Aline(Node node1, Node node2, double cost, Aline prev){
        this.n1= node1;
        this.n2= node2;
        this.cost = cost;
        this.prev = prev;
        this.researched = false;
    }

    public void research(){
        this.researched = true;
    }

    public boolean getresearch(){
        return this.researched;
    }

    public double getCost(){
        return this.cost;
    }

    public Node getNode(Node present){
        if (this.n1 == present){
            return this.n2;
        } else {
            return this.n1;
        }
    }

    public Node getN1(){
        return this.n1;
    }
    public Node getN2(){
        return this.n2;
    }
}
