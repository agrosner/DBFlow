# Retrieval

DBFlow provides a few ways to retrieve information from the database. Through
the `Model` classes we can map this information to easy-to-use objects.

DBFlow provides a few different ways to retrieve information from the database. We
can retrieve synchronously or asynchronous (preferred).

We can also use `ModelView` ([read here](ModelViews.md)) and `@Index` ([read here](Indexing.md)) to perform faster retrieval on a set of data constantly queried.

## Synchronous Retrieval

Using the [SQLite query language](SQLiteWrapperLanguage.md) we can retrieve
data easily and expressively. To perform it synchronously:


```java

// list
List<Employee> employees = SQLite.select()
                            .from(Employee.class)
                            .queryList();

// single result, we apply a limit(1) automatically to get the result even faster.
Employee employee = SQLite.select()
                        .from(Employee.class)
                        .where(Employee_Table.name.eq("Andrew Grosner"))
                        .querySingle();

// get a custom list
List<AnotherTable> employees = SQLite.select()
                            .from(Employee.class)
                            .queryCustomList(AnotherTable.class);

// custom object
AnotherTable anotherObject = SQLite.select()
                        .from(Employee.class)
                        .where(Employee_Table.name.eq("Andrew Grosner"))
                        .queryCustomSingle(AnotherTable.class);

```

To query custom objects or lists, see how to do so in [QueryModel](QueryModel.md).

Also you can query a `FlowCursorList`/`FlowTableList` from a query easily
via `queryCursorList()` and the `queryTableList()` methods. To see more on these,
go to [Flow Lists](FlowLists.md).


## Asynchronous Retrieval

DBFlow provides the very-handy `Transaction` system that allows you to place all
calls to the DB in a queue. Using this system, we recommend placing retrieval queries
on this queue to help prevent locking and threading issues when using a database.

A quick sample of retrieving data asyncly:

```java

SQLite.select()
  .from(TestModel1.class)
  .where(TestModel1_Table.name.is("Async"))
  .async()
  .queryResultCallback(new QueryTransaction.QueryResultCallback<TestModel1>() {
      @Override
      public void onQueryResult(QueryTransaction transaction, @NonNull CursorResult<TestModel1> tResult) {

      }
  }).execute();

```

This is fundamentally equal to:

```java


FlowManager.getDatabaseForTable(TestModel1.class)
                .beginTransactionAsync(new QueryTransaction.Builder<>(
                    SQLite.select()
                        .from(TestModel1.class)
                        .where(TestModel1_Table.name.is("Async")))
                    .queryResult(new QueryTransaction.QueryResultCallback<TestModel1>() {
                        @Override
                        public void onQueryResult(QueryTransaction transaction, @NonNull CursorResult<TestModel1> tResult) {

                        }
                    }).build())
.build().execute();

```

The first example in this section is more of a convenience for (2).

By default the library uses the `DefaultTransactionManager` which utilizes
a `DefaultTransactionQueue`. This queue is essentially an ordered queue that
executes FIFO (first-in-first-out) and blocks itself until new `Transaction` are added.

If you wish to customize and provide a different queue (or map it to an existing system), read up on [Transactions](StoringData.md).


Compared to pre-3.0 DBFlow, this is a breaking change from the old, priority-based
queue system. The reason for this change was to simplify the queuing system and
allow other systems to exist without confusing loss of functionality. To keep the old
system read [Transactions](StoringData.md).

## Faster Retrieval

In an effort to squeeze out more speed at the potential cost of flexibility, DBFlow provides a
couple ways to optimize loads from the DB. If you do not wish to use caching but wish
to speed conversion from `Cursor` to `Model`, read on.

 If you simply retrieve a `List` of `Model`
without any projection from your DB, you can take advantage of 2 features:
    1. `@Table(orderedCursorLookUp = true)` -> We do not call `Cursor.getColumnIndex()` and assume that the `Cursor` is ordered by column declarations in the class.
    2. `@Table(assignDefaultValuesFromCursor = false)` -> We do not expect to reuse an object from the DB (or care) if the corresponding fields aren't assigned a value when missing from the `Cursor`.
