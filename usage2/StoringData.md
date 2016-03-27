# Storing Data

DBFlow provide a few mechanisms by which we store data to the database. The difference of options
should not provide confusion but rather allow flexibility in what you decide is the best way
to store information.

## Synchronous Storage

While generally saving data synchronous should be avoided, for small amounts of data
it has little effect.

```java

model.insert(); // inserts
model.update(); // updates
model.save(); // checks if exists, if true update, else insert.

```

Code like this should be avoided:

```java

for (int i = 0; i < models.size(), i++) {
  models.get(i).save();
}

```

Instead we should move onto `Transaction`.

## Transactions

Transactions are ACID in SQLite, meaning they either occur completely or not at all.
Using transactions significantly speed up the time it takes to store. So recommendation
you should use transactions whenever you can.

DBFlow supports two kinds of transactions: synchronous (blocking) vs. asynchronous.

### Synchronous Transactions

A simple database transaction can be wrapped in a call:

```java

FlowManager.getDatabase(AppDatabase.class).executeTransaction(new ITransaction() {
            @Override
            public void execute(DatabaseWrapper databaseWrapper) {
                // something here
                Player player = new Player("Andrew", "Grosner");
                player.save(databaseWrapper); // use wrapper (from BaseModel)
            }
});

```

Even though DBFlow is ridiculously fast, this should be put on a separate thread
 outside of the UI, so that your UI remains responsive on all devices.

 ### Async Transactions

 This is the preferred method. Transactions, using the `DefaultTransactionManager`,
 occur on one thread per-database (to prevent flooding from other DB in your app)
  and receive callbacks on the UI.

 A basic transaction:

 ```java

DatabaseDefinition database = FlowManager.getDatabase(AppDatabase.class);
Transaction transaction = database.beginTransactionAsync(new ITransaction() {
            @Override
            public void execute(DatabaseWrapper databaseWrapper) {
                called.set(true);
            }
        }).build();
transaction.execute(); // execute

transaction.cancel();
 // attempt to cancel before its run. If it's already ran, this call has no effect.

 ```

 `Transaction` have callbacks to allow you to "listen" for success and errors.

 ```java


 transaction
    .success(new Transaction.Success() {
       @Override
       public void onSuccess(Transaction transaction) {
          // called post-execution on the UI thread.
       }
   })
   .error(new Transaction.Error() {
       @Override
       public void onError(Transaction transaction, Throwable error) {

       }
   });


 ```
