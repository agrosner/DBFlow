# Observable Models
When we want to listen for changes to a `Model`, the `FlowContentObserver` was born. It wraps around a standard `ContentObserver` by providing callbacks for model Uri events. You can create one observer and add as many `OnModelStateChangeListener` as you need to respond to the model change events. The callback will return the table, what action it took (save, delete, insert, update), and what primary keys changed and their values (_NOTE:_ only available in   API 16+).

## How to use
Create a `FlowContentObserver`

```java

FlowContentObserver observer = new FlowContentObserver();

// registers for callbacks from the specified table
observer.registerForContentChanges(context, MyTable.class);

// call this to release itself from content changes
observer.unregisterForContentChanges(context);
```

Take this `TestNotifiableModel`

```java

@Table(database = SomeDatabase.class)
public class TestNotifiableModel extends BaseModel {

    @PrimaryKey
    String name;

}
```

And take this `OnModelStateChangeListener`

```java

 FlowContentObserver.OnModelStateChangeListener modelChangeListener = new FlowContentObserver.OnModelStateChangeListener() {
            @Override
            public void onModelStateChanged(Class<? extends Model> table, Action action, SQLCondition[] primaryKeyValues) {

            }
        };
```

To listen for event changes call:

```java

observer.addModelChangeListener(modelChangeListener);

TestNotifiableModel testModel = new TestNotifiableModel();
testModel.setName("myName");

// will notify our observer automatically, no configuration needed!
testModel.insert();
testModel.update();
testModel.save();
testModel.delete();

// when done with listener
observer.removeModelChangeListener(modelChangeListener);
```

### Transaction notification Bundling
You can combine and transact `Model` changes into one batch call via `beginTransaction()...endTransactionAndNotify()` methods:

Example:

```java

flowContentObserver.beginTransaction();

someModel.save();
// More modifications on a table for what the Flow Content Observer is registered.


// collects all unique URI and calls onChange here
flowContentObserver.endTransactionAndNotify();
```
