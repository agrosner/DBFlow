# Storing Data

DBFlow provide a few mechanisms by which we store data to the database. The difference of options
should not provide confusion but rather allow flexibility in what you decide is the best way
to store information.

## Synchronous Storage

While generally saving data synchronous should be avoided, for small amounts of data
it has little effect.

```kotlin
model.save()
model.insert()
model.update()
```

Avoid saving large amounts of models outside of a transaction:
```kotlin

// AVOID
models.forEach { it.save() }

```

Doing operations on the main thread can block it if you read and write to the DB on a different thread while accessing DB on the main. Instead, use Async Transactions.

### Async Transactions

## Transactions

Transactions are ACID in SQLite, meaning they either occur completely or not at all.
Using transactions significantly speed up the time it takes to store. So recommendation
you should use transactions whenever you can.

Async is the preferred method. Transactions, using the `DefaultTransactionManager`,
 occur on one thread per-database (to prevent flooding from other DB in your app)
  and receive callbacks on the UI. You can override this behavior and roll your own
  or hook into an existing system, read [here](StoringData.md#custom-transactionmanager).

Also to use the legacy, priority-based system, read [here](StoringData.md#priority-queue).

 A basic transaction:

 ```kotlin

 val transaction = database<AppDatabase>().beginTransactionAsync { db ->
    // handle to DB
    // return a result, or execute a method that returns a result
  }.build()
transaction.execute(
   error = { transaction, error -> // handle any exceptions here },
   completion = { transaction -> // called when transaction completes success or fail }
  ) { transaction, result ->
  // utilize the result returned

transaction.cancel();
 // attempt to cancel before its run. If it's already ran, this call has no effect.

 ```

 The `Success` callback runs post-transaction on the UI thread.
 The `Error` callback is called on the UI thread if and only if it is specified and an exception occurs,
 otherwise it is thrown in the `Transaction` as a `RuntimeException`. **Note**:
 all exceptions are caught when specifying the callback. Ensure you handle all
 errors, otherwise you might miss some problems.

### ProcessModelTransaction

`ProcessModelTransaction` allows for more flexibility and for you to easily operate on a set of `Model` in a
`Transaction` easily. It holds a list of `Model` by which you provide the modification
method in the `Builder`. You can listen for when each are processed inside a normal
`Transaction`.

It is a convenient way to operate on them:

```kotlin

database.beginTransactionAsync(items.processTransaction { model, db ->
      // call some operation on model here
      model.save()
      model.insert() // or
      model.delete() // or
    }
    .processListener { current, total, modifiedModel ->
        // for every model looped through and completes
        modelProcessedCount.incrementAndGet();
     }
    .build())
    .execute()

```

You can listen to when operations complete for each model via the `OnModelProcessListener`.
These callbacks occur on the UI thread. If you wish to run them on same thread (great for tests),
set `runProcessListenerOnSameThread()` to `true`.

### FastStoreModelTransaction

The `FastStoreModelTransaction` is the quickest, lightest way to store a `List` of
`Model` into the database through a `Transaction`. It comes with some restrictions when compared to `ProcessModelTransaction`:
  1. All `Model` must be from same Table/Model Class.
  2. No progress listening
  3. Can only `save`, `insert`, or `update` the whole list entirely.

```kotlin
database.beginTransactionAsync(list.fastSave().build())
  .execute()
database.beginTransactionAsync(list.fastInsert().build())
  .execute()
database.beginTransactionAsync(list.fastUpdate().build())
  .execute()
```

What it provides:
  1. Reuses `ContentValues`, `DatabaseStatement`, and other classes where possible.
  2. Opens and closes own `DatabaseStatement` per total execution.
  3. Significant speed bump over `ProcessModelTransaction` at the expense of flexibility.

### Custom TransactionManager

If you prefer to roll your own thread-management system or have an existing
system you can override the default system included.


To begin you must implement a `ITransactionQueue`:

```kotlin

class CustomQueue : ITransactionQueue {
  override fun add(transaction: Transaction<out Any?>) {

  }

  override fun cancel(transaction: Transaction<out Any?>) {

  }

  override fun startIfNotAlive() {
  }

  override fun cancel(name: String) {

  }

  override fun quit() {

  }
}

```

You must provide ways to `add()`, `cancel(Transaction)`, and `startIfNotAlive()`.
The other two methods are optional, but recommended.

`startIfNotAlive()` in the `DefaultTransactionQueue` will start itself (since it's
a thread).

 Next you can override the `BaseTransactionManager` (not required, see later):

```kotlin

class CustomTransactionManager(databaseDefinition: DBFlowDatabase)
    : BaseTransactionManager(CustomTransactionQueue(), databaseDefinition)

```

To register it with DBFlow, in your `FlowConfig`, you must:

```kotlin

FlowManager.init(FlowConfig.Builder(DemoApp.context)
  .database(DatabaseConfig(
    databaseClass = AppDatabase::class.java,
    transactionManagerCreator = { databaseDefinition ->
        CustomTransactionManager(databaseDefinition)
    })
  .build())
.build())

```

### Priority Queue

In versions pre-3.0, DBFlow utilized a `PriorityBlockingQueue` to manage the asynchronous
dispatch of `Transaction`. As of 3.0, it has switched to simply a FIFO queue. To
keep the legacy way, a `PriorityTransactionQueue` was created.

As seen in [Custom Transaction Managers](StoringData.md#custom-transactionmanager),
we provide a custom instance of the  `DefaultTransactionManager` with the `PriorityTransactionQueue` specified:

```kotlin

FlowManager.init(FlowConfig.builder(context)
  .database(DatabaseConfig.Builder(AppDatabase::class.java)
          .transactionManagerCreator { db ->
              // this will be called once database modules are loaded and created.
              DefaultTransactionManager(
                      PriorityTransactionQueue("DBFlow Priority Queue"),
                      db)
          }
          .build())
  .build())
```

What this does is for the specified database (in this case `AppDatabase`),
now require each `ITransaction` specified for the database should wrap itself around
the `PriorityTransactionWrapper`. Otherwise an the `PriorityTransactionQueue`
wraps the existing `Transaction` in a `PriorityTransactionWrapper` with normal priority.


To specify a priority, wrap your original `ITransaction` with a `PriorityTransactionWrapper`:

```kotlin
database<AppDatabase>()
    .beginTransactionAsync(PriorityTransactionWrapper.Builder(myTransaction)
        .priority(PriorityTransactionWrapper.PRIORITY_HIGH).build())
    .execute();

```
