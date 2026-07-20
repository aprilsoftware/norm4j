# Schema synchronization and Maven DDL generation

## `SchemaSynchronizer`

Top-level API:

- `schemaVersionTable(String)`
- `schema(String)`
- `databaseResourcePath(String)` (default `db`)
- `version(String)`
- `startFromFirstVersion(boolean)`
- `apply()`

Version API (`VersionBuilder`):

- `description(String)`
- `execute(String)` / `execute(Query)`
- dialect-scoped overloads with `Class<? extends SQLDialect>`
- `executeResource(String)` and dialect-scoped overload
- `executeIfInitial(...)`
- `executeResourceIfInitial(...)`
- `endVersion()`

Schema API (`SchemaBuilder`):

- `enableAutoCreation(boolean)`
- `enableAutoMigration(boolean)`
- `endSchema()`

## Execution behavior

### Default mode: `startFromFirstVersion(false)`

- If no version is applied yet:
  - only the last configured version is applied
  - if auto-creation is enabled on that version, schema is created from `db/<version>/schema.json`
  - initial-only statements (`executeIfInitial*`) are executed
- If at least one version is already applied:
  - missing versions are applied in order
  - first missing version may auto-create schema
  - next versions can auto-migrate (`previous schema.json` -> `current schema.json`)
  - normal statements (`execute*`) are executed

### Replay mode: `startFromFirstVersion(true)`

- starts from first configured version even on fresh DB
- supports auto-creation on first version and auto-migration for subsequent versions
- uses normal `execute*` migration statements

## Maven plugin: `generate-ddl`

Config parameters:

- `packages`
- `dialects`
- `outputDirectory` (default `${project.basedir}/src/main/resources/db`)
- `version` (default `${norm4j.schema.version}`)
- `schema` (default `true`)
- `ddl` (default `true`)

Generated outputs:

- schema snapshot: `<outputDirectory>/<version>/schema.json`
- DDL per dialect: `<outputDirectory>/<version>/<dialect>/ddl.sql`

Package scanning uses project compile+test classpath.
