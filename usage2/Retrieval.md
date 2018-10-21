# Retrieval

DBFlow provides a few ways to retrieve information from the database. Through
the `Model` classes we can map this information to easy-to-use objects.

DBFlow provides a few different ways to retrieve information from the database. We
can retrieve synchronously or asynchronous (preferred).

We can also use `ModelView` ([read here](ModelViews.md)) and `@Index` ([read here](Indexing.md)) to perform faster retrieval on a set of data constantly queried.

## Synchronous Retrieval

Using the [SQLite query language](SQLiteWrapperLanguage.md) we can retrieve
data easily and expressively. To perform it synchronously:


```kotlin

// list
val employees = (select from Employee:class).list

// single result, we apply a limit(1) automatically to get the result even faster.
val employee: Employee? = (select from Employee::class
                        where Employee_Table.name.eq("Andrew Grosner")).result

// can require result to get non-null if you know it exists
// throws a SQLiteException if missing
val employee: Employee? = (select from Employee::class
                        where Employee_Table.name.eq("Andrew Grosner")).requireResult

// get a custom list
val employees = (select from Employee::class)
                .customList<AnotherTable>(database)

// custom object
val anotherObject = (select from Employee::class
                        where(Employee_Table.name.eq("Andrew Grosner")))
                        .customSingle<AnotherTable>();

```

To query custom objects or lists, see how to do so in [QueryModel](QueryModels.md).

Also you can query a `FlowCursorList`/`FlowTableList` from a query easily
via `queryCursorList()` and the `queryTableList()` methods. To see more on these,
go to [Flow Lists](ListBasedQueries.md).

## Asynchronous Retrieval

DBFlow provides the very-handy `Transaction` system that allows you to place all
calls to the DB in a background queue. Using this system, we recommend placing retrieval queries
on this queue to help prevent locking and threading issues when using a database.

We wrap our queries in a `beginTransactionAsync` block, executing and providing call backs to the method as follows:

```java

database.beginTransactionAsync { db ->
  (select from TestModel1::class
    where TestModel1_Table.name.is("Async")).querySingle(db)
  }
    .execute(
     success = { transaction, r ->   }, // if successful
     error  = { transaction, throwable ->  }, // any exception thrown is put here
     completion = { transaction -> }) // always called success or failure
```

A  `ITransaction<R>` simply returns a result, `R` , which could be a query, or a result from multiple queries combined into one result. 

By default the library uses an ordered queue that
executes FIFO (first-in-first-out) and blocks itself until new `Transaction` are added. Each subsequent call to the `database.beginTransactionAsync` places a new transaction on this queue.

If you wish to customize and provide a different queue (or map it to an existing system), read up on [Transactions](StoringData.md). We also provide constructs such as coroutines and RX `Observables` to map to your team's needs.
