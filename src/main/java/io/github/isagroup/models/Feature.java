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
    protected Object value;
    protected String expression;
    protected String serverExpression;

    public void prepareToPlanWriting(){
        this.description = null;
        this.defaultValue = null;
        this.expression = null;
        this.serverExpression = null;
    }
}
