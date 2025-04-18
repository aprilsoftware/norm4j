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
    <version>1.1.8</version>
</dependency>
```

#### Supported Databases

**PostgreSQL**
```xml
<dependency>
    <groupId>org.norm4j</groupId>
    <artifactId>norm4j-postgresql</artifactId>
    <version>1.1.8</version>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.5</version>
</dependency>
```

**MariaDB**
```xml
<dependency>
    <groupId>org.norm4j</groupId>
    <artifactId>norm4j-mariadb</artifactId>
    <version>1.1.8</version>
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
    <version>1.1.8</version>
</dependency>
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.10.0.jre11</version>
</dependency>
```

**Oracle**
```xml
<dependency>
    <groupId>org.norm4j</groupId>
    <artifactId>norm4j-oracle</artifactId>
    <version>1.1.8</version>
</dependency>
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

## 📚 Advanced Features

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
