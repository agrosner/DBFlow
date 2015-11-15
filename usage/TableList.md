# Queries as Lists
In this library, we enable easy list and adapter-based operations using `FlowCursorList` and `FlowQueryList`. Both of these correspond (in order) to `BaseAdapter` and `List<? extends Model>`.

## Cursor List
A read-only cursor-backed object that provides caching of retrieved models used to display in a list. It contains methods very similiar to `BaseAdapter` such as `getCount()` and `getItem(position)`.

This class becomes useful when we have a very large dataset that we wish to display on screen without killing memory, or keep memory usage to a slim minimum by only loading items we need. This class is similar to `CursorAdapter` except it does not extend any adapter class and provides a simple yet effective API for retrieving and converted data from the database with ease.

There are a few ways of defining it:
- Using any valid `ModelQueriable` such as `From`, `StringQuery`, and `Where`

  ```java
  // true to cache models, false to not.
  new FlowCursorList<>(true, SQLite.select().from(TestModel.class)
  .where(TestModel1_Table.name.like("pasta%")));

  // true to cache models, specifying a size of the cache (ignored if the cache does not support it)
  new FlowCursorList<>(1000, SQLite.select().from(TestModel.class)
   .where(TestModel1_Table.name.like("pasta%")));
  ```

- Same constructors but specify table and Conditions:

  ```java

  new FlowCursorList<>(true, TestModel.class, TestModel1_Table.name.like("pasta%"));

  new FlowCursorList<>(1000, TestModel.class, TestModel1_Table.name.like("pasta%"));
  ```

### Example

```java

  private class TestModelAdapter extends BaseAdapter {
    private FlowCursorList<TestModel1> mFlowCursorList;

    public TestModelAdapter() {

    // retrieve and cache rows that have a name like pasta%
      mFlowCursorList = new FlowCursorList<>(true, TestModel.class,
            TestModel1_Table.name.like("pasta%"));
    }

    @Override
    public int getCount() {
      return mFlowCursorList.getCount();
    }

    @Override
    public TestModel1 getItem(int position) {
      return mFlowCursorList.getItem(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      // Implement here
    }
  }
```

### Specifying custom caches
To use a different cache than the default `LruCache`, override `getBackingCache()`:

```java

FlowCursorList<TestModel> list = new FlowCursorList<TestModel>(true, someWhere) {

    @Override
    protected ModelCache<TestModel, ?> getBackingCache() {
      return new SparseArrayBasedCache();
    }

}
```

## Query List
A `FlowQueryList` is :
1. Java `List` implementation of managing a database table.
2. All modifications affect the table in real-time.
3. All modifications, by default, are immediate.
4. If you wish to not run these on the main thread, call `flowTableList.setTransact(true)`.
5. Internally its backed by a `FlowCursorList` to include all of it's existing functionality.

### Best Practices
- Avoid multiple single operations in a loop and instead use the batch methods. Each operation calls the internal `refresh()`, which reruns the query in the DB.

  ```java

  FlowQueryList<TableModel> flowQueryList = ...;

  // DON'T
  for (int i = 0; i < 50; i++) {
    TableModel object = anotherList.get(i);
    flowQueryList.remove(object);
  }

  // better
  flowQueryList.beginTransaction();
  for (int i = 0; i < 50; i++) {
    TableModel object = anotherList.get(i);
    flowQueryList.remove(object);
  }
  flowQueryList.endTransactionAndNotify();

  // DO
  flowQueryList.removeAll(anotherList);
  ```

- When making changes outside of the `FlowQueryList`, the data contained becomes stale.

  You can either: A. call `refresh()` before querying its data again or B. Register

  self-refreshes:

  ```java

  flowQueryList.enableSelfRefreshes(context);
  ```

- If you're making tons of single changes (using the FlowQueryList or not), it's much more efficient to use a transaction. In this instance we delay the notification of listeners until the

  ```java

  flowQueryList.beginTransaction();

  // perform model changes!!

  // calls any listeners associated with it (including the listener we registered earlier)
  // refreshes the list here (if registered)
  flowQueryList.endTransactionAndNotify();
  ```

### Example

```java

FlowQueryList<TestModel1> flowQueryList = new FlowQueryList<TestModel1>(TestModel1.class);

flowQueryList.beginTransaction();

// Deletes from the table and returns the item
TestModel1 model1 = flowQueryList.remove(0);

// Saves the item back into the DB, updates if it already exists
flowQueryList.add(model1);

// Updates the item in the DB
flowQueryList.set(model1);

// Clears the whole table
flowQueryList.clear();

flowQueryList.endTransactionAndNotify();
```
