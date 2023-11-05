package io.github.isagroup.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Object to model pricing features
 */
@Getter
@Setter
public abstract class Feature {
    protected String description;
    protected ValueType valueType;
    protected Object defaultValue;
    protected FeatureType type;
    protected Object value;
    protected String expression;
    protected String serverExpression;

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
