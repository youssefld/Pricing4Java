package io.github.isagroup.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Object to model pricing features
 */
@Getter
@Setter
public class Feature {
    public String description;
    public FeatureType type;
    public Object defaultValue;
    public Object value;
    public String expression;
    public String serverExpression;

    public void prepareToPlanWriting(){
        this.description = null;
        this.type = null;
        this.defaultValue = null;
        this.expression = null;
        this.serverExpression = null;
    }

    public String toString(){
        return "Feature[description: " + description + ", type: " + type + ", defaultValue: " + defaultValue + "]";
    }
}
