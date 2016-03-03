# SQL Statements Using the Wrapper Classes
In SQL with Android, writing SQL is not that _fun_, so to make it easy and useful,  this library provides a comprehensive set of SQLite statement wrappers that attempt to make the java code as similar to SQLite as possible.

In the first section I describe how using the wrapper classes drastically simplify code writing.

## Example
For example, we want to find all ants from `Ant` that are of type "worker" and are female. Writing the SQL statement is easy enough:

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

In short, we want our code to be maintainable, short, reusable, and still remain expressive of what actually is happening. In this library, this query becomes very easy:

```java

// main thread retrieval
List<Ant> devices = SQLite.select().from(Ant.class)
  .where(Ant_Table.type.eq("worker"))
  .and(Ant_Table.isMale.eq(false)).queryList();

// Async Transaction Queue Retrieval (Recommended for large queries)
  SQLite.select()
  .from(DeviceObject.class)
  .where(Ant_Table.type.eq("worker"))
  .and(Ant_Table.isMale.eq(false))
  .async().queryList(transactionListener);
```

There are many kinds of queries that are supported in DBFlow including:

1. SELECT
2. UPDATE
3. INSERT
4. DELETE
5. JOIN

## SELECT Statements and Retrieval Methods
A `SELECT` statement retrieves data from the database. We retrieve data via

1. Normal `Select` on the main thread
2. Running a `Transaction` using the `TransactionManager` (recommended for large
3. queries).

```java

// Query a List
SQLite.select().from(SomeTable.class).queryList();
SQLite.select().from(SomeTable.class).where(conditions).queryList();

// Query Single Model
SQLite.select().from(SomeTable.class).querySingle();
SQLite.select().from(SomeTable.class).where(conditions).querySingle();

// Query a Table List and Cursor List
SQLite.select().from(SomeTable.class).where(conditions).queryTableList();
SQLite.select().from(SomeTable.class).where(conditions).queryCursorList();

// Query into a ModelContainer!
SQLite.select().from(SomeTable.class).where(conditions).queryModelContainer(new MapModelContainer<>(SomeTable.class));

// SELECT methods
SQLite.select().distinct().from(table).queryList();
SQLite.select().from(table).queryList();
SQLite.select(Method.avg(SomeTable_Table.salary))
  .from(SomeTable.class).queryList();
SQLite.select(Method.max(SomeTable_Table.salary))
  .from(SomeTable.class).queryList();

// Transact a query on the DBTransactionQueue
TransactionManager.getInstance().addTransaction(
  new SelectListTransaction<>(new Select().from(SomeTable.class).where(conditions),
  new TransactionListenerAdapter<List<SomeTable>>() {
    @Override
    public void onResultReceived(List<SomeTable> someObjectList) {
      // retrieved here
});

// Selects Count of Rows for the SELECT statment
long count = SQLite.selectCountOf()
  .where(conditions).count();
```

### Order By

```java

// true for 'ASC', false for 'DESC'
SQLite.select()
  .from(table)
  .where()
  .orderBy(Customer_Table.customer_id, true)
  .queryList();

  SQLite.select()
    .from(table)
    .where()
    .orderBy(Customer_Table.customer_id, true)
    .orderBy(Customer_Table.name, false)
    .queryList();
```

### Group By

```java
SQLite.select()
  .from(table)
  .groupBy(Customer_Table.customer_id, Customer_Table.customer_name)
  .queryList();
```

### HAVING

```java
SQLite.select()
  .from(table)
  .groupBy(Customer_Table.customer_id, Customer_Table.customer_name))
  .having(Customer_Table.customer_id.greaterThan(2))
  .queryList();
```

### LIMIT + OFFSET

```java
SQLite.select()
  .from(table)
  .limit(3)
  .offset(2)
  .queryList();
```

## UPDATE statements
There are two ways of updating data in the database:

1. Calling `SQLite.update()`or Using the `Update` class
2. Running a `Transaction` using the `TransactionManager` (recommended for thread-safety, however seeing changes are async).

In this section we will describe bulk updating data from the database.

From our earlier example on ants, we want to change all of our current male "worker" ants into "other" ants because they became lazy and do not work anymore.

Using native SQL:

```sql

UPDATE Ant SET type = 'other' WHERE male = 1 AND type = 'worker';
```

Using DBFlow:

```java

// Native SQL wrapper
Where<Ant> update = SQLite.update(Ant.class)
  .set(Ant_Table.type.eq("other"))
  .where(Ant_Table.type.is("worker"))
    .and(Ant_Table.isMale.is(true));
update.queryClose();

// TransactionManager (more methods similar to this one)
TransactionManager.getInstance().addTransaction(new QueryTransaction(DBTransactionInfo.create(BaseTransaction.PRIORITY_UI), update);
```

## DELETE statements

```java

// Delete a whole table
Delete.table(MyTable.class, conditions);

// Delete multiple instantly
Delete.tables(MyTable1.class, MyTable2.class);

// Delete using query
SQLite.delete(MyTable.class)
  .where(DeviceObject_Table.carrier.is("T-MOBILE"))
    .and(DeviceObject_Table.device.is("Samsung-Galaxy-S5"))
  .query();
```

## JOIN statements
For reference, ([JOIN examples](http://www.tutorialspoint.com/sqlite/sqlite_using_joins.htm)).

`JOIN` statements are great for combining many-to-many relationships.
If your query returns non-table fields and cannot map to an existing object,
see about [query models](usage/QueryModels.md)

For example we have a table named `Customer` and another named `Reservations`.

```SQL
SELECT FROM `Customer` AS `C` INNER JOIN `Reservations` AS `R` ON `C`.`customerId`=`R`.`customerId`
```

```java
// use the different QueryModel (instead of Table) if the result cannot be applied to existing Model classes.
List<CustomTable> customers = new Select()   
  .from(Customer.class).as("C")   
  .join(Reservations.class, JoinType.INNER).as("R")    
  .on(Customer_Table.customerId
      .withTable(new NameAlias("C"))
    .eq(Reservations_Table.customerId.withTable("R"))
    .queryCustomList(CustomTable.class);
```

The `IProperty.withTable()` method will prepend a `NameAlias` or the `Table` alias  to the `IProperty` in the query, convenient for JOIN queries:

```sqlite
SELECT EMP_ID, NAME, DEPT FROM COMPANY LEFT OUTER JOIN DEPARTMENT
      ON COMPANY.ID = DEPARTMENT.EMP_ID
```

in DBFlow:

```java
SQLite.select(Company_Table.EMP_ID, Company_Table.DEPT)
  .from(Company.class)
  .leftOuterJoin(Department.class)
  .on(Company_Table.ID.withTable().eq(Department_Table.EMP_ID.withTable()))
  .queryList();
```
