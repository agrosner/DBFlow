# SQL Statements Using the Wrapper Classes

In SQL with Android, writing SQL is not that _fun_, so to make it easy and useful,  this library provides a comprehensive set of SQLite statement wrappers.

In the first section I describe how using the wrapper classes drastically simplify code writing.

### Example

For example, we want to find all ants from ```Ant``` that are of type "worker" and are female. Writing the SQL statement is easy enough:

```sql

SELECT * FROM Ant where type = 'worker' AND isMale = 0;

```

We want to write this in Android code, convert the SQL data into useful information:

```java

String[] args = new String[2];
args[0] = "worker";
args[1] = "0";
Cursor cursor = db.rawQuery("SELECT * FROM Ant where type = ? AND isMale = ?", args);
final List<Ant> ants = new ArrayList<Ant>();
Ant ant;

if (cursor.moveToFirst()) {
  do {
    // get each column and then set it on each
    ant = new Ant();
    ant.setId(cursor.getLong(cursor.getColumnIndex("id")));
    ant.setType(cursor.getString(cursor.getColumnIndex("type")));
    ant.setIsMale(cursor.getInt(cursor.getColumnIndex("isMale") == 1);
    ant.setQueenId(cursor.getLong(cursor.getColumnIndex("queen_id")));
    ants.add(ant);
  }
  while (cursor.moveToNext());
}

```

This is short and sweet for simple queries, but why do we have to keep writing these statements?

What happens when:
  1. We add or remove columns
  2. Have to write more functions like this for other tables, queries, and other kinds of data?

In short, we want our code to be maintainable, short, reusable, and still remain expressive
of what actually is happening. In this library, this query becomes very easy:

```java

// main thread retrieval
List<Ant> devices = new Select().from(Ant.class)
  .where(
      Condition.column(Ant$Table.TYPE).eq("worker"),
      Condition.column(Ant$Table.ISMALE).eq(false)).queryList();

// Async Transaction Queue Retrieval (Recommended)
TransactionManager.getInstance().addTransaction(new SelectListTransaction<>(
  new Select()
  .from(DeviceObject.class)
  .where(
      Condition.column(Ant$Table.TYPE).eq("worker"),
      Condition.column(Ant$Table.ISMALE).eq(false))),
  transactionListener);

```

There are many kinds of queries that are supported in DBFlow including:
  1. SELECT
  2. UPDATE
  3. DELETE
  4. JOIN

### SELECT Statements and Retrieval Methods

A `SELECT` statement retrieves data from the database. We retrieve data via
  1. Normal `Select` on the main thread
  2. Running a `Transaction` using the `TransactionManager` (recommended for large
  queries).

```java

// Query a List
new Select().from(SomeTable.class).queryList();
new Select().from(SomeTable.class).where(conditions).queryList();

// Query Single Model
new Select().from(SomeTable.class).querySingle();
new Select().from(SomeTable.class).where(conditions).querySingle();

// Query a Table List and Cursor List
new Select().from(SomeTable.class).where(conditions).queryTableList();
new Select().from(SomeTable.class).where(conditions).queryCursorList();

// SELECT methods
new Select().distinct().from(table).queryList();
new Select().all().from(table).queryList();
new Select().avg(SomeTable$Table.SALARY).from(SomeTable.class).queryList();
new Select().method(SomeTable$Table.SALARY, "MAX").from(SomeTable.class).queryList();

// Transact a query on the DBTransactionQueue
TransactionManager.getInstance().addTransaction(
  new SelectListTransaction<>(new Select().from(SomeTable.class).where(conditions),
  new TransactionListenerAdapter<List<SomeTable>>() {
    @Override
    public void onResultReceived(List<SomeTable> someObjectList) {
      // retrieved here
});

// Selects Count of Rows for the SELECT statment
long count = new Select().count(SomeTable.class).where(conditions).count();


```

#### Order By

```java

// true for 'ASC', false for 'DESC'
new Select()
  .from(table)
  .where()
  .orderBy(true, Customer$Table.CUSTOMER_ID)
  .queryList();

// string order by as well
new Select()
  .from(table)
  .where()
  .orderBy(Customer$Table.CUSTOMER_ID + " ASC")
  .queryList();


```

#### Group By

```java
new Select().from(table).where()
  .groupBy(new QueryBuilder()
    .appendQuotedArray(Customer$Table.CUSTOMER_ID, Customer$Table.CUSTOMER_NAME))
  .queryList();
```

#### HAVING

```java
new Select().from(table).where()
  .groupBy(new QueryBuilder().appendQuotedArray(Customer$Table.CUSTOMER_ID, Customer$Table.CUSTOMER_NAME))
  .having(Condition.column(Customer$Table.CUSTOMER_ID).greaterThan(2))
  .queryList();
```

#### LIMIT + OFFSET

```java
new Select().from(table).where()
  .limit(3)
  .offset(2)
  .queryList();
```

### UPDATE statements

There are two ways of updating data in the database:
  1. Using the ```Update``` class
  2. Running a `Transaction` using the `TransactionManager` (recommended for thread-safety,
  however seeing changes are async).

In this section we will describe bulk updating data from the database.

From our earlier example on ants, we want to change all of our current male "worker" ants
into "other" ants because they became lazy and do not work anymore.

Using native SQL:

```sql

UPDATE Ant SET type = 'other' WHERE male = 1 AND type = 'worker';

```

Using DBFlow:

```java

// Native SQL wrapper
Update<Ant> update = new Update().table(Ant.class).set(Condition.column(Ant$Table.TYPE).eq("other"))
  .where(Condition.column(Ant$Table.TYPE).is("worker"))
  .and(Condition.column(Ant$Table.ISMALE).is(true));
update.queryClose();

// TransactionManager (more methods similar to this one)
TransactionManager.getInstance().addTransaction(new UpdateTransaction<>(DBTransactionInfo.create(BaseTransaction.PRIORITY_UI), update);

```

### DELETE statements

```java

// Delete a whole table
Delete.table(MyTable.class, conditions);

// Delete multiple instantly
Delete.tables(MyTable1.class, MyTable2.class);

// Delete using query
new Delete()
  .from(MyTable.class)
  .where(Condition.column(DeviceObject$Table.CARRIER).is("T-MOBILE"))
    .and(Condition.column(DeviceObject$Table.DEVICE).is("Samsung-Galaxy-S5")).query();

```

### JOIN statements

```JOIN``` statements are great for combining many-to-many relationships.

For example we have a table named ```Customer``` and another named ```Reservations```.

```java

// use the different QueryModel (instead of Table) if the result cannot be applied to existing Model classes.
List<CustomTable> customers = new Select()
  .from(Customer.class).as("C")
  .join(Reservations.class, JoinType.INNER).as("R")
    .on(Condition.column(ColumnAlias.columnTable("C", Customer$Table.CUSTOMER_ID)
      .eq(ColumnAlias.columnTable("R", Reservations$Table.CUSTOMER_ID)).queryCustomList(CustomTable.class);

```
