package io.github.isagroup.models;

import java.util.List;

public class Plan {
    public String name;
    public String description;
    public Double price;
    public String currency;
    public List<Feature> features;

    public String toString(){
        return name + " plan: " + features.toString();
    }
}
