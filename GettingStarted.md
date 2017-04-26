# Getting Started

This section describes how Models and tables are constructed via DBFlow. first
let's describe how to get a database up and running.

## Creating a Database

In DBFlow, creating a database is as simple as only a few lines of code. DBFlow
supports any number of databases, however individual tables and other related files
can only be associated with one database.

```java

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

  public static final String NAME = "AppDatabase"; // we will add the .db extension

  public static final int VERSION = 1;
}


```

Writing this file generates (by default) a `AppDatabaseAppDatabase_Database.java`
file, which contains tables, views, and more all tied to a specific database. This
class is automatically placed into the main `GeneratedDatabaseHolder`, which holds
potentially many databases. The name, `AppDatabaseAppDatabase_Database.java`, is generated
via {DatabaseClassName}{DatabaseFileName}{GeneratedClassSepator, default = "\_"}Database

To learn more about what you can configure in a database, read [here](Databases.md)

## Initialize FlowManager

DBFlow needs an instance of `Context` in order to use it for a few features such
as reading from assets, content observing, and generating `ContentProvider`.

Initialize in your `Application` subclass. You can also initialize it from other
`Context` but we always grab the `Application` `Context` (this is done only once).

```java
public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }
}

```

Finally, add the definition to the manifest (with the name that you chose for your custom application):
```xml
<application
  android:name="{packageName}.ExampleApplication"
  ...>
</application>
```

A database within DBFlow is only initialized once you call `FlowManager.getDatabase(SomeDatabase.class).getWritableDatabase()`. If you
don't want this behavior or prefer it to happen immediately, modify your `FlowConfig`:

```java

@Override
public void onCreate() {
    super.onCreate();
    FlowManager.init(new FlowConfig.Builder(this)
        .openDatabasesOnInit(true).build());
}

```

If you do not like the built-in `DefaultTransactionManager`, or just want to roll your own existing system:

```java

FlowManager.init(new FlowConfig.Builder(this)
    .addDatabaseConfig(
        new DatabaseConfig.Builder(AppDatabase.class)
            .transactionManager(new CustomTransactionManager())
          .build()));

```
You can define different kinds for each database.
To read more on transactions and subclassing `BaseTransactionManager` go [here](StoringData.md)


## Create Models

Creating models are as simple as defining the model class, and adding the `@Table` annotation.
To read more on this, read [here](Models.md).

An example:

```java


@Table(database = TestDatabase.class)
public class Currency {

    @PrimaryKey(autoincrement = true)
    long id; // package-private recommended, not required

    @Column
    @Unique
    String symbol;

    @Column
    String shortName;

    @Column
    @Unique
    private String name; // private with getters and setters

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
}

```

## Perform Some Queries

DBFlow uses expressive builders to represent and translate to the SQLite language.

A simple query in SQLite:

```sqlite

SELECT * FROM Currency WHERE symbol='$';

```

Can be represented by:

```java

SQLite.select()
  .from(Currency.class)
  .where(Currency_Table.symbol.eq("$"));

```

We support many kinds of complex and complicated queries using the builder
language. To read more about this, see [the wrapper language docs](SQLiteWrapperLanguage.md)

There is much more you can do in DBFlow. Read through the other docs to
get a sense of the library.
