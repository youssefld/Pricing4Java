package io.github.isagroup.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Feature {
    public String description;
    public FeatureType type;
    public Object value;

    public String toString(){
        return "Feature[description: " + description + ", type: " + type + ", value: " + value + "]";
    }
}
