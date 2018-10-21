# KotlinSupport

DBFlow supports Kotlin out of the box and is fairly easily to use and implement.

## Classes

DBFlow Classes are beautifully concise to write:

```kotlin
@Table(database = KotlinDatabase::class)
class Person(@PrimaryKey var id: Int = 0, @Column var name: String? = null)
```

Also `data` classes are supported.

```kotlin
@Table(database = KotlinTestDatabase::class)
data class Car(@PrimaryKey var id: Int = 0, @Column var name: String? = null)
```

In 4.0.0+, DBFlow contains a few extensions for Kotlin models which enable you to keep your models acting like `BaseModel`, but do not have to explicitly extend the class!

```kotlin
car.save() // extension method, optional databaseWrapper parameter.
car.insert()
car.update()
car.delete()
car.exists()
```

## Null Safety

DBFlow reflects the nullability on fields defined in their classes. If you define a `@Column` as not null, it will not assign a null value to that field in the generated java.

## Query LINQ Syntax

Kotlin has nice support for custim `infix` operators. Using this we can convert a regular, Plain old java query into a C\#-like LINQ syntax.

java:

```text
List<Result> = SQLite.select()
                .from(Result.class)
                .where(Result_Table.column.eq(6))
                .and(Result_Table.column2.in("5", "6", "9")).queryList()
```

kotlin:

```text
val results = (select
              from Result::class
              where (column eq 6)
              and (column2 `in`("5", "6", "9"))
              groupBy column).list
              // can call .result for single result
              // .hasData if it has results
              // .statement for a compiled statement
```

Enabling us to write code that is closer in syntax to SQLite!

This supported for almost any SQLite operator that this library provides including: 1. `Select` 2. `Insert` 3. `Update` 4. `Delete`

**Async Operations**: With extensions we also support `async` operations on queries:

```kotlin
// easy async list query
(select
    from Result::class
    where (column eq 6))
.async list { transaction, list ->
    // do something here
    updateUI(list)
}

// easy single result query
(select
    from Result::class
    where (column eq 6))
.async result { transaction, model ->
    // do something here
    updateUI(model)
}

val model = Result()

model.async save {
  // completed, now do something with model
}
```

### Property Extensions

With Kotlin, we can define extension methods on pretty much any class.

With this, we added methods to easily create `IProperty` from anything to make queries a little more streamlined. In this query, we also make use of the extension method for `from` to streamline the query even more.

```kotlin
var query = (select
  from TestModel::class
  where (5.property lessThan column)
  and (clause(date.property between start_date)
        and(end_date)))
```

### Query Extensions

We can easily create nested `Operator` into `OperatorGroup` also fairly easily, also other, random extensions:

```kotlin
select from SomeTable::class where (name.eq("name") and id.eq(0))

"name".op<String>() collate NOCASE

"name".nameAlias

"name".nameAlias `as` "My Name"

// query sugar

select from SomeTable::class where (name eq "name") or (id eq 0)
```

### Database Extensions

#### Process Models Asynchronously

In Java, we need to write something of the fashion:

```java
List<TestModel> items = SQLite.select()
    .from(TestModel.class)
    .queryList();

    database.beginTransactionAsync(new ProcessModelTransaction.Builder<>(
      new ProcessModel<TestModel>() {
        @Override
        public void processModel(TestModel model, DatabaseWrapper database) {

        }
      })
      .success(successCallback)
      .error(errorCallback).build()
      .execute();
```

In Kotlin, we can use a combo of DSL and extension methods to:

```kotlin
var items = (select from TestModel1::class).list

 // easily delete all these items.
 items.processInTransactionAsync { it, databaseWrapper -> it.delete(databaseWrapper) }

 // easily delete all these items with success
 items.processInTransactionAsync({ it, databaseWrapper -> it.delete(databaseWrapper) },
            success = { transaction ->
                // do something here
            })
// delete with all callbacks
items.processInTransactionAsync({ it, databaseWrapper -> it.delete(databaseWrapper) },
    success = { transaction ->
        // do something here
    },
    error = { transaction, throwable ->

    })
```

The extension method on `Collection<T>` allows you to perform this on all collections from your Table!

If you wish to easily do them _synchronously_ then use:

```kotlin
items.processInTransaction { it, databaseWrapper -> it.delete(databaseWrapper) }
```

#### Class Extensions

If you need access to the Database, ModelAdapter, etc for a specific class you can now use the following \(and more\) inline reified global functions for easy access!

```kotlin
database<MyDatabase>()

databaseForTable<TestModel>()

writableDatabaseForTable<TestModel>()

tableName<TestModel>()

modelAdapter<TestModel>()
```

Which under-the-hood call their corresponding `FlowManager` methods.

