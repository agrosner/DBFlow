# Databases

This section describes how databases are created in DBFlow and some more
advanced features.

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

## Database Migrations

Database migrations are run when upon open of the database connection,
the version number increases on an existing database.

It is preferred that `Migration` files go in the same file as the database, for
organizational purposes.
An example migration:

```java

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

  public static final String NAME = "AppDatabase"; // we will add the .db extension

  public static final int VERSION = 2;

  @Migration(version = 2, database = MigrationDatabase.class)
  public static class AddEmailToUserMigration extends AlterTableMigration<User> {

    public AddEmailToUserMigration(Class<User> table) {
        super(table);
    }

    @Override
    public void onPreMigrate() {
        addColumn(SQLiteType.TEXT, "email");
    }
  }
}

```
This simple example adds a column to the `User` table named "email". In code, just add
the column to the `Model` class and this migration runs only on existing dbs.
 To read more on migrations and more examples of different kinds, visit the [page](/usage2/Migrations.md).

## Advanced Database features

This section goes through features that are for more advanced use of a database,
and may be very useful.

### Prepackaged Databases
To include a prepackaged database for your application, simply include the ".db" file in `src/main/assets/{databaseName}.db`. On creation of the database, we copy over the file into the application for usage. Since this is prepackaged within the APK, we cannot delete it once it's copied over,
which can bulk up your raw APK size. _Note_ this is only copied over on initial creation
of the database for the app.

### Global Conflict Handling
In DBFlow when an INSERT or UPDATE are performed, by default, we use `NONE`. If you wish to configure this globally, you can define it to apply for all tables from a given database:


```java

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION, insertConflict = ConflictAction.IGNORE, updateConflict= ConflictAction.REPLACE)
public class AppDatabase {
}

```

These follow the SQLite standard [here](https://www.sqlite.org/conflict.html).

### Integrity Checking

Databases can get corrupted or in an invalid state at some point. If you specify
`consistencyChecksEnabled=true` It runs a `PRAGMA quick_check(1)`
whenever the database is opened. If it fails, you should provide a backup database
that it will copy over. If not, **we wipe the internal database**. Note that during this
time in case of failure we create a **third copy of the database** in case transfer fails.

### Custom FlowSQLiteOpenHelper

For variety of reasons, you may want to provide your own `FlowSQLiteOpenHelper`
to manage database interactions. To do so, you must implement `OpenHelper`, but
for convenience you should extend `FlowSQLiteOpenHelper` (for Android databases),
or `SQLCipherOpenHelper` for SQLCipher. Read more [here](/usage2/SQLCipherSupport.md)

```java


public class CustomFlowSQliteOpenHelper extends FlowSQLiteOpenHelper {

    public CustomFlowSQliteOpenHelper(BaseDatabaseDefinition databaseDefinition, DatabaseHelperListener listener) {
        super(databaseDefinition, listener);
    }
}


```

Then in your `DatabaseConfig`:

```java

FlowManager.init(new FlowConfig.Builder(RuntimeEnvironment.application)
  .addDatabaseConfig(
      new DatabaseConfig.Builder(CipherDatabase.class)
          .openHelper(new DatabaseConfig.OpenHelperCreator() {
              @Override
              public OpenHelper createHelper(DatabaseDefinition databaseDefinition, DatabaseHelperListener helperListener) {
                  return new CustomFlowSQliteOpenHelper(databaseDefinition, helperListener);
              }
          })
      .build())
  .build());


```
