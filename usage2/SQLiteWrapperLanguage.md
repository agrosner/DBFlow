# SQLite Wrapper Language

DBFlow's SQLite wrapper language attempts to make it as easy as possible to
write queries, execute statements, and more.

We will attempt to make this doc comprehensive, but reference the SQLite language
for how to formulate queries, as DBFlow follows it as much as possible.

## SELECT

The way to query data, `SELECT` are started by:

```kotlin
select from SomeTable::class
```

### Projections

By default if no parameters are specified in the `select()` query, we use the `*` wildcard qualifier,
meaning all columns are returned in the results.

To specify individual columns, you _must_ use `Property` variables.
These get generated when you annotate your `Model` with columns, or created manually.

```kotlin

select(Player_Table.name, Player_Table.position)
   from Player::class

```

To specify methods such as `COUNT()` or `SUM()` (static import on `Method`):


```kotlin

select(count(Employee_Table.name), sum(Employee_Table.salary))
    from Employee::class

```

Translates to:

```sqlite

SELECT COUNT(`name`), SUM(`salary`) FROM `Employee`;

```

There are more handy methods in `Method`.

### Operators

DBFlow supports many kinds of operations. They are formulated into a `OperatorGroup`,
which represent a set of `SQLOperator` subclasses combined into a SQLite conditional piece.
`Property` translate themselves into `SQLOperator` via their conditional methods such as
`eq()`, `lessThan()`, `greaterThan()`, `between()`, `in()`, etc.

They make it very easy to construct concise and meaningful queries:

```kotlin
val taxBracketCount = (select(count(Employee_Table.name))
    from Employee::class
    where Employee_Table.salary.lessThan(150000)
    and Employee_Table.salary.greaterThan(80000))
    .count(database)
```

Translates to:

```sqlite

SELECT COUNT(`name`) FROM `Employee` WHERE `salary`<150000 AND `salary`>80000;

```

DBFlow supports `IN`/`NOT IN` and `BETWEEN` as well.

A more comprehensive list of operations DBFlow supports and what they translate to:

  1. is(), eq() -> =
  2. isNot(), notEq() -> !=
  3. isNull() -> IS NULL / isNotNull() -> IS NOT NULL
  4. like(), glob()
  5. greaterThan(), greaterThanOrEqual(), lessThan(), lessThanOrEqual()
  6. between() -> BETWEEN
  7. in(), notIn()

#### Nested Conditions

To create nested conditions (in parenthesis more often than not), just include
an `OperatorGroup` as a `SQLOperator` in a query:


```kotlin
(select from Location::class
  where Location_Table.latitude.eq(home.latitude)
  and (Location_Table.latitude
         - home.latitude) eq 1000L
 )
```

Translates to:

```sqlite

SELECT * FROM `Location` WHERE `latitude`=45.05 AND (`latitude` - 45.05) = 1000

```

#### Nested Queries

To create a nested query simply include a query as a `Property` via `(query).property`:

```kotlin
.where((select from(...) where(...)).property)
```

This appends a `WHERE (SELECT * FROM {table} )` to the query.

### JOINS

For reference, ([JOIN examples](http://www.tutorialspoint.com/sqlite/sqlite_using_joins.htm)).

`JOIN` statements are great for combining many-to-many relationships.
If your query returns non-table fields and cannot map to an existing object,
see about [query models](QueryModels.md)

For example we have a table named `Customer` and another named `Reservations`.

```SQL
SELECT FROM `Customer` AS `C` INNER JOIN `Reservations` AS `R` ON `C`.`customerId`=`R`.`customerId`
```

```kotlin
// use the different QueryModel (instead of Table) if the result cannot be applied to existing Model classes.
val customers = (select from Customer::class).as("C")   
  innerJoin<Reservations.class>().as("R")    
   on(Customer_Table.customerId
      .withTable("C".nameAlias)
     eq Reservations_Table.customerId.withTable("R"))
    .customList<CustomTable>());
```

The `IProperty.withTable()` method will prepend a `NameAlias` or the `Table` alias  to the `IProperty` in the query, convenient for JOIN queries:

```sqlite
SELECT EMP_ID, NAME, DEPT FROM COMPANY LEFT OUTER JOIN DEPARTMENT
      ON COMPANY.ID = DEPARTMENT.EMP_ID
```

in DBFlow:

```kotlin
(select(Company_Table.EMP_ID, Company_Table.DEPT)
  from Company::class
  leftOuterJoin<Department>()
  .on(Company_Table.ID.withTable().eq(Department_Table.EMP_ID.withTable()))
)
```

### Order By

```kotlin

// true for 'ASC', false for 'DESC'. ASC is default.
(select from table
  orderBy(Customer_Table.customer_id)

  (select from table
    orderBy(Customer_Table.customer_id, ascending = true)
    orderBy(Customer_Table.name, ascending = false))
```

### Group By

```kotlin
(select from table)
  .groupBy(Customer_Table.customer_id, Customer_Table.customer_name)
```

### HAVING

```kotlin
(select from table)
  .groupBy(Customer_Table.customer_id, Customer_Table.customer_name))
  .having(Customer_Table.customer_id.greaterThan(2))
```

### LIMIT + OFFSET

```kotlin
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
section we speak on (2). **Note:** if using model caching, you'll need to clear it out
post an operation from (2).


```sql

UPDATE Ant SET type = 'other' WHERE male = 1 AND type = 'worker';
```

Using DBFlow:

```kotlin

// Native SQL wrapper
database.beginTransactionAsync { db -> (update<Ant>()
   set Ant_Table.type.eq("other")
   where Ant_Table.type.is("worker")
   and Ant_Table.isMale.is(true))
   .executeUpdateDelete(db)
  }.execute { _, count -> }; // non-UI blocking
```

The `Set` part of the `Update` supports different kinds of values:
  1. `ContentValues` -> converts to key/value as a `SQLOperator` of `is()`/`eq()`
  2. `SQLOperator`, which are grouped together as part of the `SET` statement.

## DELETE

`DELETE` queries in DBFlow are similiar to `Update` in that we have two kinds:

  1. `Model.delete()`
  2. `SQLite.delete()`

For simple `DELETE` for a single or few, concrete set of `Model` stick with (1).
For powerful multiple `Model` deletion that can span many rows, use (2). In this
section we speak on (2). **Note:** if using model caching, you'll need to clear it out
post an operation from (2).


```kotlin

// Delete a whole table
delete<MyTable>().execute(database)

// Delete using query
database.beginTransactionAsync { db -> delete<MyTable>()
   where DeviceObject_Table.carrier.is("T-MOBILE")
   and DeviceObject_Table.device.is("Samsung-Galaxy-S5"))
   .executeUpdateDelete(db)
 }.execute { _, count -> };
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

```kotlin

// columns + values via pairs
database.beginTransactionAsync { db ->
   (insert<SomeTable>(SomeTable_Table.name to "Default",
    MSomeTable_Table.phoneNumber to "5555555")
    .executeInsert(db)
}.execute()

// or combine into Operators
database.beginTransactionAsync { db ->
   (insert<SomeTable>(SomeTable_Table.name eq "Default",
    MSomeTable_Table.phoneNumber eq "5555555")
    .executeInsert(db)
}.execute()
```

`INSERT` supports inserting multiple rows as well.

```kotlin

// columns + values separately
database.beginTransactionAsync { db ->
  (insert<SomeTable>(SomeTable_Table.name, SomeTable_Table.phoneNumber)
  .values("Default1", "5555555")
  .values("Default2", "6666666"))
  .executeInsert(db)
}.execute()

// or combine into Operators
database.beginTransactionAsync { db ->
  (insert<SomeTable>(SomeTable_Table.name.eq("Default1"),
     SomeTable_Table.phoneNumber.eq("5555555"))
    .columnValues(SomeTable_Table.name.eq("Default2"),
     SomeTable_Table.phoneNumber.eq("6666666")))
     .executeInsert(db)
   }.execute()

```

## Trigger

Triggers enable SQLite-level listener operations that perform some operation, modification,
or action to run when a specific database event occurs. [See](https://www.sqlite.org/lang_createtrigger.html) for more documentation on its usage.

```kotlin

*createTrigger("SomeTrigger")
    .after() insertOn<ConditionModel>())
    .begin(update<TestUpdateModel>()
            .set(TestUpdateModel_Table.value.is("Fired"))))
            .enable(); // enables the trigger if it does not exist, so subsequent calls are OK

```

## Case

The SQLite `CASE` operator is very useful to evaluate a set of conditions and "map" them
to a certain value that returns in a SELECT query.

We have two kinds of case:
1. Simple
2. Searched

The simple CASE query in DBFlow:

```kotlin

select(CaseModel_Table.customerId,
        CaseModel_Table.firstName,
        CaseModel_Table.lastName,
        (case(CaseModel_Table.country)
                 whenever "USA"
                 then "Domestic"
                 `else` "Foreign")
                .end("CustomerGroup"))
  from<CaseModel>()

```

The CASE is returned as `CustomerGroup` with the valyes of "Domestic" if the country is from
the 'USA' otherwise we mark the value as "Foreign". These appear alongside the results
set from the SELECT.

The search CASE is a little more complicated in that each `when()` statement
represents a `SQLOperator`, which return a `boolean` expression:

```kotlin

select(CaseModel_Table.customerId,
    CaseModel_Table.firstName,
    CaseModel_Table.lastName,
    caseWhen(CaseModel_Table.country.eq("USA"))
             then "Domestic"
             `else` "Foreign")
     .end("CustomerGroup"))
 from<CaseModel>()
```
