## Transaction Manager

`TransactionManager` is the main class that manages batch DB interactions.
It is useful for retrieving, updating, saving, and deleting lists of items.
It wraps around the _native_ builder notation for SQL statements and makes it dead-simple to perform database **Transactions**.

The `TransactionManager` utilizes a `DBTransactionQueue`. This queue is based on the `VolleyRequestQueue` from Volley
by using a `PriorityBlockingQueue`. This queue will order our database transactions by priority using the following order (highest to lowest):
  1. **UI**: Reserved for only immediate tasks and all forms of fetching that will display on the UI
  2. **HIGH**: Reserved for tasks that will influence user interaction,
  such as displaying data in the UI at some point in the future (not necessarily right away)
  3. **NORMAL**: Default priority for a ```Transaction```, good when adding transactions that the app does not need to access right away.
  4. **LOW**: Low-priority, reserved for non-essential tasks.

`DBTransactionInfo`: Holds information on how to process a `BaseTransaction` on the `DBTransactionQueue`. It contains a name and priority. The name is purely for debugging purposes during runtime and to identify it when executed. The priority corresponds to the previous paragraph on priority.

These priorities are just `int` and you can specify your own higher, or different priorities as needed.

For advance usage, the `TableTransactionManager` or by extending the `TransactionManager` class,
you can create and specify its own `DBTransactionQueue`. You will need to now use that manager for any transaction that was intended for it.


### Batch Interactions

Batching a list of models for saving without blocking the UI previously was done this way:

```java

  new Thread() {
    @Override
    public void run(){
        database.beginTransaction();
        try {
            for(ModelClass model: models){
              // save model here
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }
  }.start();

```

Instead of writing these lines of code, making a utility method for it, or having to create a whole system of threads, we use:

```java

TransactionManager.getInstance().addTransaction(new SaveModelListTransaction<>(ProcessModelInfo.withModels(models)));

```

### Batch Processing of `Model`

`ProcessModelInfo`: describes how to handle a Transaction by storing information such as the `DBTransactionInfo`, table,
a list of models, and a `TransactionListener` for the Transaction.
**Note***: This class was created to drastically simplify the number of methods in the `TransactionManager`.

For large operations that span hundreds of Models, the preferred method is to run it on the `DBTransactionQueue`.
This will queue up the operations on the same thread to mitigate synchronization locking and UI thread blocking.

For massive `save()` operations, the preferred method is through the ```DBBatchSaveQueue```.
This will run a batch `DBTransaction` once the queue is full (default is 50 models and can be changed) on all of the Models at one time.
If you are saving smaller amounts of items, or need them to save right away, the best option is to use regular save transactions.

#### Example

```java

  ProcessModelInfo<SomeModel> processModelInfo = ProcessModelInfo<SomeModel>.withModels(models)
                                                                            .result(resultReceiver)
                                                                            .info(myInfo);

  TransactionManager.getInstance().saveOnSaveQueue(models);

  // or directly to the queue
  TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(processModelInfo));

  // Updating only updates on the ``DBTransactionQueue``
  TransactionManager.getInstance().addTransaction(new UpdateModelListTransaction(processModelInfo));

  TransactionManager.getInstance().addTransaction(new DeleteModelListTransaction(processModelInfo));

  ```


There are all sorts of methods in `TransactionManager` for performing queries on the `DBTransactionQueue`
for fetching, saving, updateing, and deleting. Check it out!

### Retrieving Models

There are two concepts to learn about retrieval: **fetching** vs. **selecting**. _Fetching_ performs the _select_ on the `DBTransactionQueue`, and when it completes the `TransactionListener` will be called on the UI thread. A _select_ will be done on the current thread. While this is OK for simple database interactions, it's much better to perform these operations on the `DBTransactionQueue` so that other operations do not cause a "lock" of the main thread.

```java

  // Just get all items from the table
  // You can even use Select and Where statements instead
  TransactionManager.getInstance().fetchAllFromTable(TestModel.class, new TransactionListenerAdapter<TestModel.class>() {
     @Override
    public void onResultReceived(List<TestModel> testModels) {
        // on the UI thread, do something here
    }
  }, condition1, condition2,..);

```

### Get a model by Id

The ```TransactionManager``` will do a lookup for the ```Model``` primary where query and let you look it up by passing in the ids of the object. They must be in the order of declaration:

```java

// retrieve immediatelty
TestModel testModel = Select.byId(TestModel.class, TestModel$Table.NAME);

// Fetching so we do not block UI
TransactionManager.getInstance().fetchModelById(TestModel.class, transactionListener, TestModel$Table.NAME);

```

### Custom Transactions

This library makes it very easy to perform custom transactions. Add them to the ```TransactionManager``` by:

```java

TransactionManager.getInstance().addTransaction(myTransaction);

```

There are a few ways to create a specific transaction you wish:

  1. Extending ```BaseTransaction``` will require you to run something in ```onExecute()```.

  ```java

    BaseTransaction<TestModel1> testModel1BaseTransaction = new BaseTransaction<TestModel1>() {
      @Override
      public TestModel1 onExecute() {
      // do something and return an object
        return testModel;
      }
    };

  ```
  2. ```BaseResultTransaction``` adds a simple ```TransactionListener``` that enables you to listen for transaction updates.

  ```java

  BaseResultTransaction<TestModel1> baseResultTransaction = new BaseResultTransaction<TestModel1>(dbTransactionInfo, transactionListener) {
    @Override
    public TestModel1 onExecute() {
      return testmodel;
    }
  };

  ```
  3. ```ProcessModelTransaction``` takes in a model and enables you to define how to process each individual model in the ```DBTransactionQueue```.

  ```java

  public class CustomProcessModelTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {

    public CustomProcessModelTransaction(ProcessModelInfo<ModelClass> modelInfo) {
        super(modelInfo);
    }

    @Override
    public void processModel(ModelClass model) {
        // process model class here!
    }
  }

  ```

  4. ```QueryTransaction``` has you use a ```Queriable``` to retrieve a cursor in whatever way you what.
