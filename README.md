# norm4j

**norm4j** (Not an ORM for Java) is a lightweight, SQL-centric alternative to JPA, built for developers who want more control and better performance without giving up the productivity benefits of annotation-driven programming.

While inspired by JPA to simplify migration and ease adoption, norm4j breaks away from traditional ORM patterns. It does not load object graphs by default. Instead, entities can define relationships using familiar annotations, but related data is only loaded explicitly when needed—providing fine-grained control over performance and memory usage.

norm4j focuses on records rather than object hierarchies. There's no inheritance, no automatic eager-loading, and no deep object references by default. However, developers can build wrapper layers or proxy objects on top of norm4j records to simulate object-oriented patterns if desired. This design gives you full control over what gets loaded, when, and how. RecordMapper and RecordMapperBuilder provide a type‑safe bridge from records to fully populated domain objects—so you control *what* gets mapped and *when*.

Built with Jakarta EE in mind, norm4j integrates naturally into modern enterprise stacks. It uses CDI for dependency injection and is designed to work with JTA transactions, interceptors, and connection pooling—making it easy to slot into existing JPA-based applications or microservices.

Despite looking like an ORM, it’s not. norm4j is about control, performance, and staying close to core database concepts like primary keys, foreign keys, and native SQL. It provides just enough abstraction to avoid boilerplate—while keeping your hands on the actual SQL when needed.

Support is available for PostgreSQL, SQL Server, MariaDB, and Oracle (Oracle not yet tested—feedback welcome!). 

We’d love your feedback, ideas, and help with testing across different platforms.

---

## 🔧 Getting Started

### Maven Dependencies

Add the following to your `pom.xml`:

#### Core Library

```xml
<dependency>
    <groupId>org.norm4j</groupId>
    <artifactId>norm4j-core</artifactId>
    <version>1.1.16</version>
</dependency>
```

#### Supported Databases

**PostgreSQL**
```xml
<dependency>
    <groupId>org.norm4j</groupId>
    <artifactId>norm4j-postgresql</artifactId>
    <version>1.1.16</version>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.2</version>
</dependency>
```

**MariaDB**
```xml
<dependency>
    <groupId>org.norm4j</groupId>
    <artifactId>norm4j-mariadb</artifactId>
    <version>1.1.16</version>
</dependency>
<dependency>
    <groupId>org.mariadb.jdbc</groupId>
    <artifactId>mariadb-java-client</artifactId>
    <version>3.5.2</version>
</dependency>
```

**SQL Server**
```xml
<dependency>
    <groupId>org.norm4j</groupId>
    <artifactId>norm4j-sqlserver</artifactId>
    <version>1.1.16</version>
</dependency>
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.10.2.jre11</version>
</dependency>
```

**Oracle**
```xml
<dependency>
    <groupId>org.norm4j</groupId>
    <artifactId>norm4j-oracle</artifactId>
    <version>1.1.16</version>
</dependency>
```

---

### Optional: norm4j DDL Maven Plugin

To automatically generate DDL scripts from your `@Table` classes at build time, you can use the **norm4j Maven plugin**.

Example configuration (single dialect, PostgreSQL):

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.norm4j</groupId>
      <artifactId>norm4j-maven-plugin</artifactId>
      <version>1.1.16</version>
      <configuration>
        <!-- Packages to scan for @Table-annotated classes -->
        <packages>
          <package>com.mycompany.app.records</package>
        </packages>

        <!-- Dialects for which to generate DDL -->
        <dialects>
          <dialect>postgresql</dialect>
        </dialects>

        <!-- Schema version label used in the folder path -->
        <version>v0.1</version>

        <!-- Optional: Base output directory (per project) -->
        <outputDirectory>${project.basedir}/src/main/resources/db</outputDirectory>
      </configuration>
      <executions>
        <execution>
          <!-- Optional: VS Code / m2e hint to execute in IDE builds -->
          <?m2e execute onConfiguration,onIncremental?>
          <id>generate-ddl</id>
          <!-- Run after classes are compiled -->
          <phase>process-classes</phase>
          <goals>
            <goal>generate-ddl</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

This will generate, for each configured dialect:

```text
src/main/resources/db/<version>/<dialect>/ddl.sql
```

For the example above:

```text
src/main/resources/db/v0.1/postgresql/ddl.sql
```

You can then feed this SQL into `SchemaSynchronizer` via `executeResourceIfInitial(...)` (see **Schema Versioning & Migrations** below).

For tests or multi-dialect scenarios, you can configure several dialects and a test resources output directory, for example:

```xml
<plugin>
  <groupId>org.norm4j</groupId>
  <artifactId>norm4j-maven-plugin</artifactId>
  <version>1.1.16</version>
  <configuration>
    <packages>
      <package>org.norm4j.tests.test15</package>
    </packages>
    <dialects>
      <dialect>mariadb</dialect>
      <dialect>oracle</dialect>
      <dialect>postgresql</dialect>
      <dialect>sqlserver</dialect>
    </dialects>
    <version>v0.1</version>
    <outputDirectory>${project.basedir}/src/test/resources/db</outputDirectory>
  </configuration>
  <executions>
    <execution>
      <?m2e execute onConfiguration,onIncremental?>
      <id>generate-ddl</id>
      <phase>process-test-classes</phase>
      <goals>
        <goal>generate-ddl</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

which will generate:

```text
src/test/resources/db/v0.1/mariadb/ddl.sql
src/test/resources/db/v0.1/oracle/ddl.sql
src/test/resources/db/v0.1/postgresql/ddl.sql
src/test/resources/db/v0.1/sqlserver/ddl.sql
```

---

## 🧱 Defining Entities

Use annotations to define database tables and relationships.

### Author Entity
```java
@Table(name = "author")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    // Getters and Setters ...
}
```

### Book Entity
```java
@Table(name = "book")
@Join(
    columns = "author_id", 
    reference = @Reference(table = Author.class, columns = "id")
)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(name = "author_id")
    private int authorId;

    // Getters and Setters ...
}
```

---

## 🚀 Usage

### Initialize Metadata & TableManager

```java
MetadataManager metadataManager = new MetadataManager();
metadataManager.registerTable(Book.class);
metadataManager.registerTable(Author.class);
metadataManager.createTables(getDataSource());

TableManager tableManager = new TableManager(getDataSource(), metadataManager);
```

**or**

```java
MetadataManager metadataManager = new MetadataManager();
metadataManager.registerPackage("org.norm4j.tests.test11");
metadataManager.createTables(getDataSource());

TableManager tableManager = new TableManager(getDataSource(), metadataManager);
```

### CRUD Operations

**Persist**
```java
Author author = new Author();
author.setName("Author A");
tableManager.persist(author);

Book book = new Book();
book.setName("Book A");
book.setAuthorId(author.getId());
tableManager.persist(book);
```

**Find**
```java
Author loadedAuthor = tableManager.find(Author.class, author.getId());
```

**Merge**
```java
loadedAuthor.setName("Author A+");
tableManager.merge(loadedAuthor);
```

**Remove**
```java
tableManager.remove(book);
tableManager.remove(Author.class, loadedAuthor.getId());
```

---

## 🔗 Relationships

### Join One-to-One
```java
Author bookAuthor = tableManager.joinOne(book, Author.class);
```

### Join One-to-Many
```java
List<Book> books = tableManager.joinMany(author, Book.class);
```

### Join With Custom Key Mapping
```java
List<Book> books = tableManager.joinMany(author, Author::getId, Book.class, Book::getAuthorId);
```

---

## 🔍 Query Builder

### Fluent SQL Builder API
```java
List<Book> books = tableManager.createSelectQueryBuilder()
    .select(Book.class)
    .from(Book.class)
    .innerJoin(Author.class)
    .where(Book::getAuthorId, "=", author.getId())
    .orderBy(Book::getName)
    .getResultList(Book.class);
```

```java
List<Book> books = tableManager.createSelectQueryBuilder()
    .select(Book.class)
    .from(Book.class)
    .where(q -> q.condition(Book::getId, "=", 1)
            .or(Book::getId, "=", 2))
    .and(Book::getAuthorId, "=", author.getId())
    .orderByDesc(Book::getName)
    .getResultList(Book.class);
```

```java
tableManager.createUpdateQueryBuilder()
        .update(Book.class)
        .set(Book::getBookType, BookType.Documentation)
        .where(Book::getId, "=", book1.getId())
    .executeUpdate();
```

```java
tableManager.createDeleteQueryBuilder()
        .from(Book.class)
        .where(Book::getId, "=", book1.getId())
    .executeUpdate();
```

### Native SQL Queries
```java
Query query = tableManager.createQuery("SELECT * FROM book WHERE id = ?");
query.setParameter(1, book.getId());

List<Book> books = query.getResultList(Book.class);
```

---

## 🧬 Schema Versioning & Migrations

norm4j provides a small helper, `SchemaSynchronizer`, to manage schema versions in a **code-first** way.

- Each version has a unique **name** (`"v0.1"`, `"v0.2"`, `"add-orders-2025-11-17"`, …).
- Applied versions are stored in a `schema_version` table (or a custom table name).
- A version can:
  - execute one or more **SQL statements** or `Query` objects,
  - load SQL from **classpath resources** (e.g. DDL generated by the norm4j Maven plugin),
  - optionally restrict execution to a **specific dialect**,
  - define **special statements** that are only executed when this version is the **initial version** applied on an empty database.

You can safely run the synchronizer on every startup; already-applied versions are automatically skipped based on their `name`. All operations for all versions are executed in a single transaction, and a lock is taken on the `schema_version` table to avoid concurrent startups applying the same version twice.

### Version execution model

The `SchemaSynchronizer` execution model is:

- If **no version has been applied yet** (i.e. `schema_version` is empty), it:
  - considers this a **fresh database**,
  - applies **only the last defined version** in your configuration,
  - executes that version’s `executeIfInitial(...)` / `executeResourceIfInitial(...)` statements (the *initial* statements),
  - records that version in `schema_version`.

- If **at least one version exists** in `schema_version`:
  - it behaves as an **upgrade path**,
  - iterates through all configured versions in order,
  - for each version that is not yet in `schema_version`, executes its normal `execute(...)` / `executeResource(...)` statements,
  - records it in `schema_version` after success.

In this upgrade path, the `executeIfInitial*` statements are *ignored*.

This lets you:

- define **full “initial” DDL** for a given version (e.g. using DDL generated by the Maven plugin),
- define **incremental changes** for upgrading from previous versions.

### VersionBuilder API

`SchemaSynchronizer` exposes a fluent `VersionBuilder` with the following main methods:

- `name(String name)` – unique version identifier (required).  
- `description(String description)` – optional human-readable description.

**Normal (upgrade) statements:**

- `execute(String sql)` – execute a raw SQL string for all dialects.  
- `execute(String sql, Class<? extends SQLDialect> dialect)` – execute SQL only for a specific dialect.  
- `execute(Query query)` – execute a `Query` object for all dialects.  
- `execute(Query query, Class<? extends SQLDialect> dialect)` – execute a `Query` only for a specific dialect.  
- `executeResource(String resourcePath)` – load SQL from a classpath resource and execute it for all dialects.  
- `executeResource(String resourcePath, Class<? extends SQLDialect> dialect)` – execute a resource only for a specific dialect.

**Initial-only statements (only when this version is applied first on an empty DB):**

- `executeIfInitial(String sql)`  
- `executeIfInitial(String sql, Class<? extends SQLDialect> dialect)`  
- `executeIfInitial(Query query)`  
- `executeIfInitial(Query query, Class<? extends SQLDialect> dialect)`  
- `executeResourceIfInitial(String resourcePath)`  
- `executeResourceIfInitial(String resourcePath, Class<? extends SQLDialect> dialect)`

These `executeIfInitial*` methods are **only used when**:

- `schema_version` is currently empty, and  
- this version is the **last** version in your `SchemaSynchronizer` configuration.

In that case, only its `executeIfInitial*` statements are executed (normal `execute*` are ignored).  
In all other cases (when upgrading), only `execute*` statements are considered.

`executeResource*` methods load the file from the classpath. If the dialect supports multi-statements, the whole file is executed as one statement; otherwise, it is split using `Dialect.parseMultiStatements(...)`.

### Example: using DDL generated by the Maven plugin (Author/Book)

A typical setup using the **Author/Book** example and PostgreSQL might look like this:

1. Configure the Maven plugin to generate DDL:

   ```xml
   <plugin>
     <groupId>org.norm4j</groupId>
     <artifactId>norm4j-maven-plugin</artifactId>
     <version>1.1.15</version>
     <configuration>
       <packages>
         <package>com.mycompany.app.records</package>
       </packages>
       <dialects>
         <dialect>postgresql</dialect>
       </dialects>
       <version>v0.1</version>
       <outputDirectory>${project.basedir}/src/main/resources/db</outputDirectory>
     </configuration>
     <executions>
       <execution>
         <?m2e execute onConfiguration,onIncremental?>
         <id>generate-ddl</id>
         <phase>process-classes</phase>
         <goals>
           <goal>generate-ddl</goal>
         </goals>
       </execution>
     </executions>
   </plugin>
   ```

   This will produce:

   ```text
   src/main/resources/db/v0.1/postgresql/ddl.sql
   ```

2. Use `SchemaSynchronizer` at startup to apply the schema and seed initial data:

   ```java
   TableManager tableManager = new TableManager(getDataSource());

   new SchemaSynchronizer(tableManager)
           .version()
               .name("v0.1")
               .description("Initial author & book schema")

               // For a fresh database (no rows in schema_version yet):
               // use the full DDL generated by norm4j-maven-plugin.
               .executeResourceIfInitial("db/v0.1/postgresql/ddl.sql", PostgreSQLDialect.class)

               // For upgrades from earlier versions (if you later add v0.2, v0.3, etc),
               // you can add incremental 'execute(...)' calls here.
           .endVersion()
           .version()
               .name("v0.2")
               .description("Seed initial author & book data")

               // When this version is used as the first version on a brand-new DB:
               .executeIfInitial("insert into author (name) values ('Author A');")
               .executeIfInitial("insert into book (name, author_id) values ('Book A', 1);")

               // When upgrading from v0.1 to v0.2:
               .execute("insert into author (name) values ('Author A');")
               .execute("insert into book (name, author_id) values ('Book A', 1);")
           .endVersion()
       .apply();
   ```

In this example:

- On a **fresh database** (no rows in `schema_version`):
  - only the **last version** (`v0.2`) is applied,
  - only its `executeIfInitial*` statements are executed,
  - `v0.2` is recorded in `schema_version`.
- On an **existing database** where `v0.1` is already recorded:
  - `v0.2` is applied using its `execute(...)` statements,
  - its `executeIfInitial*` statements are ignored.

You can extend this pattern with more versions (`v0.3`, `v0.4`, …) and use the dialect-specific overloads when you need vendor-specific syntax.

---

## 🔨 Domain Object Mapping

### Basic DTO
```java
AuthorDTO authorDTO;

authorDTO = RecordMapper.from(Author.class, AuthorDTO.class).map(author);
```

### DTO with relations
**One to Many**

```java
RecordMapper<Author, AuthorDTO> authorMapper;
List<BookDTO> books;

authorMapper = RecordMapperBuilder.from(Author.class, AuthorDTO.class)
        .map(Author::getId).to(AuthorDTO::getId) // Automactivally mapped, but can be overriden
        .join(AuthorDTO::getBooks, Book.class, BookDTO.class)
        .endJoin()
    .build(tableManager);

authorDTO = authorMapper.map(author);

books = authorDTO.getBooks();
```

**Many to One**

```java
RecordMapper<Author, AuthorDTO> authorMapper;
AuthorDTO authorDTO;
BookDTO bookDTO;

bookMapper = RecordMapperBuilder.from(Book.class, BookDTO.class)
        .join(BookDTO::getAuthor, Author.class, AuthorDTO.class)
        .endJoin()
    .build(tableManager);

bookDTO = bookMapper.map(book);

authorDTO = bookDTO.getAuthor();
```

**Map a specific field of the related object**

```java
RecordMapper<Author, AuthorDTO> authorMapper;
List<UUID> bookIds;

authorMapper = RecordMapperBuilder.from(Author.class, AuthorDTO.class)
        .join(AuthorDTO::getBookIds, Book.class, UUID.class)
            .map(Book::getId).toObject()
    .build(tableManager);

authorDTO = authorMapper.map(author);

bookIds = authorDTO.getBookIds();
```

---

## ✨ Jakarta EE

### TableManagerFactory

```java
@ApplicationScoped
public class TableManagerFactory
{
    @Resource(name = "jdbc/norm_test")
    private DataSource dataSource;
    private TableManager tableManager;

    public TableManagerFactory()
    {
    }

    @PostConstruct
    public void initialize()
    {
        MetadataManager metadataManager;

        metadataManager = new MetadataManager();

        metadataManager.registerTable(Book.class);
        metadataManager.registerTable(Author.class);

        // or register all tables in a package
        // metadataManager.registerPackage("com.company.records");

        metadataManager.createTables(dataSource);

        tableManager = new TableManager(dataSource, metadataManager);
    }

    @Produces
    public TableManager getTableManager()
    {
        return tableManager;
    }
}

```

### @Inject TableManager

```java
@Stateless
public class AuthorService
{
    @Inject
    private TableManager tableManager;

    public AuthorService()
    {
    }

    public Author findById(int id)
    {
        return tableManager.find(Author.class, id);
    }
```
---

#### Using SchemaSynchronizer at startup (optional)

Instead of calling `createTables(...)` manually, you can let `SchemaSynchronizer` manage schema versions and DDL execution:

```java
@ApplicationScoped
public class TableManagerFactory {

    @Resource(name = "jdbc/norm_test")
    private DataSource dataSource;

    private TableManager tableManager;

    @PostConstruct
    public void initialize() {
        MetadataManager metadataManager;

        metadataManager = new MetadataManager();

        metadataManager.registerPackage("com.mycompany.app.records");
        
        tableManager = new TableManager(dataSource, metadataManager);

        new SchemaSynchronizer(tableManager)
                .version()
                    .name("v0.1")
                    .description("Initial schema")
                    .executeResourceIfInitial("db/v0.1/postgresql/ddl.sql", PostgreSQLDialect.class)
                .endVersion()
            .apply();
    }

    @Produces
    public TableManager getTableManager() {
        return tableManager;
    }
}
```

---

## 📚 Advanced Features

- **Schema Versioning & Migrations** with `SchemaSynchronizer`
- **Composite Primary Keys** via `@IdClass`
- **Join with Multiple Columns** using `@Join(columns = {...})`
- **Enumerated Fields** with `@Enumerated(EnumType.STRING|ORDINAL)`
- **Date/Time Mapping** via `@Temporal`
- **Array Fields** using `@Array(type = ArrayType.Vector/Array)`
- **Join without Referencial Integrity (No Foreign Key)** using `@Join(referencialIntegrity = false)`

---

## ✅ Supported ID Generation Strategies

- `AUTO`
- `IDENTITY`
- `SEQUENCE`
- `TABLE`
- `UUID`

---

## 📖 Articles

1. [Norm4j: A SQL-First, High-Performance Alternative to JPA](https://medium.com/@cedric.nanni/norm4j-a-sql-first-high-performance-alternative-to-jpa-4d96bdf8ecbd)
2. [Why Norm4j takes a Code-First Approach (vs. Database-First Tools Like jOOQ)](https://medium.com/@cedric.nanni/why-norm4j-takes-a-code-first-approach-vs-database-first-tools-like-jooq-9f0bc4344696)
3. [Norm4j: Simplifying Entity-to-DTO Mapping with a Powerful DSL](https://medium.com/@cedric.nanni/norm4j-simplifying-entity-to-dto-mapping-with-a-powerful-dsl-c1c478d45288)
4. [Schema Versioning in norm4j: Keeping Your SQL-First World in Sync](https://medium.com/@cedric.nanni/schema-versioning-in-norm4j-keeping-your-sql-first-world-in-sync-3098eda88572)

---

## 🧪 Running Tests

### Configure Test Database

Edit the file:
```
norm4j-test/src/test/resources/application-test.properties
```

#### PostgreSQL Example
```properties
datasource.driver=org.postgresql.Driver
datasource.url=jdbc:postgresql://localhost:5432/norm_test
datasource.username=test
datasource.password=password
```

### Create Test Schema (PostgreSQL)

```sql
CREATE USER test WITH PASSWORD 'password';

CREATE DATABASE norm_test;

CREATE EXTENSION vector;

ALTER DATABASE norm_test OWNER TO test;

CREATE SCHEMA test1;
CREATE SCHEMA test2;
CREATE SCHEMA test3;
CREATE SCHEMA test4;
CREATE SCHEMA test5;

GRANT ALL PRIVILEGES ON SCHEMA test1 TO test;
GRANT ALL PRIVILEGES ON SCHEMA test2 TO test;
GRANT ALL PRIVILEGES ON SCHEMA test3 TO test;
GRANT ALL PRIVILEGES ON SCHEMA test4 TO test;
GRANT ALL PRIVILEGES ON SCHEMA test5 TO test;

GRANT ALL PRIVILEGES ON SCHEMA public TO test;
```

#### MariaDB Example
```properties
datasource.driver=org.mariadb.jdbc.Driver
datasource.url=jdbc:mariadb://localhost:3306/norm_test
datasource.username=test
datasource.password=password
```

### Create Test Schema (MariaDB)

```sql
CREATE USER 'test'@'%' IDENTIFIED BY 'password';

CREATE DATABASE norm_test;

GRANT ALL PRIVILEGES ON norm_test.* TO 'test'@'%';

FLUSH PRIVILEGES;
```

#### SQL Server Example
```properties
datasource.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
datasource.url=jdbc:sqlserver://localhost;encrypt=false;database=norm_test;
datasource.username=test
datasource.password=password
```

### Create Test Schema (SQL Server)

```sql
CREATE LOGIN [test] WITH PASSWORD=N'password'

CREATE DATABASE norm_test

CREATE SCHEMA test1 AUTHORIZATION test
CREATE SCHEMA test2 AUTHORIZATION test
CREATE SCHEMA test3 AUTHORIZATION test
CREATE SCHEMA test4 AUTHORIZATION test
CREATE SCHEMA test5 AUTHORIZATION test
```

### Run Tests

```bash
cd norm4j-test
mvn clean test
```
---

## 💬 Need Help?

norm4j is actively looking for feedback and contributors!  
If you test with Oracle or other platforms, please share your experience!
