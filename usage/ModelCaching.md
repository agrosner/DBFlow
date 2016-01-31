# Powerful Model Caching
Model caching in this library is very simple and is extremely extensible, accessible, and usable.

A `ModelCache` is an interface to an actual cache that is used within a SQLite query, `FlowQueryList`, `FlowCursorList`, or can use it anywhere you wish.

## Enable Caching In Tables
To enabled caching, simply add `cachingEnabled = true` to your `@Table` annotation. To enable caching on classes with multiple `@PrimaryKey` columns, you _must_ define a `@MultiCacheField` object (explained below).

When a query runs on the DB, it will store the instance of the `Model` in the cache, and the cache is solely responsible for managing memory efficiently.

There are a few kinds of caches supported out the box:
  1. `ModelLruCache` -> using an LruCache, this cache will evict members as it adds items that go past a predefined limit.
  2. `SimpleMapCache` -> simply stores models in a `Map` (default is `HashMap`) with a predefined capacity on the `Map`.
  3. `SparseArrayBasedCache` -> an int->object key/value based object that uses a `SparseArray` underneath. It works with any _Number_ descendant or primitive counterpart (Integer, Double, Long, etc.), or `MulitCacheConverter` that returns the same key type.

**Note** if you run a `SELECT` with columns specified, it may cache partial `Model` classes if they're loaded before the full counterparts. It is highly recommended to just load the full models in this case, since the caching mechanism will make up most efficiency problems.

The default cache is a `SimpleMapCache`. You can specify `cacheSize()` in the `@Table`  to tell the default cache the size of its contents. Once specifying a custom cache, this parameter becomes invalid.

To use a custom cache, specify the cache in your `Model` class:

```java
@ModelCacheField
public static ModelCache<CacheableModel3, ?> modelCache = new SimpleMapCache<>();
```

The `@ModelCacheField` must be `public static`.

As of 3.0, DBFlow now will smartly reload `@ForeignKey` relationships when loading from the cache.

### How Caching Actually works
  1.Each `@Table`/`Model` class has its own cache, they are not shared between tables or subclasses.
  2. It "intercepts" the query running and references the cache (explained next).
  3. Using any wrapper `Insert`, `Update`, or `Delete` SQLite-builder method are **strongly discouraged** when caching a `Model` as the cache will not update (due to efficiency and performance reasons). If you need to run these queries, a simple `FlowManager.getModelAdapter(MyTable.class).getModelCache().clear()` after running the query will invalidate the cache and it will update its information going forward.
  4. Modifying objects from the cache follows Java-reference rules: changing field values in one thread may result in an inconsistent state on in your application that may be loading from the DB until you `save()`, `insert()` or `update()`. Changing any fields are directly modifying objects from the cache (when loaded directly from it), so take note.

When running a query via the wrapper language, DBFlow will:
  1. Run the query, resulting in a `Cursor`
  2. Retrieve the primary key column values from the `Cursor`
  3. If the combo from the keys is in the cache, we:
    1. Reload relationships such as `@ForeignKey` (if any exist). _TIP:_ Make this faster by enabling caching on this object's table too.
    2. Then return the cached object.

  4. If the object does not exist, we load the full object from the DB, and subsequent queries to the same object will return the object from the cache (until it's evicted or cleared from the cache.)

### Multiple Primary key caching
As of 3.0, DBFlow supports multiple primary keys in a cache. It is _required_ that any `Model` with more than one primary key define a `@MultiCacheField`:

```java
@Table(database = TestDatabase.class, cachingEnabled = true)
public class MultipleCacheableModel extends BaseModel {

    @MultiCacheField
    public static IMultiKeyCacheConverter<String> multiKeyCacheModel = new IMultiKeyCacheConverter<String>() {

        @Override
        @NonNull
        public String getCachingKey(@NonNull Object[] values) { // in order of the primary keys defined
            return "(" + values[0] + "," + values[1] + ")";
        }
    };

    @PrimaryKey
    double latitude;

    @PrimaryKey
    double longitude;

    @ForeignKey(references = {@ForeignKeyReference(columnName = "associatedModel",
            columnType = String.class, foreignKeyColumnName = "name", referencedFieldIsPackagePrivate = true)})
    TestModel1 associatedModel;

}
```

The return type can be anything, as long as the `ModelCache` you define for the class supports the return type as a key.

### FlowCursorList + FlowQueryList
`FlowCursorList` and `FlowQueryList` utilize a separate `ModelCache` from the caches associated with `@Table`/`Model` classes. To override the default caching mechanism:

```java

@Override
protected ModelCache<? extends BaseCacheableModel, ?> getBackingCache() {
        return new MyCustomCache<>();
}
```

### Custom Caches
You can create your own cache and use it wherever you want.

An example cache is using a copied `LruCache` from the support library:

```java

public class ModelLruCache<ModelClass extends Model> extends ModelCache<ModelClass, LruCache<Long, ModelClass>>{

    public ModelLruCache(int size) {
        super(new LruCache<Long, ModelClass>(size));
    }

    @Override
    public void addModel(Object id, ModelClass model) {
        if(id instanceof Number) {
            synchronized (getCache()) {
                Number number = ((Number) id);
                getCache().put(number.longValue(), model);
            }
        } else {
            throw new IllegalArgumentException("A ModelLruCache must use an id that can cast to" +
                                               "a Number to convert it into a long");
        }
    }

    @Override
    public ModelClass removeModel(Object id) {
        ModelClass model;
        if(id instanceof Number) {
            synchronized (getCache()) {
                model = getCache().remove(((Number) id).longValue());
            }
        }  else {
            throw new IllegalArgumentException("A ModelLruCache uses an id that can cast to" +
                                               "a Number to convert it into a long");
        }
        return model;
    }

    @Override
    public void clear() {
        synchronized (getCache()) {
            getCache().evictAll();
        }
    }

    @Override
    public void setCacheSize(int size) {
        getCache().resize(size);
    }

    @Override
    public ModelClass get(Object id) {
        if(id instanceof Number) {
            return getCache().get(((Number) id).longValue());
        } else {
            throw new IllegalArgumentException("A ModelLruCache must use an id that can cast to" +
                                               "a Number to convert it into a long");
        }
    }
}
```
