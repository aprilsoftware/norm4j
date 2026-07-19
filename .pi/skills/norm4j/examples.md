# norm4j Examples

This document provides practical examples of using norm4j in various scenarios.

## Basic CRUD Operations

### Creating and Persisting Entities

```java
// Define an entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    // Getters and setters
}

// Usage
MetadataManager metadataManager = new MetadataManager();
metadataManager.registerTable(Customer.class);
metadataManager.createTables(dataSource);

TableManager tableManager = new TableManager(dataSource, metadataManager);

// Create and persist
Customer customer = new Customer();
customer.setName("John Doe");
customer.setEmail("john@example.com");

tableManager.persist(customer);
```

### Finding Entities

```java
// Find by primary key
Customer foundCustomer = tableManager.find(Customer.class, customer.getId());

// Find with query
List<Customer> customers = tableManager.createSelectQueryBuilder()
    .select(Customer.class)
    .from(Customer.class)
    .where(Customer::getName, "=", "John Doe")
    .getResultList(Customer.class);
```

### Updating Entities

```java
// Update using merge
foundCustomer.setEmail("john.doe@example.com");
tableManager.merge(foundCustomer);

// Update using query builder
int updatedCount = tableManager.createUpdateQueryBuilder()
    .update(Customer.class)
    .set(Customer::getEmail, "john.doe@example.com")
    .where(Customer::getId, "=", customer.getId())
    .executeUpdate();
```

### Deleting Entities

```java
// Delete entity
TableManager.remove(customer);

// Delete by primary key
tableManager.remove(Customer.class, customer.getId());

// Delete using query builder
int deletedCount = tableManager.createDeleteQueryBuilder()
    .from(Customer.class)
    .where(Customer::getName, "=", "John Doe")
    .executeUpdate();
```

## Relationships

### One-to-Many Relationship

```java
// Define entities
@Table(name = "author")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;
}

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
    private String title;

    @Column(name = "author_id")
    private int authorId;
}

// Usage
Author author = new Author();
author.setName("J.K. Rowling");
tableManager.persist(author);

Book book1 = new Book();
book1.setTitle("Harry Potter 1");
book1.setAuthorId(author.getId());
tableManager.persist(book1);

Book book2 = new Book();
book2.setTitle("Harry Potter 2");
book2.setAuthorId(author.getId());
tableManager.persist(book2);

// Join relationships
List<Book> books = tableManager.joinMany(author, Book.class);
Book firstBook = tableManager.joinOne(author, Book.class);
```

### Many-to-One Relationship

```java
// Using field getters for more control
List<Book> books = tableManager.joinMany(
    author, 
    Author::getId, 
    Book.class, 
    Book::getAuthorId
);

Book book = tableManager.joinOne(
    author, 
    Author::getId, 
    Book.class, 
    Book::getAuthorId
);
```

## Query Building

### Simple Queries

```java
// Basic select
List<Customer> customers = tableManager.createSelectQueryBuilder()
    .select(Customer.class)
    .from(Customer.class)
    .getResultList(Customer.class);

// With where clause
List<Customer> activeCustomers = tableManager.createSelectQueryBuilder()
    .select(Customer.class)
    .from(Customer.class)
    .where(Customer::getActive, "=", true)
    .getResultList(Customer.class);
```

### Complex Queries

```java
// Multiple conditions
List<Customer> customers = tableManager.createSelectQueryBuilder()
    .select(Customer.class)
    .from(Customer.class)
    .where(Customer::getActive, "=", true)
    .and(Customer::getCreatedAt, ">=", startDate)
    .and(Customer::getCreatedAt, "<=", endDate)
    .orderBy(Customer::getName)
    .getResultList(Customer.class);

// OR conditions
List<Customer> customers = tableManager.createSelectQueryBuilder()
    .select(Customer.class)
    .from(Customer.class)
    .where(q -> q.condition(Customer::getName, "=", "John")
            .or(Customer::getName, "=", "Jane"))
    .getResultList(Customer.class);
```

### Joins

```java
// Inner join
List<Book> books = tableManager.createSelectQueryBuilder()
    .select(Book.class)
    .from(Book.class)
    .innerJoin(Author.class)
    .where(Book::getAuthorId, "=", author.getId())
    .getResultList(Book.class);

// Left join
List<Book> allBooks = tableManager.createSelectQueryBuilder()
    .select(Book.class)
    .from(Book.class)
    .leftJoin(Author.class)
    .getResultList(Book.class);
```

### Aggregation

```java
// Count
Long count = tableManager.createSelectQueryBuilder()
    .selectCount()
    .from(Customer.class)
    .where(Customer::getActive, "=", true)
    .getSingleResult(Long.class);

// Max
Integer maxId = tableManager.createSelectQueryBuilder()
    .selectMax(Customer::getId)
    .from(Customer.class)
    .getSingleResult(Integer.class);
```

## Schema Management

### Basic Schema Creation

```java
// Create tables from entities
MetadataManager metadataManager = new MetadataManager();
metadataManager.registerPackage("com.example.entities");
metadataManager.createTables(dataSource);
```

### Schema Versioning

```java
// Simple versioning
new SchemaSynchronizer(tableManager)
    .version("v0.1")
        .description("Initial schema")
        .schema()
            .enableAutoCreation(true)
        .endSchema()
    .endVersion()
    .apply();

// Multiple versions
new SchemaSynchronizer(tableManager)
    .version("v0.1")
        .description("Initial schema")
        .schema()
            .enableAutoCreation(true)
        .endSchema()
    .endVersion()
    
    .version("v0.2")
        .description("Add email column")
        .execute("ALTER TABLE customer ADD COLUMN email VARCHAR(255)")
    .endVersion()
    
    .version("v0.3")
        .description("Add index on email")
        .execute("CREATE INDEX idx_customer_email ON customer(email)")
    .endVersion()
    
    .apply();
```

### Dialect-Specific Migrations

```java
new SchemaSynchronizer(tableManager)
    .version("v1.0")
        .description("Database-specific features")
        .execute("CREATE EXTENSION IF NOT EXISTS pgcrypto", PostgreSQLDialect.class)
        .execute("CREATE EXTENSION IF NOT EXISTS vector", PostgreSQLDialect.class)
        .execute("ALTER TABLE customer ADD COLUMN password_hash VARCHAR(255)")
    .endVersion()
    .apply();
```

## DTO Mapping

### Basic DTO Mapping

```java
// Define DTO
public class CustomerDTO {
    private int id;
    private String name;
    private String email;
    private List<OrderDTO> orders;
    
    // Getters and setters
}

// Create mapper
RecordMapper<Customer, CustomerDTO> mapper = RecordMapperBuilder.from(Customer.class, CustomerDTO.class)
    .build(tableManager);

// Use mapper
CustomerDTO dto = mapper.map(customer);
```

### DTO with Relationships

```java
// Define DTOs
public class CustomerDTO {
    private int id;
    private String name;
    private List<OrderDTO> orders;
}

public class OrderDTO {
    private int id;
    private String number;
    private BigDecimal total;
}

// Create mapper with relationships
RecordMapper<Customer, CustomerDTO> mapper = RecordMapperBuilder.from(Customer.class, CustomerDTO.class)
    .join(CustomerDTO::getOrders, Order.class, OrderDTO.class)
    .endJoin()
    .build(tableManager);

// Use mapper
CustomerDTO customerDTO = mapper.map(customer);
List<OrderDTO> orders = customerDTO.getOrders();
```

### Custom Field Mapping

```java
// Map specific fields
RecordMapper<Customer, CustomerDTO> mapper = RecordMapperBuilder.from(Customer.class, CustomerDTO.class)
    .map(Customer::getId).to(CustomerDTO::setId)
    .map(Customer::getName).to(CustomerDTO::setName)
    .map(Customer::getEmail).to(CustomerDTO::setEmail)
    .build(tableManager);

// Map to specific field of related object
RecordMapper<Author, AuthorDTO> authorMapper = RecordMapperBuilder.from(Author.class, AuthorDTO.class)
    .join(AuthorDTO::getBookIds, Book.class, UUID.class)
        .map(Book::getId).toObject()
    .build(tableManager);

AuthorDTO authorDTO = authorMapper.map(author);
List<UUID> bookIds = authorDTO.getBookIds();
```

## Advanced Features

### Composite Primary Keys

```java
// Define composite key class
public class CustomerId implements Serializable {
    private String region;
    private String customerNumber;
}

// Define entity with composite key
@IdClass(CustomerId.class)
@Table(name = "customer")
public class Customer {
    @Id
    private String region;
    
    @Id
    private String customerNumber;
    
    @Column(nullable = false)
    private String name;
}

// Usage
CustomerId customerId = new CustomerId();
customerId.setRegion("US");
customerId.setCustomerNumber("12345");

Customer customer = tableManager.find(Customer.class, customerId);
```

### Array Fields

```java
// Define entity with array field
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "tags")
    @Array(type = ArrayType.Array)
    private String[] tags;
}

// Usage
Product product = new Product();
product.setName("Laptop");
product.setTags(new String[]{"electronics", "computer", "laptop"});

tableManager.persist(product);

// Query with array
List<Product> electronics = tableManager.createSelectQueryBuilder()
    .select(Product.class)
    .from(Product.class)
    .where(Product::getTags, "@>", new String[]{"electronics"})
    .getResultList(Product.class);
```

### Enumerated Fields

```java
// Define entity with enum
public enum CustomerStatus {
    ACTIVE, INACTIVE, PENDING
}

@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CustomerStatus status;
}

// Usage
Customer customer = new Customer();
customer.setName("John Doe");
customer.setStatus(CustomerStatus.ACTIVE);

tableManager.persist(customer);

// Query with enum
List<Customer> activeCustomers = tableManager.createSelectQueryBuilder()
    .select(Customer.class)
    .from(Customer.class)
    .where(Customer::getStatus, "=", CustomerStatus.ACTIVE)
    .getResultList(Customer.class);
```

### Date/Time Fields

```java
// Define entity with temporal fields
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    
    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
}

// Usage
Event event = new Event();
event.setName("Conference");
event.setStartTime(new Date());
event.setEndTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));

tableManager.persist(event);

// Query with date range
List<Event> upcomingEvents = tableManager.createSelectQueryBuilder()
    .select(Event.class)
    .from(Event.class)
    .where(Event::getEndTime, ">=", new Date())
    .orderBy(Event::getStartTime)
    .getResultList(Event.class);
```

## Jakarta EE Integration

### CDI Injection

```java
// Define TableManager producer
@ApplicationScoped
public class TableManagerFactory {
    @Resource(name = "jdbc/norm_test")
    private DataSource dataSource;
    private TableManager tableManager;

    @PostConstruct
    public void initialize() {
        MetadataManager metadataManager = new MetadataManager();
        metadataManager.registerPackage("com.example.entities");
        metadataManager.createTables(dataSource);
        
        tableManager = new TableManager(dataSource, metadataManager);
    }

    @Produces
    public TableManager getTableManager() {
        return tableManager;
    }
}

// Use in service
@Stateless
public class CustomerService {
    @Inject
    private TableManager tableManager;

    public Customer findById(int id) {
        return tableManager.find(Customer.class, id);
    }
}
```

### Transaction Management

```java
@Stateless
public class CustomerService {
    @Inject
    private TableManager tableManager;

    @Transactional
    public Customer createCustomer(Customer customer) {
        // Multiple operations in a single transaction
        tableManager.persist(customer);
        
        // Create related entities
        Address address = new Address();
        address.setCustomerId(customer.getId());
        address.setStreet("123 Main St");
        tableManager.persist(address);
        
        return customer;
    }
}
```

## Testing

### Basic Test Setup

```java
public class CustomerTest extends BaseTest {
    @Test
    public void testCustomerCRUD() {
        // Setup
        dropTable("test", "customer");
        
        MetadataManager metadataManager = new MetadataManager();
        metadataManager.registerTable(Customer.class);
        metadataManager.createTables(getDataSource());
        
        TableManager tableManager = new TableManager(getDataSource(), metadataManager);
        
        // Test
        Customer customer = new Customer();
        customer.setName("Test Customer");
        customer.setEmail("test@example.com");
        
        tableManager.persist(customer);
        
        Customer found = tableManager.find(Customer.class, customer.getId());
        assertNotNull(found);
        assertEquals("Test Customer", found.getName());
        
        // Cleanup
        dropTable("test", "customer");
    }
}
```

### Integration Testing

```java
@BeforeAll
public static void setup() {
    // Initialize schema
    MetadataManager metadataManager = new MetadataManager();
    metadataManager.registerPackage("com.example.entities");
    metadataManager.createTables(dataSource);
    
    // Apply migrations
    new SchemaSynchronizer(new TableManager(dataSource, metadataManager))
        .version("v0.1")
            .description("Initial schema")
            .schema()
                .enableAutoCreation(true)
            .endSchema()
        .endVersion()
        .apply();
}

@AfterAll
public static void cleanup() {
    // Drop all tables
    // (Implementation depends on your test setup)
}
```

## Performance Considerations

### Batch Operations

```java
// Batch insert
List<Customer> customers = getCustomersToInsert();
for (Customer customer : customers) {
    tableManager.persist(customer);
}

// Batch update
List<Customer> customers = tableManager.createSelectQueryBuilder()
    .select(Customer.class)
    .from(Customer.class)
    .where(Customer::getActive, "=", false)
    .getResultList(Customer.class);

for (Customer customer : customers) {
    customer.setActive(true);
    tableManager.merge(customer);
}
```

### Selective Loading

```java
// Load only specific fields
List<Customer> customers = tableManager.createSelectQueryBuilder()
    .select(Customer::getId, Customer::getName)
    .from(Customer.class)
    .getResultList(Customer.class);

// Avoid N+1 problem by using joins
Author author = tableManager.find(Author.class, authorId);
List<Book> books = tableManager.joinMany(author, Book.class);

// Instead of:
// for (Book book : books) {
//     Author bookAuthor = tableManager.find(Author.class, book.getAuthorId());
// }
```

### Connection Pooling

```java
// Configure connection pooling (example with HikariCP)
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
config.setUsername("user");
config.setPassword("password");
config.setMaximumPoolSize(10);
config.setConnectionTimeout(30000);
config.setIdleTimeout(600000);
config.setMaxLifetime(1800000);

HikariDataSource dataSource = new HikariDataSource(config);

// Use with norm4j
TableManager tableManager = new TableManager(dataSource, metadataManager);
```

## Best Practices

### Entity Design

1. **Keep entities simple**: Avoid business logic in entities
2. **Use appropriate ID generation**: Choose strategy based on database
3. **Add proper constraints**: Use @Column nullable/unique as needed
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

### Common Issues and Solutions

**Issue**: "No metadata found for class"
- **Solution**: Ensure the class is registered with MetadataManager
- **Check**: `metadataManager.registerTable(MyClass.class)`

**Issue**: "No primary key found"
- **Solution**: Add @Id annotation to primary key field
- **Check**: `@Id private int id;`

**Issue**: "Join not found"
- **Solution**: Ensure @Join annotation is properly configured
- **Check**: `@Join(columns = "author_id", reference = @Reference(table = Author.class, columns = "id"))`

**Issue**: SQL syntax errors
- **Solution**: Check database dialect compatibility
- **Check**: Use correct dialect in SchemaSynchronizer

**Issue**: Connection leaks
- **Solution**: Use connection pooling and proper resource management
- **Check**: Ensure connections are properly closed

**Issue**: Performance problems
- **Solution**: Analyze queries with EXPLAIN, add indexes
- **Check**: Review query patterns and database schema

## Migration from JPA

### Step-by-Step Migration

1. **Replace dependencies**: Replace JPA with norm4j
2. **Update annotations**: Replace JPA annotations with norm4j equivalents
3. **Replace EntityManager**: Replace with TableManager
4. **Update queries**: Replace JPQL with norm4j query builder
5. **Add explicit joins**: Replace automatic loading with explicit joins
6. **Configure schema**: Set up schema versioning
7. **Test thoroughly**: Test all database operations

### Annotation Mapping

| JPA Annotation | norm4j Equivalent |
|----------------|-------------------|
| `@Entity` | `@Table` |
| `@Id` | `@Id` |
| `@GeneratedValue` | `@GeneratedValue` |
| `@Column` | `@Column` |
| `@OneToMany` | `@Join` + `@Reference` |
| `@ManyToOne` | `@Join` + `@Reference` |
| `@JoinColumn` | Part of `@Join` |
| `@Enumerated` | `@Enumerated` |
| `@Temporal` | `@Temporal` |

### Query Migration

**JPA JPQL**:
```java
List<Customer> customers = entityManager.createQuery(
    "SELECT c FROM Customer c WHERE c.active = true", 
    Customer.class
).getResultList();
```

**norm4j**:
```java
List<Customer> customers = tableManager.createSelectQueryBuilder()
    .select(Customer.class)
    .from(Customer.class)
    .where(Customer::getActive, "=", true)
    .getResultList(Customer.class);
```

**JPA Criteria API**:
```java
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<Customer> query = cb.createQuery(Customer.class);
Root<Customer> c = query.from(Customer.class);
query.select(c).where(cb.equal(c.get("active"), true));
List<Customer> customers = entityManager.createQuery(query).getResultList();
```

**norm4j**:
```java
List<Customer> customers = tableManager.createSelectQueryBuilder()
    .select(Customer.class)
    .from(Customer.class)
    .where(Customer::getActive, "=", true)
    .getResultList(Customer.class);
```
