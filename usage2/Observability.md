# Observability

DBFlow provides a simple way to observe changes on content and respond to those changes via the `FlowContentObserver`.

The `FlowContentObserver` enables you to watch a specific table or (down to a specific `Model`) for CUDS (Create/Insert, Update, Delete, or Save) operations.

Using the [`ContentObserver`](http://developer.android.com/reference/android/database/ContentObserver.html) subclass `FlowContentObserver`, we can easily listen for changes.


## Register the Observer

```java

FlowContentObserver observer = new FlowContentObserver();

observer.registerForContentChanges(context, MyModelClass.class);

// do something here
// unregister when done
observer.unregisterForContentChanges(context);

```

**Note**: registering the observer uses a count that will only notify if the count of registered observers > 0.

To force notifications call `FlowContentObserver.setShouldForceNotify(true)`.

To reset count, call:
`FlowContentObserver.clearRegisteredObserverCount()`


## Register For Model Changes

From our observer from last section, we now need to register a listener on this observer to get the `Model` change events we want.

```java

observer.addModelChangeListener(new OnModelStateChangedListener() {
  @Override
  public void onModelStateChanged(@Nullable Class<? extends Model> table, BaseModel.Action action, @NonNull SQLCondition[] primaryKeyValues) {
    // do something here
  }
});


```
The method will return the `Action` which is one of:
  1. `SAVE` (will call `INSERT` or `UPDATE` as well if that operation was used)
  2. `INSERT`
  3. `UPDATE`
  4. `DELETE`

The `SQLCondition[]` passed back specify the primary column and value pairs that were changed for the model.

## Register for Table Changes

Table change events are similar to `OnModelStateChangedListener`, except that they only specify the table and action taken. When performed in a batch transaction as we read in next section, this can cut down on the number of callbacks to be one callabck per transaction.

```java

addOnTableChangedListener(new OnTableChangedListener() {
    @Override
    public void onTableChanged(@Nullable Class<? extends Model> tableChanged, BaseModel.Action action) {
        // perform an action. May get called more than once! Use batch transactions to combine them.
    }
});

```

## Batch Up Many Events

Sometimes we're modifying tens or hundreds of items at the same time and we do not wish to get notified for _every_ one but only once for each _kind_ of change that occurs.

To batch up the notifications so that they fire all at once, we use batch transactions:

```java

FlowContentObserver observer = new FlowContentObserver();

observer.beginTransaction();

// save, modify models here
employee.save();

observer.endTransactionAndNotify(); // callback batched

```

Batch interactions will store up all unique `Uri` for each action (these include `@Primary` key of the `Model` changed). When `endTransactionAndNotify()` is called,
all those `Uri` are called in the `onChange()` method from the `FlowContentObserver` as expected.

If we are using `OnTableChangedListener` callbacks, then by default we will receive one callback per `Action` per table.  If we wish to only receive a single callback, set `setNotifyAllUris(false)`, which will make the `Uri` all only specify `CHANGE`.
