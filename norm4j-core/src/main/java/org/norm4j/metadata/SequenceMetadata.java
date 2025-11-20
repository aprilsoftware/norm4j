package org.norm4j.metadata;

public class SequenceMetadata {
    private final String schema;
    private final String name;
    private final int initialValue;

    public SequenceMetadata(String schema, String name, int initialValue) {
        this.schema = schema;
        this.name = name;
        this.initialValue = initialValue;
    }

    public String getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }

    public int getInitialValue() {
        return initialValue;
    }
}
