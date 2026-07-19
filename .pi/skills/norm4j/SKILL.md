---
name: norm4j
description: Comprehensive support for norm4j - a lightweight SQL-centric alternative to JPA for Java applications. Provides entity generation, DDL creation, migration management, query analysis, DTO mapping, and project setup assistance.
---

# norm4j

A comprehensive skill for working with norm4j, a lightweight SQL-centric alternative to JPA for Java applications.

## Features

- **Entity Generation**: Create properly annotated entity classes with @Table, @Id, @GeneratedValue, and @Column annotations
- **DDL Generation**: Generate database-specific DDL for PostgreSQL, SQL Server, MariaDB, and Oracle
- **Migration Creation**: Create schema migration versions with SchemaSynchronizer for versioned database changes
- **Query Analysis**: Explain what SQL will be generated from norm4j queries and validate query syntax
- **Entity Validation**: Validate entity annotations and structure to catch common issues early
- **DTO Mapping**: Generate DTO classes and RecordMapper configurations for clean separation of concerns
- **Project Setup**: Generate complete project structures, configurations, and test infrastructure
- **Database Configuration**: Generate dialect-specific database configurations with connection pooling
- **Maven Integration**: Generate Maven plugin configurations for automatic DDL generation during builds

## Commands

### Entity Commands

- `create-entity`: Generate a basic entity class with proper annotations
  ```bash
  norm4j:create-entity --name Customer --package com.example.entities --tableName customers
  ```

- `validate-entity`: Validate entity annotations and structure
  ```bash
  norm4j:validate-entity --file src/main/java/com/example/Customer.java
  ```

### Schema Commands

- `generate-ddl`: Generate DDL for a schema
  ```bash
  norm4j:generate-ddl --package com.example.entities --dialect postgresql
  ```

- `create-migration`: Create a new migration version
  ```bash
  norm4j:create-migration --version v0.1 --description "Initial schema"
  ```

### Query Commands

- `explain-query`: Explain a norm4j query and show generated SQL
  ```bash
  norm4j:explain-query --query "SELECT c FROM Customer c WHERE c.active = true"
  ```

- `validate-query`: Validate a norm4j query for syntax issues
  ```bash
  norm4j:validate-query --query "SELECT c FROM Customer c WHERE c.id = ?"
  ```

### DTO Commands

- `generate-dto`: Generate a DTO class from an entity
  ```bash
  norm4j:generate-dto --entity Customer --package com.example.dtos --fields id,name,email
  ```

- `generate-mapper`: Generate a RecordMapper configuration
  ```bash
  norm4j:generate-mapper --entity Customer --dto CustomerDTO --mappings id:id,name:name,email:email
  ```

### Project Commands

- `generate-config`: Generate a basic norm4j configuration
  ```bash
  norm4j:generate-config --package com.example --dialect postgresql
  ```

- `generate-test-base`: Generate a test base class
  ```bash
  norm4j:generate-test-base --package com.example.tests --dialect postgresql
  ```

- `generate-project-structure`: Generate a basic norm4j project structure
  ```bash
  norm4j:generate-project-structure --baseDir . --package com.example
  ```

### Database Commands

- `generate-database-config`: Generate database-specific configuration
  ```bash
  norm4j:generate-database-config --dialect postgresql --url "jdbc:postgresql://localhost:5432/mydb" --username user --password password
  ```

- `generate-maven-plugin-config`: Generate Maven plugin configuration
  ```bash
  norm4j:generate-maven-plugin-config --package com.example.entities --dialects postgresql,mariadb --version v0.1
  ```

## Usage Examples

### Creating a Basic Entity

```bash
# Create a Customer entity
norm4j:create-entity --name Customer --package com.example.entities --tableName customers

# Validate the entity
norm4j:validate-entity --file src/main/java/com/example/entities/Customer.java
```

### Generating Database Schema

```bash
# Generate PostgreSQL DDL
norm4j:generate-ddl --package com.example.entities --dialect postgresql

# Create initial migration
norm4j:create-migration --version v0.1 --description "Initial schema"
```

### Working with Queries

```bash
# Explain a query to see generated SQL
norm4j:explain-query --query "SELECT c FROM Customer c WHERE c.active = true"

# Validate query syntax
norm4j:validate-query --query "SELECT c FROM Customer c WHERE c.id = ?"
```

### DTO Mapping

```bash
# Generate DTO
norm4j:generate-dto --entity Customer --package com.example.dtos --fields id,name,email

# Generate mapper
norm4j:generate-mapper --entity Customer --dto CustomerDTO --mappings id:id,name:name,email:email
```

## Setup

No setup required. The skill uses Node.js and is compatible with:
- Node.js 14+
- norm4j library (1.1.25+)
- Java 11+

## Requirements

- Node.js 14 or higher
- norm4j library version 1.1.25 or higher
- Java 11 or higher
- Maven 3.6+ (for Maven plugin commands)

## License

Apache License 2.0

## Documentation

For more information, see:
- [examples.md](examples.md) - Practical examples of norm4j usage
- [demo.md](demo.md) - Step-by-step usage demonstrations
- [README.md](README.md) - Complete user guide
- [SKILL.md](SKILL.md) - This reference documentation
