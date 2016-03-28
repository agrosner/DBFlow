# SQLite Wrapper Language

DBFlow's SQLite wrapper language attempts to make it as easy as possible to
write queries, execute statements, and more.

We will attempt to make this doc comprehensive, but reference the SQLite language
for how to formulate queries, as DBFlow follows it as much as possible.

## SELECT

The way to query data, `SELECT` are started by:

```java

SQLite.select().from(SomeTable.class)

```

### Projections

By default if no parameters are specified in the `select()` query, we use the `*` wildcard qualifier,
meaning all columns are returned in the results.

To specify individual columns, you _must_ use `Property` variables.
These get generated when you annotate your `Model` with columns, or created manually.

```java

SQLite.select(Player_Table.name, Player_Table.position)
    .from(Player.class)

```

To specify methods such as `COUNT()` or `SUM()` (static import on `Method`):


```java

SQLite.select(count(Employee_Table.name), sum(Employee_Table.salary))
    .from(Employee.class)

```

Translates to:

```sqlite

SELECT COUNT(`name`), SUM(`salary`) FROM `Employee`;

```

There are more handy methods in `Method`.

### Conditions

DBFlow supports many kinds of conditions. They are formulated into a `ConditionGroup`,
which represent a set of `SQLCondition` subclasses combined into a SQLite conditional piece.
`Property` translate themselves into `SQLCondition` via their conditional methods such as
`eq()`, `lessThan()`, `greaterThan()`, `between()`, `in()`, etc.

They make it very easy to construct concise and meaningful queries:

```java

int taxBracketCount = SQLite.select(count(Employee_Table.name))
    .from(Employee.class)
    .where(Employee_Table.salary.lessThan(150000))
    .and(Employee_Table.salary.greaterThan(80000))
    .count();

```

Translates to:

```sqlite

SELECT COUNT(`name`) FROM `Employee` WHERE `salary`<150000 AND `salary`>80000;

```

DBFlow supports `IN`/`NOT IN` and `BETWEEN` as well.

A more comprehensive list of conditional operations DBFlow supports and what they translate to:

  1. is(), eq() -> =
  2. isNot(), notEq() -> !=
  3. isNull() -> IS NULL / isNotNull() -> IS NOT NULL
  4. like(), glob()
  5. greaterThan(), greaterThanOrEqual(), lessThan(), lessThanOrEqual()
  6. between() -> BETWEEN
  7. in(), notIn()

#### Nested Conditions

To create nested conditions (in parenthesis more often than not), just include
a `ConditionGroup` as a `SQLCondition` in a query:


```java

SQLite.select()
  .from(Location.class)
  .where(Location_Table.latitude.eq(home.getLatitude()))
  .and(ConditionGroup.clause()
      .and(Location_Table.latitude
        .minus(PropertyFactory.from(home.getLatitude())
        .eq(1000L))))

```

Translates to:

```sqlite

SELECT * FROM `Location` WHERE `latitude`=45.05 AND (`latitude` - 45.05) = 1000

```

#### Nested Queries

To create a nested query simply include it as a `Property` via `PropertyFactory.from(BaseQueriable)`:

```java

.where(PropertyFactory.from(SQLite.select().from(...).where(...))

```

This appends a `WHERE (SELECT * FROM {table} )` to the query.

### JOINS

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
```

### Order By

```java

// true for 'ASC', false for 'DESC'
SQLite.select()
  .from(table)
  .where()
  .orderBy(Customer_Table.customer_id, true)

  SQLite.select()
    .from(table)
    .where()
    .orderBy(Customer_Table.customer_id, true)
    .orderBy(Customer_Table.name, false)
```

### Group By

```java
SQLite.select()
  .from(table)
  .groupBy(Customer_Table.customer_id, Customer_Table.customer_name)
```

### HAVING

```java
SQLite.select()
  .from(table)
  .groupBy(Customer_Table.customer_id, Customer_Table.customer_name))
  .having(Customer_Table.customer_id.greaterThan(2))
```

### LIMIT + OFFSET

```java
SQLite.select()
  .from(table)
  .limit(3)
  .offset(2)
```

## UPDATE

DBFlow supports two kind of UPDATE:
  1. `Model.update()`
  2. `SQLite.update()`

For simple `UPDATE` for a single or few, concrete set of `Model` stick with (1).
For powerful multiple `Model` update that can span many rows, use (2). In this
section we speak on (2). **Note:** using model caching, you'll need to clear it out
post an operation from (2).


```sql

UPDATE Ant SET type = 'other' WHERE male = 1 AND type = 'worker';
```

Using DBFlow:

```java

// Native SQL wrapper
SQLite.update(Ant.class)
  .set(Ant_Table.type.eq("other"))
  .where(Ant_Table.type.is("worker"))
    .and(Ant_Table.isMale.is(true))
    .async()
    .execute(); // non-UI blocking
```

The `Set` part of the `Update` supports different kinds of values:
  1. `ContentValues` -> converts to key/value as a `SQLCondition` of `is()`/`eq()`
  2. `SQLCondition`, which are grouped together as part of the `SET` statement.

## DELETE

`DELETE` queries in DBFlow are similiar to `Update` in that we have two kinds:

  1. `Model.delete()`
  2. `SQLite.delete()`

For simple `DELETE` for a single or few, concrete set of `Model` stick with (1).
For powerful multiple `Model` deletion that can span many rows, use (2). In this
section we speak on (2). **Note:** using model caching, you'll need to clear it out
post an operation from (2).


```java

// Delete a whole table
Delete.table(MyTable.class, conditions);

// Delete multiple instantly
Delete.tables(MyTable1.class, MyTable2.class);

// Delete using query
SQLite.delete(MyTable.class)
  .where(DeviceObject_Table.carrier.is("T-MOBILE"))
    .and(DeviceObject_Table.device.is("Samsung-Galaxy-S5"))
  .async()
  .execute();
```

## INSERT

`INSERT` queries in DBFlow are also similiar to `Update` and `Delete` in that we
have two kinds:

  1. `Model.insert()`
  2. `SQLite.insert()`

For simple `INSERT` for a single or few, concrete set of `Model` stick with (1).
For powerful multiple `Model` insertion that can span many rows, use (2). In this
section we speak on (2). **Note:** using model caching, you'll need to clear it out
post an operation from (2).

```java

// columns + values separately
SQLite.insert(SomeTable.class)
  .columns(SomeTable_Table.name, SomeTable_Table.phoneNumber)
  .values("Default", "5555555")
  .async()
  .execute()

// or combine into conditions
  SQLite.insert(SomeTable.class)
    .columnValues(SomeTable_Table.name.eq("Default"),
     SomeTable_Table.phoneNumber.eq("5555555"))
    .async()
    .execute()

```
