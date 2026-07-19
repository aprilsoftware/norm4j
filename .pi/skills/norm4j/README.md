# norm4j Skill

A comprehensive skill for working with norm4j, a lightweight SQL-centric alternative to JPA for Java applications.

## Overview

This skill provides tools, examples, and documentation to help developers work effectively with norm4j. It includes:

- **Documentation**: Comprehensive guides and reference material
- **Examples**: Practical code examples for common scenarios
- **Helpers**: Utility functions for working with norm4j
- **Commands**: Automated generation of entities, DDL, migrations, and more

## Installation

The skill is automatically available when you have norm4j installed in your project. No additional setup is required.

## Usage

### Basic Commands

```bash
# Create a new entity
norm4j:create-entity --name Customer --package com.example.entities

# Generate DDL for a schema
norm4j:generate-ddl --package com.example.entities --dialect postgresql

# Create a new migration
norm4j:create-migration --version v0.2 --description "Add email field"

# Explain a norm4j query
norm4j:explain-query "SELECT * FROM customer WHERE id = ?"
```

### Interactive Help

```bash
# Get help for any command
norm4j:help
norm4j:help create-entity
norm4j:help generate-ddl
```

## Key Features

### Entity Generation

Generate complete entity classes with proper annotations:

```bash
norm4j:create-entity --name Author --package com.example.entities --tableName authors
```

### Schema Management

Generate DDL and manage schema versions:

```bash
norm4j:generate-ddl --package com.example.entities --dialect postgresql
norm4j:create-migration --version v0.1 --description "Initial schema"
```

### Query Analysis

Understand what SQL will be generated from norm4j queries:

```bash
norm4j:explain-query "SELECT c FROM Customer c WHERE c.active = true"
```

### DTO Mapping

Generate DTO classes and mappers:

```bash
norm4j:generate-dto --entity Customer --package com.example.dtos
norm4j:generate-mapper --entity Customer --dto CustomerDTO
```

## Documentation

### Getting Started

See [SKILL.md](SKILL.md) for comprehensive documentation on:

- **Core concepts**: Entities, TableManager, Query Builder
- **Relationships**: One-to-many, many-to-one patterns
- **Schema management**: Versioning and migrations
- **DTO mapping**: RecordMapper and RecordMapperBuilder
- **Advanced features**: Composite keys, arrays, enums

### Examples

See [examples.md](examples.md) for practical examples covering:

- **Basic CRUD**: Create, read, update, delete operations
- **Relationships**: Working with joins and associations
- **Query building**: Complex queries and joins
- **Schema management**: Versioning and migrations
- **DTO mapping**: Mapping entities to DTOs
- **Advanced features**: Composite keys, arrays, enums
- **Jakarta EE integration**: CDI and transaction management
- **Testing**: Unit and integration testing patterns
- **Performance**: Best practices and optimization

### Helpers

See [helpers.js](helpers.js) for utility functions:

- **Entity validation**: Check if a class is a valid norm4j entity
- **Information extraction**: Extract entity metadata from Java classes
- **Configuration generation**: Generate norm4j configurations
- **Test base classes**: Generate test infrastructure
- **Maven plugin configs**: Generate DDL generation configurations
- **Schema synchronizers**: Generate migration code
- **DTO generation**: Generate DTO classes from entities
- **RecordMapper generation**: Generate mapper configurations
- **Query analysis**: Analyze and validate norm4j queries
- **Project structure**: Generate complete project layouts
- **Database configuration**: Generate database connection configs

## Commands Reference

### Entity Commands

#### `norm4j:create-entity`

Generate a basic entity class

**Options:**
- `--name` or `-n`: Entity name
- `--package` or `-p`: Java package
- `--tableName` or `-t`: Table name (optional)

**Example:**
```bash
norm4j:create-entity -n Customer -p com.example.entities -t customers
```

#### `norm4j:validate-entity`

Validate entity annotations

**Options:**
- `--file` or `-f`: Java file to validate
- `--content` or `-c`: Java content to validate

**Example:**
```bash
norm4j:validate-entity -f src/main/java/com/example/Customer.java
```

### Schema Commands

#### `norm4j:generate-ddl`

Generate DDL for a schema

**Options:**
- `--package` or `-p`: Java package to scan
- `--dialect` or `-d`: Database dialect
- `--output` or `-o`: Output file (optional)

**Example:**
```bash
norm4j:generate-ddl -p com.example.entities -d postgresql -o ddl.sql
```

#### `norm4j:create-migration`

Create a new migration version

**Options:**
- `--version` or `-v`: Version name
- `--description` or `-d`: Version description
- `--output` or `-o`: Output file (optional)

**Example:**
```bash
norm4j:create-migration -v v0.2 -d "Add email field" -o migration.java
```

### Query Commands

#### `norm4j:explain-query`

Explain a norm4j query

**Options:**
- `--query` or `-q`: norm4j query
- `--dialect` or `-d`: Database dialect (optional)

**Example:**
```bash
norm4j:explain-query -q "SELECT c FROM Customer c WHERE c.active = true"
```

#### `norm4j:validate-query`

Validate a norm4j query

**Options:**
- `--query` or `-q`: norm4j query

**Example:**
```bash
norm4j:validate-query -q "SELECT c FROM Customer c WHERE c.active = true"
```

### DTO Commands

#### `norm4j:generate-dto`

Generate a DTO class from an entity

**Options:**
- `--entity` or `-e`: Entity class name
- `--package` or `-p`: Target package
- `--fields` or `-f`: Fields to include (comma-separated)

**Example:**
```bash
norm4j:generate-dto -e Customer -p com.example.dtos -f id,name,email
```

#### `norm4j:generate-mapper`

Generate a RecordMapper configuration

**Options:**
- `--entity` or `-e`: Entity class name
- `--dto` or `-d`: DTO class name
- `--mappings` or `-m`: Field mappings (format: source:target)
- `--joins` or `-j`: Join configurations (format: target:entity:dto)

**Example:**
```bash
norm4j:generate-mapper -e Customer -d CustomerDTO -m "id:id,name:name,email:email" -j "orders:Order:OrderDTO"
```

### Project Commands

#### `norm4j:generate-config`

Generate a basic norm4j configuration

**Options:**
- `--package` or `-p`: Base package name
- `--dialect` or `-d`: Database dialect
- `--output` or `-o`: Output file (optional)

**Example:**
```bash
norm4j:generate-config -p com.example -d postgresql -o Norm4jConfig.java
```

#### `norm4j:generate-test-base`

Generate a test base class

**Options:**
- `--package` or `-p`: Test package
- `--dialect` or `-d`: Database dialect
- `--output` or `-o`: Output file (optional)

**Example:**
```bash
norm4j:generate-test-base -p com.example.tests -d postgresql -o BaseTest.java
```

#### `norm4j:generate-project-structure`

Generate a basic norm4j project structure

**Options:**
- `--baseDir` or `-b`: Base directory
- `--package` or `-p`: Base package
- `--output` or `-o`: Output file (optional)

**Example:**
```bash
norm4j:generate-project-structure -b . -p com.example -o project-structure.txt
```

### Database Commands

#### `norm4j:generate-database-config`

Generate database-specific configuration

**Options:**
- `--dialect` or `-d`: Database dialect
- `--url` or `-u`: JDBC URL
- `--username` or `-n`: Database username
- `--password` or `-p`: Database password
- `--output` or `-o`: Output file (optional)

**Example:**
```bash
norm4j:generate-database-config -d postgresql -u "jdbc:postgresql://localhost:5432/mydb" -n user -p password -o DatabaseConfig.java
```

### Maven Commands

#### `norm4j:generate-maven-plugin-config`

Generate Maven plugin configuration

**Options:**
- `--package` or `-p`: Package to scan
- `--dialects` or `-d`: Dialects to generate (comma-separated)
- `--version` or `-v`: Schema version
- `--output` or `-o`: Output file (optional)

**Example:**
```bash
norm4j:generate-maven-plugin-config -p com.example.entities -d postgresql,mariadb -v v0.1 -o plugin-config.xml
```

## Examples

### Creating a Complete Entity

```bash
# Create the entity class
norm4j:create-entity -n Customer -p com.example.entities -t customers

# Validate the entity
norm4j:validate-entity -f src/main/java/com/example/entities/Customer.java

# Generate DDL
norm4j:generate-ddl -p com.example.entities -d postgresql

# Create initial migration
norm4j:create-migration -v v0.1 -d "Initial customer schema"
```

### Setting Up DTO Mapping

```bash
# Generate DTO
norm4j:generate-dto -e Customer -p com.example.dtos -f id,name,email,createdAt

# Generate mapper
norm4j:generate-mapper -e Customer -d CustomerDTO -m "id:id,name:name,email:email,createdAt:createdAt"
```

### Creating a Migration

```bash
# Create migration for adding a new field
norm4j:create-migration -v v0.2 -d "Add phone number field"

# Edit the generated migration to include:
.execute("ALTER TABLE customer ADD COLUMN phone VARCHAR(20)")
```

### Analyzing Queries

```bash
# Explain a complex query
norm4j:explain-query -q "SELECT c FROM Customer c JOIN c.orders o WHERE c.active = true AND o.total > 100"

# Validate query syntax
norm4j:validate-query -q "SELECT c FROM Customer c WHERE c.id = ?"
```

## Best Practices

### Entity Design

1. **Keep entities simple**: Focus on data structure, not business logic
2. **Use appropriate ID generation**: Choose strategy based on your database
3. **Add proper constraints**: Use @Column annotations for nullable/unique
4. **Consider table naming**: Use consistent naming conventions

### Query Design

1. **Use explicit joins**: Don't rely on implicit loading
2. **Limit result sets**: Use pagination for large datasets
3. **Select only needed fields**: Avoid SELECT *
4. **Use indexes**: Create indexes for frequently queried columns

### Schema Management

1. **Version your schema**: Always use SchemaSynchronizer
2. **Test migrations**: Test migrations on a staging database
3. **Backup before migrating**: Especially for production
4. **Document changes**: Add clear descriptions to migrations

### Performance

1. **Use connection pooling**: Essential for production
2. **Batch operations**: Reduce round trips for bulk operations
3. **Avoid N+1 queries**: Use joins instead of multiple single queries
4. **Monitor queries**: Use EXPLAIN to analyze query performance

## Troubleshooting

### Common Issues

**Issue**: "No metadata found for class"
- **Solution**: Ensure the class is registered with MetadataManager
- **Command**: `norm4j:validate-entity` to check your entity

**Issue**: "No primary key found"
- **Solution**: Add @Id annotation to primary key field
- **Command**: `norm4j:create-entity` will generate proper entities

**Issue**: "Join not found"
- **Solution**: Ensure @Join annotation is properly configured
- **Command**: `norm4j:validate-entity` can help identify issues

**Issue**: SQL syntax errors
- **Solution**: Check database dialect compatibility
- **Command**: `norm4j:generate-ddl` with correct dialect

### Debugging Queries

```bash
# Explain the query to see what SQL will be generated
norm4j:explain-query -q "YOUR_QUERY_HERE"

# Validate the query syntax
norm4j:validate-query -q "YOUR_QUERY_HERE"

# Check entity definitions
norm4j:validate-entity -f YOUR_ENTITY_FILE.java
```

## Resources

- **norm4j Documentation**: [https://github.com/aprilsoftware/norm4j](https://github.com/aprilsoftware/norm4j)
- **Medium Articles**: [https://medium.com/@cedric.nanni](https://medium.com/@cedric.nanni)
- **norm4j Tests**: [norm4j-test module](https://github.com/aprilsoftware/norm4j/tree/main/norm4j-test)

## Support

For issues or questions about the norm4j skill:

1. Check the documentation in this directory
2. Look at the examples in [examples.md](examples.md)
3. Review the helper functions in [helpers.js](helpers.js)
4. Consult the main norm4j documentation

For norm4j itself, refer to the [norm4j repository](https://github.com/aprilsoftware/norm4j).

## License

This skill is provided under the same license as norm4j itself (Apache License 2.0).

## Contributing

Contributions to the norm4j skill are welcome! Please:

1. Follow the existing code style and patterns
2. Add tests for new functionality
3. Update documentation when making changes
4. Submit pull requests to the main norm4j repository
