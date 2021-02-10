---
description: >-
  This section describes how Models and tables are constructed via DBFlow. first
  let's describe how to get a database up and running.
---

# Getting Started

## Creating a Database

In DBFlow, creating a database is as simple as only a few lines of code. DBFlow supports any number of databases, however individual tables and other related files can only be associated with one database.

```kotlin
@Database(version = 1)
abstract class AppDatabase : DBFlowDatabase()
```

The default name of the database is the class name. To change it, read [here](usage/databases.md).

Writing this file generates \(by default\) a `AppDatabaseAppDatabase_Database.java` file, which contains tables, views, and more all tied to a specific database. This class is automatically placed into the main `GeneratedDatabaseHolder`, which holds potentially many databases and global `TypeConverter`. The name, `AppDatabaseAppDatabase_Database.java`, is generated via {DatabaseClassName}{DatabaseFileName}\_Database

To learn more about what you can configure in a database, read [here](usage/databases.md)

## Initialize FlowManager

DBFlow currently needs an instance of `Context` in order to use it for a few features such as reading from assets, content observing, and generating `ContentProvider`.

Initialize in your `Application` subclass. You can also initialize it from other `Context` but we always grab the `Application` `Context` \(this is done only once\).

```kotlin
class ExampleApplication : Application {

  override fun onCreate() {
    super.onCreate()
    FlowManager.init(this)
  }
}
```

By default without passing in a `DatabaseConfig`, we construct an `AndroidSQLiteOpenHelper` database instance. To learn more about what you can configure in a database, read [here](usage/databases.md), including providing own database instances.

Finally, add the custom `Application` definition to the manifest \(with the name that you chose for the class\):

```markup
<application
  android:name="{packageName}.ExampleApplication"
  ...>
</application>
```

A database within DBFlow is only initialized once you call `database<SomeDatabase>()`. If you don't want this behavior or prefer it to happen immediately, modify your `FlowConfig`:

```kotlin
override fun onCreate() {
    super.onCreate()
    FlowManager.init(FlowConfig.builder(this)
        .openDatabasesOnInit(true)
        .build())
}
```

Each `DBFlowDatabase` contains a `TransactionManager`, which runs any `async` transaction on a queue / dispatch system. If you do not like the built-in `DefaultTransactionManager`, or just want to roll your own existing system:

```kotlin
FlowManager.init(FlowConfig.builder(this)
    .database(DatabaseConfig.builder(AppDatabase::class)
            .transactionManagerCreator { db -> CustomTransactionManager(db))
          .build()))
```

You can define different kinds for each database. To read more on transactions and subclassing `BaseTransactionManager` go [here](usage/storingdata.md)

## Create Models

Creating models are as simple as defining the model class, and adding the `@Table` annotation. To read more on this, read [here](usage/models.md).

**For now**: Models must provide a default, parameterless constructor. Also, all fields must be mutable \(currently, we hope to evolve this requirement in the future\). An example:

```kotlin
@Table(database = TestDatabase::class)
class Currency(@PrimaryKey(autoincrement = true) var id: Long = 0,
               @Unique var symbol: String? = null,
               var shortName: String? = null,
               @Unique var name: String = "") // nullability of fields are respected. We will not assign a null value to this field.
```

## Build Your DAO

Set up a DAO \(Data Access Object\) to help you interact with your database. Using dependency injection and service locating, we can build better, highly testable code. While not required to use DBFlow, it is **highly** recommended utilize this approach.

With kotlin, we can utilize it in a powerful way:

```kotlin
/**
*  Create this class in your own database module.
*/
interface DBProvider<out T: DBFlowDatabase> {

  val database: T

}

interface CurrencyDAO : DBProvider<AppDatabase> {

  /**
   *  Utilize coroutines package
   */
  fun coroutineRetrieveUSD(): Deferred<MutableList<Currency>> =
          database.beginTransactionAsync {
              (select from Currency::class
                      where (Currency_Table.symbol eq "$")).queryList(it)
          }.defer()

  /**
   *  Utilize RXJava2 package.
   * Also can use asMaybe(), or asFlowable() (to register for changes and continue listening)
   */
  fun rxRetrieveUSD(): Single<MutableList<Currency>> =
          database.beginTransactionAsync {
              (select from Currency::class
                      where (Currency_Table.symbol eq "$"))
                      .queryList(it)
          }.asSingle()

    /**
     *  Utilize Vanilla Transactions.
     */
    fun retrieveUSD(): Transaction.Builder<MutableList<Currency>> =
            database.beginTransactionAsync {
                (select from Currency::class
                        where (Currency_Table.symbol eq "$"))
                        .queryList(it)
            }

    /**
     *  Utilize Paging Library from paging artifact.
     */
    fun pagingRetrieveUSD(): QueryDataSource.Factory<Currency, Where<Currency>> = (select from Currency::class
            where (Currency_Table.symbol eq "$"))
            .toDataSourceFactory(database)

}
```

DBFlow uses expressive builders to represent and translate to the SQLite language.

We can represent the query above in SQLite:

```text
SELECT * FROM Currency WHERE symbol='$';
```

Wherever we perform dependency injection we supply the instance:

```kotlin
fun provideCurrencyDAO(db: AppDatabase) = object : CurrencyDAO {
    override val database: AppDatabase = db
}
```

Then in our `ViewModel`, we can inject it via the constructor and utilize it in our queries:

```kotlin
class SampleViewModel(private currencyDAO: CurrencyDAO)
```

We support many kinds of complex and complicated queries using the builder language. To read more about this, see [the wrapper language docs](usage/sqlitewrapperlanguage.md)

There is much more you can do in DBFlow. Read through the other docs to get a sense of the library.

