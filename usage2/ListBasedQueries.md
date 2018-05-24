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

    list.close(); // ensure you close these, as they utilize active cursors :)

```

```kotlin
val list = (select from MyTable::class where (...)).cursorList
val list = (select from MyTable::class where (...)).flowQueryList
list.close()
```

Any query method allows you to retrieve a default implementation of each. You
can also manually instantiate them:

```java

FlowQueryList<MyTable> list = new FlowQueryList.Builder<>(SQLite.select().from(MyTable.class))
  .cachingEnabled(false) // caching enabled by default
  .build();

FlowCursorList<MyTable> list = new FlowCursorList.Builder<>(SQLite.select().from(MyTable.class))
  .cachingEnabled(true)
  .modelCache(cache) // provide custom cache for this list
  .build();

```

## Caching

Both of these classes come with the ability to cache `Model` used in it's queries
so that loading only happens once and performance can remain high once loaded. The default
caching mechanism is a `ModelLruCache`, which provides an `LruCache` to manage
loading `Model`.

They are done in almost the same way:

```java

FlowCursorList<MyTable> list = new FlowCursorList.Builder<>(SQLite.select().from(MyTable.class))
  .modelCache(cache) // provide custom cache for this list
  .build();
FlowQueryList<MyTable> list = new FlowQueryList.Builder<>(SQLite.select().from(MyTable.class))
  .modelCache(cache)
  .build();

```

## FlowCursorList

The `FlowCursorList` is simply a wrapper around a standard `Cursor`, giving it the
ability to cache `Model`, load items at specific position with conversion, and refresh
it's content easily.

The `FlowCursorList` by default caches its results, for fast usage. The cache size is determined by the `ModelCache` you're using. Read on [here](Caching.md).

The `FlowCursorList` provides these methods:

  1. `getItem(position)` - loads item from `Cursor` at specified position, caching and loading from cache (if enabled)
  2. `refresh()` - re-queries the underlying `Cursor`, clears out the cache, and reconstructs it. Use a `OnCursorRefreshListener` to get callbacks when this occurs.
  3. `getAll()` - returns a `List` of all items from the `Cursor`, no caching used
  4. `getCount()` - returns count of `Cursor` or 0 if `Cursor` is `null`
  5. `isEmpty()` - returns if count == 0
  6. `clearCache()` - manually clears cache

## Flow Query List

This class is a much more powerful version of the `FlowCursorList`. It contains a `FlowCursorList`,
which backs it's retrieval operations.

This class acts as `List` and can be used almost wherever a `List` is used. Also, it is a `FlowContentObserver`
see [Observability](Observability.md), meaning other classes can listen
for its specific changes and it can auto-refresh itself when content changes.

Feature rundown:
  1. `List` implementation of a Query
  2. `FlowContentObserver`, only for the table that it corresponds to in its initial `ModelQueriable` query statement. Mostly used for self refreshes.
  3. Transact changes to the query asynchronously (note that this refreshes itself every callback unless in a transaction state)
  5. Caching (almost same implementation as `FlowCursorList`)

### List Implementation

The `List` implementation is mostly for convenience. Please note that most of the modification
methods (`add`, `addAll` etc.) may not affect the query that you expect it to, unless the object you pass
objects that are valid for the query and you enable self refreshes.

The retrieval methods are where the query works as you would expect. `get()` calls
`getItem()` on the internal `FlowCursorList`, `isEmpty()`, `getCount()`, etc all correspond
to the `Cursor` underneath.

Both `FlowQueryList` and `FlowTableList` support `Iterator` and provide a very
efficient class: `FlowCursorIterator` that iterates through each row in a `Cursor`
and provides efficient operations.

**Note**: any retrieval operation that turns it into another object (i.e. `subList()`,
`toArray`, etc) retrieves all objects contained in the query into memory,
and then converts it using the associated method on that returned `List`.

### FlowContentObserver Implementation

Using the `FlowContentObserver`, we can enable self-refreshes whenever a model changes
for the table this query points to. See [Observability](Observability.md).

To turn on self-refreshes, call `registerForContentChanges(context)`, which requeries
the data whenever it changes.

We recommend placing this within a transaction on the `FlowQueryList`, so we only
refresh content the minimal amount of times:

```java

flowQueryList.beginTransaction();

// perform a bunch of modifications

flowQueryList.endTransactionAndNotify();

```

To listen for `Cursor` refreshes register a `OnCursorRefreshListener`:

```java

modelList
  .addOnCursorRefreshListener(new FlowCursorList.OnCursorRefreshListener<ListModel>() {
      @Override
      public void onCursorRefreshed(FlowCursorList<ListModel> cursorList) {

      }
  });

```


### Transact Changes Asynchronously

If you want to pass or modify the `FlowQueryList` asynchronously, set `setTransact(true)`.
This will run all modifications in a `Transaction` and when completed, a `Cursor` refresh occurs.

You can also register `Transaction.Error` and `Transaction.Success` callbacks for these modifications
on the `FlowQueryList` to handle when these `Transaction` finish.
