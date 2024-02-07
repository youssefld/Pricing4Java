package io.github.isagroup.models;

import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import io.github.isagroup.exceptions.CloneUsageLimitException;

@Getter
@Setter
public abstract class UsageLimit implements Serializable {
    private String name;
    private String description;
    private ValueType valueType;
    private Object defaultValue;
    private String unit;
    private transient Object value;
    private List<String> linkedFeatures;
    private String expression;
    private String serverExpression;

    public static UsageLimit cloneUsageLimit(UsageLimit original) throws CloneUsageLimitException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(original);

            // Deserializa el objeto desde el flujo de bytes, creando una copia
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

            return (UsageLimit) objectInputStream.readObject();

        } catch (Exception e) {
            throw new CloneUsageLimitException("Error cloning usageLimit");
        }
    }

}
