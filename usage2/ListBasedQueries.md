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
FlowCursorList<MyTable> list = new FlowCursorList<>(true, SQLite.select().from(MyTable.class));

```

## Caching

Both of these classes come with the ability to cache `Model` used in it's queries
so that loading only happens once and performance can remain high once loaded. They are done
in the same way:

```java

FlowCursorList list = ...;
list.setCacheModels(true); // caching enabled

```

## FlowCursorList
