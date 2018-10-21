# Caching

DBFlow provides powerful caching mechanisms to speed up retrieval from the database to enable high performance in our applications.

Caching is not enabled by default, but it is very easy to enable.

## When to Use Caching

1. retrieve the near-same list of objects from the DB when data does not change frequently.
2. Need to load large, full objects from the DB repeatedly in different places in the app.

## When Not To Use Caching

Do not use caching when: 1. you need a subset of columns from the DB. i.e. \(`select(name, age, firstName)`\) 2. The data is expected to change frequently as any operation on the DB we do that is not tied to model instances will invalidate our cache anyways. 3. You load data from `@OneToMany`, or have nested `@OneToMany` fields within inner objects.

**Note**: DBFlow is fast and efficient. Caching may not be required at all, except in very particular use-cases. Do not abuse. You can call `disableCaching()` on a query to ensure it's a fresh dataset.

## How Caching Works

Caching under the hood is done by storing an instance of each `Model` returned from a query on a specific table into memory. 1. Developer enables caching on Table A 2. Query from Table A 3. When receiving the `Cursor`, we read the primary key values from it and look them up from `ModelCache`. If the `Model` exists, return it from cache; otherwise create new instance, read in values, and store in cache. 4. That instance remains in memory such on next query, we return that instance instead of recreating one from a `Cursor`. 5. When we call `ModelAdapter.save()`, `insert()`, `update()`, or `delete()`, we update model in the cache so that on next retrieval, the model with proper values is returned. 6. When wrapper operations are performed on tables with caching, caches are not modified. When doing such a call, please call `TableA_Table.cacheAdapter.clearCache()`

### Supported Backing Objects

Caching is supported under the hood for: 1. `SparseArray` via `SparseArrayBasedCache` \(platform SparseArray\) 2. `Map` via `SimpleMapCache` 3. `LruCache` via `ModelLruCache` \(copy of `LruCache`, so dependency avoided\) 4. Custom Caching classes that implement `ModelCache`

Cache sizes are not supported for `SimpleMapCache`. This is because `Map` can hold arbitrary size of contents.

### Enable Caching

To enable caching on a single-primary key table, simply specify that it is enabled:

```kotlin
@Table(database = AppDatabase.class, cachingEnabled = true)
class CacheableModel {

    @PrimaryKey(autoincrement = true)
    var id: Long = 0L

    @Column
    var name: String? = null
}
```

or in Java:

```java
@Table(database = AppDatabase.class, cachingEnabled = true)
public class CacheableModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;
}
```

to use caching on a table that uses multiple primary keys, [see](caching.md#multiple-primary-key-caching).

By default we use a `SimpleMapCache`, which loads `Model` into a `Map`. The key is either the primary key of the object or a combination of the two, but it should have an associated `HashCode` and `equals()` value.

## Modifying Cache Objects

Any time a field on these objects are modified, you _should_ immediately save those since we have a direct reference to the object from the cache. Otherwise, the DB and cache could get into an inconsistent state.

```kotlin
database<AppDatabase> {
  (select from MyModel::class where (...)).result?.let { result ->
    result.name = "Name"
    result.save()
  }
}
```

```java
MyModel model = SQLite.select(db).from(MyModel.class).where(...).querySingle();
model.setName("Name");
model.save(); // save it to DB post any modifications to this object.
```

## Disable Caching For Some Queries

To disable caching on certain queries as you might want to project on only a few columns, rather than the full dataset. Just call `disableCaching()`:

```kotlin
database<AppDatabase> {
  select(My_Table.column, My_Table.column2)
    .from(My::class)
    .disableCaching()
    .list
}
```

or in Java:

```java
select(db, My_Table.column, My_Table.column2)
  .from(My.class)
  .disableCaching()
  .queryList();
```

## Advanced

### Specifying cache Size

To specify cache size, set `@Table(cacheSize = {size})`. Please note that not all caches support sizing. It's up to each cache.

### Custom Caches

To specify a custom cache for a table, please define a `@JvmField` field:

```kotlin
companion object {

  @JvmField @ModelCacheField
  val modelCache = SimpleMapCache<CacheableModel3, Any>()
}
```

or in Java using `public static final`:

```java
@ModelCacheField
public static ModelCache<CacheableModel3, ?> modelCache = new SimpleMapCache<>(); // replace with any cache you want.
```

### Multiple Primary Key Caching

This allows for tables that have multiple primary keys be used in caching. To use, add a `@MultiCacheField` `@JvmField` field. for example we have a `Coordinate` class:

```kotlin
@Table(database = AppDatabase.class, cachingEnabled = true)
class Coordinate(@PrimaryKey latitude: Double = 0.0,
                 @PrimaryKey longitude: Double = 0.0) {

    companion object {
      @JvmField
      @MultiCacheField
      val cacheConverter = IMultiKeyCacheConverter { values -> "${values[0]},${values[1]}" }
    }
}
```

or in Java:

```java
@Table(database = AppDatabase.class, cachingEnabled = true)
public class Coordinate {

    @MultiCacheField
    public static final IMultiKeyCacheConverter<String> multiKeyCacheModel = new IMultiKeyCacheConverter<String>() {

        @Override
        @NonNull
        public String getCachingKey(@NonNull Object[] values) {
            return "(" + values[0] + "," + values[1] + ")";
        }
    };

    @PrimaryKey
    double latitude;

    @PrimaryKey
    double longitude;
```

In this case we use the `IMultiKeyCacheConverter` class, which specifies a key type that the object returns. The `getCachingKey` method returns an ordered set of `@PrimaryKey` columns in declaration order. Also the value that is returned should have an `equals()` or `hashcode()` specified \(use a `data class`\) especially when used in the `SimpleMapCache`.

