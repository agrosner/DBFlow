# SQL Statements Using the Wrapper Classes

In SQL with Android, writing SQL is not that _fun_, so to make it easy and useful,  this library provides a comprehensive set of SQLite statement wrappers. 

In the first section I describe how using the wrapper classes drastically simplify code writing.

### Code-Wrapped Exmaple

For example, we want to find all devices from ```DeviceTable``` that are Samsung Galaxy S5 and from T-Mobile. Writing the SQL statement is easy enough:

```sql

SELECT * FROM DeviceTable where name = 'Samsung-Galaxy-S5' AND carrier = 'T-MOBILE'; 

```

We want to write this in Android code, convert the statement into data that we can use in our application:

```java

String[] args = new String[2];
args[0] = "Samsung-Galaxy-S5";
args[1] = "T-MOBILE";
Cursor cursor = db.rawQuery("SELECT * FROM DeviceTable where name = ? AND carrier = ?", args);
final List<DeviceObject> entities = new ArrayList<DeviceObject>();
DeviceObject deviceObject;

if (cursor.moveToFirst()) {
  do {
    // get each column and then set it on each 
    deviceObject = new DeviceObject();
    deviceObject.setName(cursor.getString(cursor.getColumnIndex("name")));
    deviceObject.setCarrier(cursor.getString(cursor.getColumnIndex("carrier"));
    entities.add(deviceObject);
  }
  while (cursor.moveToNext());
}

```

This is short and sweet for simple queries, but why do we have to keep writing these statements? 

What happens when:
  1. We add or remove columns
  2. Have to write more functions like this for other tables, queries, and other kinds of data?

In short, we want this code to be maintainable, short, reusable, and expressive of what actually is happening. In this library, calling this statement becomes as easy as, given you built your database and model correctly: 

```java

// main thread retrieval
List<DeviceObject> devices = new Select().from(DeviceObject.class)
  .where(
      Condition.column(DeviceObject$Table.NAME).is("Samsung-Galaxy-S5"), 
      Condition.column(DeviceObject$Table.CARRIER).is("T-Mobile")).queryList();
      
// Async Transaction Queue Retrieval (Recommended)
new Select().from(DeviceObject.class)
  .where(
      Condition.column(DeviceObject$Table.NAME).is("Samsung-Galaxy-S5"), 
      Condition.column(DeviceObject$Table.CARRIER).is("T-Mobile"))
  .transactList(new TransactionListenerAdapter<List<DeviceObject>>() {
    @Override
    public void onResultReceived(List<DeviceObject> devices) {
      // retrieved here
    }
  
  });

```
