package io.github.isagroup.models;

public class Feature {
    public String name;
    public String description;
    public FeatureType type;
    public Object value;

    public String toString(){
        return name + ": " + value.toString();
    }
}
