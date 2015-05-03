# DBFlow 2.0 Migration and new features Guide

This guide provides a handy way to summarize the changes that you must make when upgrading
to 2.x from 1.8.1 and previous.

_Please note there many breaking changes in this version_. That said, the new version
brings much more consistency, bug fixes, and a few notable new features.

## Breaking Changes

In this guide we break up each kind of breaking changes into sections:
  1. Models
  2. Queries
  3. Transaction Manager
  4. Notable removal of deprecated methods and classes
  5. Generated Code


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

### Queries

#### FlowTableList

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

This release comes with some _very_ notable features such as:
  1. Subquery support
  2. Better Column name and alias support
  3. One-To-Many annotation support
  4. Better caching support with other column types
  5. Non-table Models, providing support for non-uniform or standard queries
  6. Custom SQLite Open Helpers
