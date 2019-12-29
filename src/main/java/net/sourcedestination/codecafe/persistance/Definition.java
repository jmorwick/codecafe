package net.sourcedestination.codecafe.persistance;

import java.util.Map;
import java.util.Objects;

public class Definition {

    public enum DefinitionType {
        VARIABLE, FUNCTION, CLASS
    }

    private final DefinitionType defintionType;
    private final String dataType;
    private final String identifier;
    private final String value;

    public Definition(DefinitionType defintionType, String dataType, String identifier, String value) {
        this.defintionType = defintionType;
        this.dataType = dataType;
        this.identifier = identifier;
        this.value = value;
    }

    public DefinitionType getDefinitionType() {
        return defintionType;
    }

    public String getDataType() { return dataType; }

    public String getIdentifier() {
        return identifier;
    }

    public String getValue() {  return value; }

    public Map<String,String> toPropertyMap() {
        return Map.of(
                "definition", defintionType.toString(),
                "type", dataType,
                "id", identifier,
                "value", value,
                "valid", "" + (value != null)
        );
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Definition that = (Definition) o;
        return defintionType == that.defintionType &&
                dataType.equals(that.dataType) &&
                identifier.equals(that.identifier) &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defintionType, dataType, identifier, value);
    }

    @Override
    public String toString() {
        return "[id: " + getIdentifier() + ", " +
                "deftype: " + getDefinitionType() + ", " +
                "value: " + getValue() + ", " +
                "datatype: " + getDataType() + "]";
    }
}
