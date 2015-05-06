# Observable Models

When we want to listen for changes to a ```Model```, the ```FlowContentObserver``` was born. 
It wraps around a standard ```ContentObserver``` by providing callbacks for model Uri events. 
You can create one observer and add as many ```ModelChangeListener``` as you need to respond to the model change events.

### How to use

Create a ```FlowContentObserver```

```java

FlowContentObserver observer = new FlowContentObserver();

// registers for callbacks from the specified table
observer.registerForContentChanges(context, MyTable.class);

// call this to release itself from content changes
observer.unregisterForContentChanges(context);
```

Take this ```TestNotifiableModel```

```java

@Table
public class TestNotifiableModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY)
    String name;

}

```

And take this ```ModelChangeListener```

```java

 FlowContentObserver.ModelChangeListener modelChangeListener = new FlowContentObserver.ModelChangeListener() {
            @Override
            public void onModelChanged() {
                // called in SDK<14
            }

            @Override
            public void onModelSaved() {
                
            }

            @Override
            public void onModelDeleted() {
                
            }

            @Override
            public void onModelInserted() {
               
            }

            @Override
            public void onModelUpdated() {
                
            }
        };

```


To listen for event changes call:

```java

observer.addModelChangeListener(modelChangeListener);

TestNotifiableModel testModel = new TestNotifiableModel();
testModel.setName("myName");

// will notify our observer automatically, no configuration needed!
testModel.insert(async);
testModel.update(async);
testModel.save(async);
testModel.delete(async);

// when done with listener
observer.removeModelChangeListener(modelChangeListener);

```
