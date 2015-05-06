# DBFlow 2.0 Migration and new features Guide

This guide provides a handy way to summarize the changes that you must make when upgrading
to 2.x from 1.8.1 and previous.

_Please note there many breaking changes in this version_. That said, the new version
brings much more consistency, bug fixes, and a few notable new features.

**New Features**: This release comes with some _very_ notable features such as:
  1. Subquery + Nested Condition support
  2. Better Column name and alias support
  3. Private columns support
  4. One-To-Many annotation support
  5. Better caching support with other column types
  6. Non-table Models, providing support for non-uniform or standard queries
  7. Custom SQLite Open Helpers
  8. Content Observer improvements.


**Breaking Changes**: In this guide we break up each kind of major breaking changes into sections:
  1. Models
  2. Queries
  3. Transaction Manager
  4. Notable removal of deprecated methods and classes
  5. Generated Code

## Breaking Changes

### Models

The `Model` interface has been simplified from caring about `async` methods.
All async operations move into its own `AsyncModel`.

Previously:

```java

model.save(false);
model.update(false);
// etc.. all "async" params move into AsyncModel class

```

Becomes:

```java

model.async().withListener(onModelChangedListener).save();
model.async().withListener(onModelChangedListener).update();

```

This allows you to update a `RecyclerView.Adapter` or something else when the
transaction completes. The new `OnModelChangedListener` provides a simplified callback
on the UI thread when the transaction completes.


#### Annotations

To simplify the `@Column` annotation, we broke apart the primary key, foreign key,
not null, and unique groups into their own separate annotations. Originally, it made sense
to put it all in one annotation, but as features were added, it became apparent the
annotation allowed you to use it in unintended ways.

So these annotations (and their corresponding methods moved into):
  1. `@PrimaryKey`
  2. `@ForeignKey`
  3. `@Unique`
  4. `@NotNull`

_Note: all the method names are the same from what was previously in `Model`,
so just copy and paste into new annotation!_.

### Queries

#### Condition

`Between`and `In` no longer subclass `Condition` (to narrow down methods we care about with each).
These two and `Condition` now subclass `BaseCondition` which implements `SQLCondition`
that provides us with the ability to nest `SQLCondition` using the `CombinedCondition` class.

#### FlowTableList -> FlowQueryList

The `FlowTableList` is renamed to `FlowQueryList`, since it's used for not only for tables,
but also queries.

#### Transactions

Queries no longer allow you to `transactQuery()` asynchronously. Instead
call `async()` on a `ModelQueriable` statement, and it returns an `AsyncQuery`
object that provides a set of methods to asynchronously query from the DB.

Previously:

```java

new Select().from(SomeTable.class).where(conditions).transactQuery(transactionListener);

```

Becomes:

```java

new Select().from(SomeTable.class).where(conditions).async().querySingle(transactionListener);

```

#### New Methods

Also now the `ModelQueriable` interface provides you with methods to recall
different `Model` classes for a query that does not necessarily pertain to it.
More is on it in New Features.

```java

// attempt to load the resultant cursor into the corresponding RetrievalAdapter for another table.
List<AnotherTable> list = new Select(ColumnAlias.column(MyTable$Table.COLUMN).as("column2"))
  .from(MyTable.class).queryCustomList(AnotherTable.class);

```

#### Constructors

A few classes for SQLite statements change their constructor from `public`
to package private to encourage the use of `static` creators.

Previously:

```java

new Insert(table);

new Trigger(triggerName);

```

Becomes:

```java

Insert.into(table);

Trigger.create(triggerName);

```

### Transaction Manager

The `TransactionManager` methods all but have been removed except for a few notable
methods. One such method is `addTransaction()`, which becomes the default way
to run code on the `TransactionManager`. As a result, the `TableTransactionManager` is
now removed from the API completely.

Previously:

```java

TransactionManager.getInstance().save(ProcessModelInfo.withModels(models));

```

Becomes:

```java

TransactionManager.getInstance()
  .addTransaction(new SaveModelListTransaction<>(ProcessModelInfo.withModels(models)));

```

This is to encourage the use of the transaction classes and to simplify the API.

### Deprecated Methods and classes

The list follows:
  1. `SaveMode` in `SqlUtils` and `sync` have been replaced with their corresponding
  methods such as `update`, `insert`, and `save`.
  2. `ResultReceiver` has been removed for `TransactionListener` and `TransactionListenerAdapter`
  3. The methods in `ModelAdapter` and other adapters that corresponded to `sync`


## New Features

### Subquery + Nested Conditions Support

Subqueries are here!

To run a subquery, simply:

```java
List<SomeTable> list = new Select()
                        .from(SomeTable.class)
                        .where(Condition.column(SomeTable$Table.COLUMN)
                          .greaterThan(new Select()
                                    .from(SomeTable.class)
                                    .where(Condition.columnRaw(SomeTable$Table.ANOTHER_COLUMN)
                                                       .eq(SomeTable$Table.ANOTHER_COLUMN_2)))
                        .queryList();

```

By passing a `Where` object into any of the `Condition` methods, the SQL language
knows that it's a subquery and appends it appropriately.

Nested Conditions are here!

To nest conditions simply use the `CombinedCondition` class:

```java

new Select().from(SomeTable.class)
  .where(CombinedCondition
    .begin(Condition.column(SomeTable$Table.COLUMN).eq(5))
    .and(Condition.column(SomeTable$Table.ANOTHER_COLUMN).eq(6)))
    .and(CombinedCondition.begin(SomeTable$Table.NESTED_COLUMN).eq("Test")));

```

The example produces the query:

```SQL

SELECT * FROM `SomeTable` WHERE (`column`=5 AND `another_column`=6) AND `nested_column`='Test'

```

Also you can nest a nested CombinedCondition!

```java

CombinedCondition
  .begin(CombinedCondition
    .begin(CombinedCondition
      .begin(Condition.column("salary").greaterThan(15000))
      .and(Condition.column("title").is("worker")))
  .and(CombinedCondition.begin(Condition.column("fuss").is(false))));

```

Produces:

```java

((`salary`>15000 AND `title`='worker') AND (`fuss`=0))

```

### Better Column name and alias support!

Welcome to `ColumnAlias`! It's a handy class that enables you to wrap logic in specifying
column names with:
  1. Proper table appending. I.e.: `MyTable`.`columnName`

  ```SQL

  SELECT * FROM `SomeTable`
  WHERE `SomeTable`.`column`='test'

  ```

  ```java

  new Select().from(SomeTable.class)
    .where(Condition.column(ColumnAlias.columnWithTable(SomeTable$Table.TABLE_NAME, SomeTable$Table.COLUMN).eq("test")));

  ```

  2. Using the `AS` operator:

  ```SQL

  SELECT `name` AS `newName`
  ...

  ```

  ```java

  new Select(ColumnAlias.column(SomeTable$Table.NAME).as("newName"))

  ```

  Its available in all `Select`, `Condition`, `Index`, and `Trigger` classes for
  specifying column names.

### Private Column Support

Now in this release, you can specify private `@Column` fields. Simply specify
a getter and setter for the field:

```java

@Table(databaseName = TestDatabase.NAME)
public class PrivateModelTest extends BaseModel {

    @Column
    @PrimaryKey
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


```

And the `$Adapter` classes know to use the corresponding methods!

### One-to-Many annotation support

The `OneToMany` annotation is used in instances where you have a relationship to
manage yourself. For example

```java

@Table(databaseName = TestDatabase.NAME)
public class OneToManyModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;

    List<TestModel2> orders;

    public List<TestModel2> getOrders() {
        if (orders == null) {
            orders = new Select().from(TestModel2.class)
                    .where(Condition.column(TestModel2$Table.MODEL_ORDER).greaterThan(3))
                    .queryList();
        }
        return orders;
    }

}

```

In this scenario we want the `Model` to load the relationship on load from the
database. Also, when we destroy this parent, we want to corresponding children to get destroyed
as well. Instead of either forgetting to do so and ultimately "orphaning" the children,
or having to manage the code of doing it yourself, which can lead to errors, let the
`@OneToMany` handle it for you!
The annotation comes with these restrictions:
  1. The method must follow this pattern: `getFieldName` for `fieldName`, otherwise you can specify
 `variableName()` to connect the method to the variable.
  2. The method must be accessible to the annotation processor, so either `public`
  or package private.
  3. The method must return a `List` of `Model` and the variable must be of the same type.

```java

@Table(databaseName = TestDatabase.NAME)
public class OneToManyModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;

    List<TestModel2> orders;

    @OneToMany(methods = {OneToMany.Method.ALL})
    public List<TestModel2> getOrders() {
        if (orders == null) {
            orders = new Select().from(TestModel2.class)
                    .where(Condition.column(TestModel2$Table.MODEL_ORDER).greaterThan(3))
                    .queryList();
        }
        return orders;
    }

}

```



There are a few options this library supports:
  1. `LOAD`: loads the relationship out of the database. (this is the default)
  2. `DELETE`: deletes all of the related models when this model is deleted.
  3. `SAVE`: save all of the models returned from the `OneToMany`
  4. `ALL`: shorthand to support all kinds.

### Better Caching Support

Previously with `ModelCache` and `CacheableModel` items, you could only use an
`int` or `long` primary key / primary key auto-increment. With this update,
all primary key types are supported as long as:
  1. Doesn't require a `TypeConverter`
  2. One, single primary key

### Non-Table Models and better support for non-uniform table queries

In `2.0.0`, you can now create `Model` classes that do not pertain to a specific
table, but still generating `Cursor` loading methods!
Just:
  1. Use the `QueryModel` annotation
  2. extend `BaseQueryModel`
  3. Specify only `@Column`


```java

@QueryModel(databaseName = TestDatabase.NAME)
public class TestQueryModel extends BaseQueryModel {
...
}

```

To use non-uniform queries (with this example):

```java

List<TestQueryModel> list = new Select(ColumnAlias.column(SalaryModel$Table.DEPARTMENT),
                                                    ColumnAlias.column(SalaryModel$Table.SALARY).as("average_salary"),
                                                    ColumnAlias.column(SalaryModel$Table.NAME).as("newName"))
                                            .from(SalaryModel.class).where().limit(1).groupBy(SalaryModel$Table.DEPARTMENT).queryCustomList(TestQueryModel.class);

```

### Custom FlowSQLiteOpenHelper

In better to support other systems such as SQLCipher, I have opened up support
for extending the `FlowSQLiteOpenHelper`. The support is very basic and must:

  1. Contain a Constructor with these parameters exactly: `BaseDatabaseDefinition`, `DatabaseHelperListener`
  2. Be `public`

To use it, specify in your `@Database` class:

```java


@Database(name = HelperDatabase.NAME, version = HelperDatabase.VERSION,
          sqlHelperClass = CustomOpenHelper.class)
public class HelperDatabase {

    public static final String NAME = "Helper";

    public static final int VERSION = 1;
}

```

### Content Observer Improvements

In this release, we add the ability to transact `Model` changes and notify the
`FlowContentObserver` once the transactions are done.

Example:


```java

flowContentObserver.beginTransaction();

someModel.save();
// More modifications on a table for what the Flow Content Observer is registered.


// collects all unique URI and calls onChange here
flowContentObserver.endTransactionAndNotify();

```

Instead of notifying changes to the `OnModelChangeListener`s registered on the observer
for every single modification, this allows us to only care when we're done.

It also provides two options:
  1. All unique URIs such as if `Model` was saved, inserted, updated, or deleted.
  (Only appears on JellyBean and up)
  2. All unique URIs for a single table that the observer is registered for. This
  returns one `onChange()` with `CHANGED` as the action. For devices lower than JellyBean,
  only one `onChange()` is ever called.

Now the `FlowQueryList` extends `FlowContentObserver` and gets _all_ the benefits and
functionality from the `FlowContentObserver` such as transactions, notifications,
`OnModelChangeListener`, and more! It also means the list becomes self-refreshing
when `Model` on a table changes.
