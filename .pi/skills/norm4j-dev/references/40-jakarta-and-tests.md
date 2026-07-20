# Jakarta EE integration and test setup

## Jakarta EE integration pattern

Typical setup:

- inject datasource with `@Resource`
- create `MetadataManager`
- register tables or package
- initialize schema using either:
  - `metadataManager.createTables(dataSource)`, or
  - `new SchemaSynchronizer(tableManager).version(...).apply()`
- expose `TableManager` with `@Produces`
- inject `TableManager` in services with `@Inject`

## Test configuration

Properties file used by tests:

- `norm4j-test/src/test/resources/application-test.properties`

Base test behavior:

- loads datasource properties from that file
- creates datasource with Apache DBCP
- detects SQL dialect at runtime with `SQLDialect.detectDialect(connection)`

## Running tests

```bash
cd norm4j-test
mvn clean test
```

## Database setup examples included

Setup examples are available for:

- PostgreSQL
- MariaDB
- SQL Server

including user/database/schema creation patterns.
