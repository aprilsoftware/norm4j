package org.norm4j.schema;

import java.util.ArrayList;
import java.util.List;

import org.norm4j.metadata.ColumnMetadata;
import org.norm4j.metadata.MetadataManager;
import org.norm4j.metadata.TableMetadata;

public class SchemaGenerator {
    private final MetadataManager metadataManager;

    public SchemaGenerator(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    public Schema generate(String version) {
        Schema schema = new Schema();
        schema.setVersion(version);
        schema.setSchemaModelVersion(1);

        List<Schema.Table> tables = new ArrayList<>();
        List<Schema.Sequence> sequences = new ArrayList<>();

        for (TableMetadata tableMetadata : metadataManager.getTableMetadata()) {
            tables.add(toTable(tableMetadata, sequences));
        }

        schema.setTables(tables);
        schema.setSequences(sequences);

        return schema;
    }

    private Schema.Table toTable(TableMetadata tableMetadata,
            List<Schema.Sequence> sequences) {
        Schema.Table table = new Schema.Table();
        table.setSchema(tableMetadata.getSchema());
        table.setName(tableMetadata.getTableName());

        List<Schema.Column> columns = new ArrayList<>();
        for (ColumnMetadata columnMetadata : tableMetadata.getColumns()) {
            columns.add(toColumn(columnMetadata, sequences));
        }
        table.setColumns(columns);

        // TODO To be continuted...

        table.setForeignKeys(new ArrayList<>());

        return table;
    }

    private Schema.Column toColumn(ColumnMetadata columnMetadata,
            List<Schema.Sequence> sequences) {
        Schema.Column column = new Schema.Column();
        column.setName(columnMetadata.getColumnName());

        // TODO To be continuted...

        return column;
    }
}
