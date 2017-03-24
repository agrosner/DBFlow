# Observability

DBFlow provides a flexible way to observe changes on models and tables in this library.

By default, DBFlow utilizes the [`ContentResolver`](https://developer.android.com/reference/android/content/ContentResolver.html)
to send changes through the android system. We then can utilize [`ContentObserver`](http://developer.android.com/reference/android/database/ContentObserver.html) to listen for these changes via the `FlowContentObserver`.

Also, DBFlow also supports direct [model notification](/usage2/Observability.md#direct-changes) via a custom `ModelNotifier`.

## FlowContentObserver

The content observer converts each model passed to it into `Uri` format that describes the `Action`, primary keys, and table of the class that changed.

A model:
```kotlin

@Table(database = AppDatabase.class)
class User(@PrimaryKey var id: Int = 0, @Column var name: String = "")

```

with data:
```kotlin

User(55, "Andrew Grosner").delete()

```

converts to:

```
dbflow://%60User%60?%2560id%2560=55#DELETE
```

Then after we register a `FlowContentObserver`:

```java

FlowContentObserver observer = new FlowContentObserver();

observer.registerForContentChanges(context, User.class);

// do something here
// unregister when done
observer.unregisterForContentChanges(context);

```

## Model Changes

It will now receive the `Uri` for that table. Once we have that, we can register for model changes on that content:

```java

observer.addModelChangeListener(new OnModelStateChangedListener() {
  @Override
  public void onModelStateChanged(@Nullable Class<? extends Model> table, BaseModel.Action action, @NonNull SQLOperator[] primaryKeyValues) {
    // do something here
  }
});


```
The method will return the `Action` which is one of:
  1. `SAVE` (will call `INSERT` or `UPDATE` as well if that operation was used)
  2. `INSERT`
  3. `UPDATE`
  4. `DELETE`

The `SQLOperator[]` passed back specify the primary column and value pairs that were changed for the model.

If we want to get less granular and just get notifications when generally a table changes, read on.

## Register for Table Changes

Table change events are similar to `OnModelStateChangedListener`, except that they only specify the table and action taken. These get called for any action on a table, including granular model changes. We recommend batching those events together, which we describe in the next section.

```java

addOnTableChangedListener(new OnTableChangedListener() {
    @Override
    public void onTableChanged(@Nullable Class<? extends Model> tableChanged, BaseModel.Action action) {
        // perform an action. May get called many times! Use batch transactions to combine them.
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
for(User user: users) {
  users.save();
}

observer.endTransactionAndNotify(); // callback batched

```

Batch interactions will store up all unique `Uri` for each action (these include `@Primary` key of the `Model` changed). When `endTransactionAndNotify()` is called,
all those `Uri` are called in the `onChange()` method from the `FlowContentObserver` as expected.

If we are using `OnTableChangedListener` callbacks, then by default we will receive one callback per `Action` per table.  If we wish to only receive a single callback, set `setNotifyAllUris(false)`, which will make the `Uri` all only specify `CHANGE`.

# Direct Changes

DBFlow also supports direct observability on model changes rather than convert those models into `Uri` and have to decipher what has changed.

To set up direct changes we override the default `ModelNotifier`:

```java

FlowManager.init(FlowConfig.Builder(context)
            .addDatabaseConfig(DatabaseConfig.Builder(TestDatabase.class)
                .modelNotifier(DirectModelNotifier.get())
                .build()).build());

```

We must use the shared instance of the `DirectModelNotifier` since if we do not, your listeners will not receive callbacks.

Next register for changes on the `DirectModelNotifier`:
```java

DirectModelNotifier.get().registerForModelChanges(User.class, new ModelChangedListener<User>() {
            @Override
            public void onModelChanged(User model, BaseModel.Action action) {
                // react to model changes
            }

            @Override
            public void onTableChanged(BaseModel.Action action) {
              // react to table changes.
            }
        };)

```

Then unregister your model change listener when you don't need it anymore (to prevent memory leaks):

```java
DirectModelNotifier.get().unregisterForModelChanges(Userr.class, modelChangedListener);

```
