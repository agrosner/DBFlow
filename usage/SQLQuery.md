In SQL with Android, writing SQL can be _fun_, however I provide query wrapping to make it very, very easy.

### Example

Writing the SQL statement is easy enough:

```sql

SELECT * FROM DeviceTable where name = 'Samsung-Galaxy-S5' AND carrier = 'T-MOBILE'; 

```

What if we want a different name and carrier? And then we need to retrieve the list of data for our usage.

```java

String[] args = new String[2];
args[0] = "Samsung-Galaxy-S5";
args[1] = "T-MOBILE";
Cursor cursor = db.rawQuery("SELECT * FROM DeviceTable where name = ? AND carrier = ?", args);
final List<DeviceObject> entities = new ArrayList<DeviceObject>();

if (cursor.moveToFirst()) {
  do {
    // get each column and then set it on each 
    entities.add(deviceObject);
  }
  while (cursor.moveToNext());
}

```

This is good for simple queries, but why do we have to keep writing these statements, and what happens when we add or remove columns? We want this code to be maintainable, and not to mention if there are SQL errors from these strings.

### Code Wrapped Example

We introduce something known as the ```Condition```. Conditions are simply pieced together as:

  columnName {operator} value

The **columnName** matches the table's column name. The operator can be any Sqlite comparison such as "=", "<", ">", etc. The value is the ```Model``` value, and the ```WhereQueryBuilder``` converts the value into its proper database value for comparison! Use the ```[ModelClassName]$Table``` class for the column names!

```java
Location location = new Location("");
location.setLatitude(40.7127);
location.setLongitude(74.0059);
Where<DeviceObject> deviceWhere = new Select().from(DeviceObject.class)
                             .where(Condition.column(DeviceObject$Table.NAME).is("Samsung-Galaxy S5"))
                             .and(Condition.column(DeviceObject$Table.CARRIER).is("T-MOBILE"))
                             .and(Condition.column(DeviceObject$Table.LOCATION).is(location);

// or can use Select.all(DeviceObject.class, conditions);

// perform this on current thread (not recommended)
List<DeviceObject> devices = deviceWhere.queryList();

```

The **recommended** way is to do any kind of large query in the BG:

```java

// To do it in the background (to not block UI thread)
deviceWhere.transactList(new TransactionListenerAdapter<List<DeviceObject>>(){
                             @Override
                             public void onResultReceived(List<DeviceObject> devices){
                               // called on the UI thread, do something with the results
                             }
                         });

```
