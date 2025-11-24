package org.norm4j.dialects;

import java.sql.Connection;

import org.norm4j.metadata.TableMetadata;
import org.norm4j.schema.SchemaColumn;
import org.norm4j.schema.SchemaJoin;
import org.norm4j.schema.SchemaTable;

public class GenericDialect extends AbstractDialect {
    public GenericDialect() {
    }

    @Override
    public boolean isDialect(String productName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTupleSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String createSequenceTable(String schema, String tableName, String pkColumnName, String valueColumnName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean sequenceExists(Connection connection, String schema, String sequenceName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tableExists(Connection connection, String schema, String tableName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String createSequence(String schema, String sequenceName, int initialValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String createTable(TableMetadata table) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String createTable(SchemaTable table) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String alterTableAddColumn(SchemaTable table, SchemaColumn column) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String alterTableAddForeignKey(SchemaTable table, SchemaJoin join, String foreignKeyName,
            String referenceTable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String limitSelect(int offset, int limit) {
        throw new UnsupportedOperationException();
    }
}
