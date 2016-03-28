# Retrieval

DBFlow provides a few ways to retrieve information from the database. Through
the `Model` classes we can map this information to easy-to-use objects.

DBFlow provides a few different ways to retrieve information from the database. We
can retrieve synchronously or asynchronous (preferred).

## Synchronous Retrieval

Using the [SQLite query language](/usage2/SQLiteWrapperLanguage.md) we can retrieve
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

To query custom objects or lists, see how to do so in [QueryModel](/usage2/QueryModel.md).

Also you can query a `FlowCursorList`/`FlowTableList` from a query easily
via `queryCursorList()` and the `queryTableList()` methods. To see more on these,
go to [Flow Lists](/usage2/FlowLists.md).


## Asynchronous Retrieval

DBFlow provides the very-handy `Transaction` system that allows you to place all
calls to the DB in a queue. Using this system, we recommend placing retrieval queries
on this queue to help prevent locking and threading issues when using a database.


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
