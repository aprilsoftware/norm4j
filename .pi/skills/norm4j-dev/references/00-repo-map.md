# norm4j repository map

## Maven modules in root aggregator (`pom.xml`)

- `norm4j-core`
- `norm4j-mariadb`
- `norm4j-maven-plugin`
- `norm4j-oracle`
- `norm4j-postgresql`
- `norm4j-sqlserver`

## Main responsibilities

- `norm4j-core`: annotations, metadata, table manager, query builders, mapping DSL, schema synchronization.
- `norm4j-<dialect>` modules: DB-specific `SQLDialect` implementations.
- `norm4j-maven-plugin`: `generate-ddl` Mojo generating `schema.json` and per-dialect `ddl.sql`.
- `norm4j-test`: integration-style tests and sample resources (`application-test.properties`, generated db resources).

## Java level

Each module POM declares `maven.compiler.release` as `17`.
