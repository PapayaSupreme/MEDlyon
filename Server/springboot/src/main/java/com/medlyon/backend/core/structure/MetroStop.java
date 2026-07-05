package com.medlyon.backend.core.structure;


public class MetroStop extends Node {

    private final String desserte;

    public MetroStop(String id, String name, Coordinates coordinates, String desserte) {
        super(id, name, coordinates);
        this.desserte = desserte;
    }

    public String getDesserte() {
        return desserte;
    }

    @Override
    String getStopType() {
        return "MetroStop";
    }
}
