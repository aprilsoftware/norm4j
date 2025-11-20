package org.norm4j.metadata;

import org.norm4j.Join;

public class ForeignKeyMetadata {
    private final TableMetadata table;
    private final TableMetadata referenceTable;
    private final Join join;
    private final String foreignKeyName;

    public ForeignKeyMetadata(String foreignKeyName, TableMetadata table, TableMetadata referenceTable, Join join) {
        this.foreignKeyName = foreignKeyName;

        this.table = table;

        this.referenceTable = referenceTable;

        this.join = join;
    }

    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public TableMetadata getTable() {
        return table;
    }

    public TableMetadata getReferenceTable() {
        return referenceTable;
    }

    public Join getJoin() {
        return join;
    }
}
