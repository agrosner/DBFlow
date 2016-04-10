# Observability

DBFlow provides a simple way to observe changes on content and respond to those changes via the `FlowContentObserver`.

The `FlowContentObserver` enables you to watch a specific table or (down to a specific `Model`) for CUDS (Create/Insert, Update, Delete, or Save) operations.

Using the [`ContentObserver`](http://developer.android.com/reference/android/database/ContentObserver.html) subclass `FlowContentObserver`, we can easily listen for changes.


## Register the Observer

```java

FlowContentObserver observer = new FlowContentObserver();

observer.registerForContentChanges(context, MyModelClass.class);


observer.unregisterForContentChanges(context);

```
