# Caching

DBFlow provides powerful caching mechanisms to speed up retrieval from the database
to enable high performance in our applications.

Caching is not enabled by default, but it is very easy to enable.

**Note**: caching functions correctly in only when we do CRUD operations on full model queries (i.e. Single table, no projections).

Caching should be used when:
  1. Loading the same set of `Model` many times with slightly different selection.
  2. Operating on full `Model` objects (containing all `@PrimaryKey`)

Caching should be avoided (or clear caches) when:
  1. Performing selection, complex queries, anything different than starting with `SQLite.select()`
  2. Using the wrapper modifications such as `Insert`, `Update`, or `Delete`. In this case you should clear the associated cache, post-operation.

## Supported Caching Classes

Caching is supported for:
  1. `SparseArray` via `SparseArrayBasedCache` (platform SparseArray)
  2. `Map` via `SimpleMapCache`
  3. `LruCache` via `ModelLruCache` (copy of `LruCache`, so dependency avoided)
  4. Custom Caching classes that implement `ModelCache`

Cache sizes are not supported for `SimpleMapCache`. This is because `Map` can hold
arbitrary size of contents.

## Enable Caching

To enable caching on a single-primary key table, simply specify that it is enabled:

```java


@Table(database = AppDatabase.class, cachingEnabled = true)
public class CacheableModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;
}

```

or in Kotlin:

```kotlin

@Table(database = AppDatabase.class, cachingEnabled = true)
class CacheableModel {

    @PrimaryKey(autoincrement = true)
    var id: Long = 0L

    @Column
    var name: String? = null;
}

```

to use caching on a table that uses multiple primary keys, [see](Caching.md#multiple-primary-key-caching).

By default we use a `SimpleMapCache`, which loads `Model` into a `Map`. The key is
either the primary key of the object or a combination of the two, but it should have
an associated `HashCode` and `equals()` value.

## Loading from the DB

When retrieving from the database, we still run a full query that returns a `Cursor`.
We skirt the expensive conversion process by checking the combination of Primary key on each row.
If an item exists with the same primary key combination, we return that object out of the cache.

If you operate on a model object, that change gets reflected in the cache. But beware
modifying them in a separate thread might result in state state returned if the cache is not synchronized
properly.

Any time a field on these objects are modified, you _should_ immediately save those
since we have a direct reference to the object from the cache. Otherwise, the DB
and cache could get into an inconsistent state.

```java

MyModel model = SQLite.select().from(MyModel.class).where(...).querySingle();
model.setName("Name");
model.save(); // save it to DB post any modifications to this object.

```

```kotlin

(select from MyModel::class where (...)).result?.let { result ->
  result.name = "Name"
  result.save()
}

```
## Disable Caching For Some Queries

To disable caching on certain queries as you might want to project on only a few columns,
rather than the full dataset. Just call `disableCaching()`:

```java

select(My_Table.column, My_Table.column2)
  .from(My.class)
  .disableCaching()
  .queryList();

```

```kotlin

select(My_Table.column, My_Table.column2)
  .from(My::class)
  .disableCaching()
  .list;

```
## Advanced

### Specifying cache Size

To specify cache size, set `@Table(cacheSize = {size})`. Please note that not all
caches support sizing. It's up to each cache.  

### Custom Caches

To specify a custom cache for a table, please define a `public static final` field:

```java
@ModelCacheField
public static ModelCache<CacheableModel3, ?> modelCache = new SimpleMapCache<>(); // replace with any cache you want.
```

```kotlin
companion object {

  @JvmField @ModelCacheField
  val modelCache = SimpleMapCache<CacheableModel3, Any>()
}
```
### Multiple Primary Key Caching

This allows for tables that have multiple primary keys be used in caching. To use,
add a `@MultiCacheField` `public static final` field.
for example we have a `Coordinate` class:


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

In this case we use the `IMultiKeyCacheConverter` class, which specifies a key type
that the object returns. The `getCachingKey` method returns an ordered set of `@PrimaryKey`
columns in declaration order. Also the value that is returned should have an `equals()` or `hashcode()` specified
especially when used in the `SimpleMapCache`.
