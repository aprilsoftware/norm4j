package org.norm4j.dialects;

import java.sql.Connection;

import org.norm4j.metadata.TableMetadata;
import org.norm4j.schema.Schema.Column;
import org.norm4j.schema.Schema.ForeignKey;
import org.norm4j.schema.Schema.Sequence;
import org.norm4j.schema.Schema.Table;

public class GenericDialect extends AbstractDialect {
    public GenericDialect() {
    }

    public boolean isDialect(String productName) {
        throw new UnsupportedOperationException();
    }

    public boolean isTupleSupported() {
        throw new UnsupportedOperationException();
    }

    public String createSequenceTable(String schema, String tableName, String pkColumnName, String valueColumnName) {
        throw new UnsupportedOperationException();
    }

    public boolean sequenceExists(Connection connection, String schema, String sequenceName) {
        throw new UnsupportedOperationException();
    }

    public boolean tableExists(Connection connection, String schema, String tableName) {
        throw new UnsupportedOperationException();
    }

    public String createSequence(String schema, String sequenceName, int initialValue) {
        throw new UnsupportedOperationException();
    }

    public String createSequence(Sequence sequence) {
        throw new UnsupportedOperationException("Unimplemented method 'createSequence'");
    }

    public String createTable(TableMetadata table) {
        throw new UnsupportedOperationException();
    }

    public String createTable(Table table) {
        throw new UnsupportedOperationException("Unimplemented method 'createTable'");
    }

    public String alterTable(String tableSchema, String tableName, Column column) {
        throw new UnsupportedOperationException("Unimplemented method 'addColumn'");
    }

    public String alterTable(String tableSchema, String tableName, ForeignKey foreignKey) {
        throw new UnsupportedOperationException("Unimplemented method 'addForeignKey'");
    }

    public String limitSelect(int offset, int limit) {
        throw new UnsupportedOperationException();
    }
}
