# norm4j usage reference

## Dependencies

Use:

- `org.norm4j:norm4j-core`
- one dialect module:
  - `org.norm4j:norm4j-postgresql`
  - `org.norm4j:norm4j-mariadb`
  - `org.norm4j:norm4j-sqlserver`
  - `org.norm4j:norm4j-oracle`

## Entity modeling

Core annotations:

- `@Table(name = "...")`
- `@Id`
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- `@Column(...)`
- `@Join(..., reference = @Reference(...))`

## Bootstrap

```java
MetadataManager metadataManager = new MetadataManager();
metadataManager.registerTable(Book.class);
metadataManager.registerTable(Author.class);
metadataManager.createTables(getDataSource());

TableManager tableManager = new TableManager(getDataSource(), metadataManager);
```

Alternative registration:

```java
metadataManager.registerPackage("com.mycompany.records");
```

## CRUD

- `tableManager.persist(...)`
- `tableManager.find(...)`
- `tableManager.merge(...)`
- `tableManager.remove(...)`

## Relationship loading

- `tableManager.joinOne(book, Author.class)`
- `tableManager.joinMany(author, Book.class)`
- `tableManager.joinMany(author, Author::getId, Book.class, Book::getAuthorId)`

## Querying

- Select builder with `select`, `from`, joins, `where/and/or`, `orderBy`.
- Update builder with `update(...).set(...).where(...).executeUpdate()`.
- Delete builder with `from(...).where(...).executeUpdate()`.
- Native query with `createQuery("...")`, `setParameter`, `getResultList(...)`.

## Mapping

- `RecordMapper.from(...).map(...)`
- `RecordMapperBuilder.from(...).map(...).join(...).endJoin().build(tableManager)`

## Advanced features

- `SchemaSynchronizer`
- `@IdClass`
- multi-column joins via `@Join(columns={...})`
- `@Enumerated(EnumType.STRING|ORDINAL)`
- `@Temporal`
- `@Array(type = ArrayType.Vector|Array)`
- `@Join(referencialIntegrity = false)`

## Supported ID strategies

- `AUTO`
- `IDENTITY`
- `SEQUENCE`
- `TABLE`
- `UUID`
