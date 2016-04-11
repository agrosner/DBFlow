# List-Based Queries

When we have large datasets from the database in our application, we wish to
display them in a `ListView`, `RecyclerView` or some other component that recycles
it's views. Instead of running a potentially very large query on the database,
converting it to a `List` and then keeping that chunk of memory active, we
can lazy-load each row from the query/table.

DBFlow makes it easy using the `FlowCursorList`, for simple `BaseAdapter`-like methods,
or the `FlowQueryList`, which implements the `List` interface.

Getting one of these lists is as simple as:

```java

FlowQueryList<MyTable> list = SQLite.select()
    .from(MyTable.class)
    .where(...) // some conditions
    .flowQueryList();
FlowCursorList<MyTable> list = SQLite.select()
    .from(MyTable.class)
    .where(...) // some conditions
    .cursorList();

```

Any query method allows you to retrieve a default implementation of each. You
can also manually instantiate them:

```java

FlowQueryList<MyTable> list = new FlowQueryList<>(SQLite.select().from(MyTable.class));
FlowCursorList<MyTable> list = new FlowCursorList<>(SQLite.select().from(MyTable.class));

```

## Caching

Both of these classes come with the ability to cache `Model` used in it's queries
so that loading only happens once and performance can remain high once loaded. The default
caching mechanism is a `ModelLruCache`, which provides an `LruCache` to manage
loading `Model`.

They are done in almost the same way:

```java

FlowCursorList<MyTable> list = new FlowCursorList<>(SQLite.select().from(MyTable.class));
FlowQueryList<MyTable> list = new FlowQueryList<>(SQLite.select().from(MyTable.class));

// caching on by default.
// to turn on or off use this method
list.setCachingEnabled(false); // caching off, will clear cache too

// or we can pass if on or off in constructor too
FlowCursorList<MyTable> list = new FlowCursorList<>(enabled, SQLite.select().from(MyTable.class));
FlowQueryList<MyTable> list = new FlowQueryList<>(enabled, SQLite.select().from(MyTable.class));

```

To override or specify a custom cache, you must subclass and override the `getBackingCache()`
methods depending on which one you use:


```java

FlowQueryList<MyTable> list = new FlowQueryList<>(SQLite.select().from(MyTable.class)) {
  @Override
  protected ModelCache<MyTable, ?> getBackingCache() {
    return new SimpleMapCache<MyTable>(); // stores content in a Map
  }
};

FlowQueryList<MyTable> list = new FlowQueryList<>(SQLite.select().from(MyTable.class)) {
  @Override
  protected ModelCache<MyTable, ?> getBackingCache(int size) {
    return new SimpleMapCache<MyTable>(); // stores content in a Map, size ignored
  }
}


```

## FlowCursorList

The `FlowCursorList` is simply a wrapper around a standard `Cursor`, giving it the
ability to cache `Model`, load items at specific position with conversion, and refresh
it's content easily.

By default the cache size becomes the size of the `Cursor` returned from the query.
We constrain the size to greater than or equal to 20 unless 0 or not specified, where
we set it to 50.  **Note** not all caches support a size, read on default provided caches
[here](/usage2/Caching.md).

The `FlowCursorList` provides these methods:

  1. `getItem(position)` - loads item from `Cursor` at specified position, caching and loading from cache (if enabled)
  2. `refresh()` - re-queries the underlying `Cursor`, clears out the cache, and reconstructs it
  3. `getAll()` - returns a `List` of all items from the `Cursor`, no caching used
  4. `getCount()` - returns count of `Cursor` or 0 if `Cursor` is `null`
  5. `isEmpty()` - returns if count == 0
  6. `clearCache()` - manually clears cache

## Flow Query List

This class is a much more powerful version of the `FlowCursorList`. This class
acts as `List` and can be used wherever a `List` is used. Also, it is a `FlowContentObserver`
see [Observability](/usage2/Observability.md), meaning other classes can listen
for its specific changes and it can auto-refresh itself when content changes.

Feature rundown:
  1. `List` implementation of a Query
  2. `FlowContentObserver`, only for the table that it corresponds to in its initial `ModelQueriable` query statement
  3. Transact changes to the query asynchronously (note that this refreshes itself every callback)
