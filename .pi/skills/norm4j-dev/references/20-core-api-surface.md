# Core API surface

## `TableManager`

Primary methods:

- `persist(Object record)`
- `merge(T record)`
- `remove(Object record)`
- `remove(Class<?> tableClass, Object primaryKey)`
- `find(Class<T> tableClass, Object primaryKey)`
- `joinOne(...)` overloads
- `joinMany(...)` overloads
- `mapMany(List<R> records, Class<T> rightTableClass)`
- `execute(String sql)`
- `createQuery(String query)`
- `createSelectQueryBuilder()`
- `createUpdateQueryBuilder()`
- `createDeleteQueryBuilder()`

## `MetadataManager`

Key behavior:

- `registerTable(Class<?>)`
- `registerPackage(String packageName)` using classpath scanning
- validates `@Table` presence on registration
- initializes SQL dialect from JDBC connection with `initDialect(...)`

## Query builders

### `SelectQueryBuilder`

Supports:

- aggregates (`count`, `sum`, `avg`, `min`, `max`)
- `select`, `from`
- `innerJoin`, `leftJoin`, `rightJoin`
- `where`, `and`, `or`
- `groupBy`, `orderBy`, `orderByDesc`
- `offset`, `limit`
- result accessors (`getResultList`, `getSingleResult`, map helpers)

### `UpdateQueryBuilder`

- `update(...)`
- `set(...)`
- conditions
- `executeUpdate()`

### `DeleteQueryBuilder`

- `from(...)`
- conditions
- `executeUpdate()`

## Condition model

`QueryBuilder` + `ConditionBuilder` support:

- field-getter-based conditions
- expression/raw-string conditions
- subquery-based conditions
- grouped/lambda conditions like:

```java
.where(q -> q.condition(...).or(...))
```

## Mapping DSL (`RecordMapperBuilder`)

- `from(sourceClass, targetClass)`
- `map(...).to(...)`
- `join(targetGetter, sourceClass, targetClass)`
- nested relation mapping with `.map(...).toObject()` or `.to(...)`
- `.endJoin()`
- `.build()` or `.build(tableManager)`
