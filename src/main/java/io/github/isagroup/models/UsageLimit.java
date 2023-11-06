package io.github.isagroup.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class UsageLimit {
    private String description;
    private ValueType valueType;
    private Object defaultValue;
    private String unit;
    private Object value;
    private String linkedFeature;
    private String expression;
    private String serverExpression;
}
