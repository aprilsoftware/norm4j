---
name: norm4j-dev
description: Guidance for implementing application persistence with norm4j: entity annotations, MetadataManager/TableManager setup, CRUD, joins, query builders, native queries, RecordMapper, SchemaSynchronizer, Maven DDL generation, and Jakarta EE/test configuration.
compatibility: Java 17 and Maven projects using org.norm4j artifacts.
---

# norm4j-dev

## Purpose

Use this skill to help users implement and troubleshoot **norm4j** usage in Java applications.

## What this skill covers

- Dependency setup for `norm4j-core` and dialect modules
- Entity/record modeling with norm4j annotations
- Bootstrapping with `MetadataManager` and `TableManager`
- CRUD operations with `TableManager`
- Relationship loading with `joinOne` / `joinMany`
- Querying with `SelectQueryBuilder`, `UpdateQueryBuilder`, `DeleteQueryBuilder`
- Native SQL through `Query`
- DTO/domain mapping with `RecordMapper` and `RecordMapperBuilder`
- Schema lifecycle with `SchemaSynchronizer`
- Build-time schema/DDL generation with `norm4j-maven-plugin` (`generate-ddl`)
- Jakarta EE wiring examples and test DB setup used in `norm4j-test`

## Repository references

Load only the relevant reference file(s):

- `references/00-repo-map.md` — modules and responsibilities
- `references/10-readme-usage.md` — documented usage examples
- `references/20-core-api-surface.md` — core public API surface from source
- `references/30-schema-and-plugin.md` — schema sync and Maven plugin behavior
- `references/40-jakarta-and-tests.md` — Jakarta EE pattern and test setup

## When to use which reference

- Setup, entities, CRUD, joins, queries, mapping → `10-readme-usage.md`
- Method availability / exact class APIs → `20-core-api-surface.md`
- Versioned schema, migration flow, generated files → `30-schema-and-plugin.md`
- CDI injection, datasource/test properties, test run flow → `40-jakarta-and-tests.md`

## Response format

For implementation requests, provide:

1. short implementation plan
2. concrete code snippet(s)
3. target file path(s)
4. validation command(s)
5. caveats specific to the requested feature

## Output style

- Use exact class/method names from repository sources.
- Keep examples minimal and runnable.
- Prefer practical implementation steps over general theory.
