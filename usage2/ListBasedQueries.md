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
