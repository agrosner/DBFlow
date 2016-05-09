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

To learn more about what you can configure in a database, read [here](/usage2/Databases.md)

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
        FlowManager.init(new FlowConfig.Builder(this).build());
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
To read more on transactions and subclassing `BaseTransactionManager` go [here](/usage2/StoringData.md)


## Create Models

All your database tables _must_ implement `Model`, which is simply an interface:

```java

public interface Model {

    /**
     * Saves the object in the DB.
     */
    void save();

    /**
     * Deletes the object in the DB
     */
    void delete();

    /**
     * Updates an object in the DB. Does not insert on failure.
     */
    void update();

    /**
     * Inserts the object into the DB
     */
    void insert();

    /**
     * @return true if this object exists in the DB. It combines all of it's primary key fields
     * into a SELECT query and checks to see if any results occur.
     */
    boolean exists();

```

As a convenience (and recommended for most uses), you should extend `BaseModel`, which provides the default implementation. If for some reason you must implement `Model`, you should reference its implementation. **Also** you don't need to directly extend `BaseModel`, in fact you can extend other tables to combine their columns. However those fields must be package-private, public, or private with accessible java-bean getters and setters.
To read more on this, read [here](/usage2/Models.md).

An example:

```java


@Table(database = TestDatabase.class)
public class Currency extends BaseModel {

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
language. To read more about this, see [the wrapper language docs](/usage2/SQLiteWrapperLanguage.md)

There is much more you can do in DBFlow. Read through the other docs to
get a sense of the library.
