# Databases

This section describes how databases are created in DBFlow and some more advanced features.

## Creating a Database

In DBFlow, creating a database is as simple as only a few lines of code. DBFlow supports any number of databases, however individual tables and other related files can only be associated with one database. **Note**: Starting with DBFlow 5.0, databases are required to extend `DBFlowDatabase`.

```kotlin
@Database(version = 1)
abstract class AppDatabase : DBFlowDatabase()
```

or in Java:

```java
@Database(version = 1)
public abstract class AppDatabase extends DBFlowDatabase() {
}
```

## Initialization

To specify a custom **name** to the database, in previous versions of DBFlow \(&lt; 4.1.0\), you had to specify it in the `@Database` annotation. As of 5.0 now you pass it in the initialization of the `FlowManager`:

```kotlin
FlowManager.init(FlowConfig.builder()
    .database(DatabaseConfig.builder(AppDatabase::class)
      .databaseName("AppDatabase")
      .build())
    .build())
```

To dynamically change the database name, call:

```kotlin
database<AppDatabase>()
  .reopen(DatabaseConfig.builder(AppDatabase::class)
    .databaseName("AppDatabase-2")
    .build())
```

or in Java:

```java
FlowManager.getDatabase(AppDatabase.class)
  .reopen(DatabaseConfig.builder(AppDatabase.class)
    .databaseName("AppDatabase-2")
    .build())
```

This will close the open DB, reopen the DB, and replace previous `DatabaseConfig` with this new one. Ensure that you persist the changes to the `DatabaseConfig` somewhere as next time app is launched and DBFlow is initialized, the new config would get overwritten.

### In Memory Databases

As with **name**, in previous versions of DBFlow \(&lt; 5.0\), you specified `inMemory` in the `@Database` annotation. Starting with 5.0 that is replaced with:

```kotlin
FlowManager.init(FlowConfig.builder()
    .database(DatabaseConfig.inMemoryBuilder(AppDatabase::class.java)
      .databaseName("AppDatabase")
      .build())
    .build())
```

```java
FlowManager.init(FlowConfig.builder()
    .database(DatabaseConfig.inMemoryBuilder(AppDatabase::class)
      .databaseName("AppDatabase")
      .build())
    .build())
```

This will allow you to use in-memory databases in your tests, while writing to disk in your apps. Also if your device the app is running on is low on memory, you could also swap the DB into memory by calling `reopen(DatabaseConfig)` as explained above.

## Database Migrations

Database migrations are run when upon open of the database connection, the version number increases on an existing database.

It is preferred that `Migration` files go in the same file as the database, for organizational purposes. An example migration:

```kotlin
@Database(version = 2)
abstract class AppDatabase : DBFlowDatabase() {

  @Migration(version = 2, database = MigrationDatabase::class)
  class AddEmailToUserMigration : AlterTableMigration<User>(User::class.java) {

    override fun onPreMigrate() {
        addColumn(SQLiteType.TEXT, "email")
    }
  }
}
```

```java
@Database(version = 2)
public abstract class AppDatabase extends DBFlowDatabase {

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

This simple example adds a column to the `User` table named "email". In code, just add the column to the `Model` class and this migration runs only on existing dbs. To read more on migrations and more examples of different kinds, visit the [page](migrations.md).

## Advanced Database features

This section goes through features that are for more advanced use of a database, and may be very useful.

### Prepackaged Databases

To include a prepackaged database for your application, simply include the ".db" file in `src/main/assets/{databaseName}.db`. On creation of the database, we copy over the file into the application for usage. Since this is prepackaged within the APK, we cannot delete it once it's copied over, which can bulk up your raw APK size. _Note_ this is only copied over on initial creation of the database for the app.

### Global Conflict Handling

In DBFlow when an INSERT or UPDATE are performed, by default, we use `NONE`. If you wish to configure this globally, you can define it to apply for all tables from a given database:

```kotlin
@Database(version = 2, insertConflict = ConflictAction.IGNORE, updateConflict= ConflictAction.REPLACE)
abstract class AppDatabase : DBFlowDatabase()
```

```java
@Database(version = 2, insertConflict = ConflictAction.IGNORE, updateConflict= ConflictAction.REPLACE)
public abstract class AppDatabase extends DBFlowDatabase {
}
```

These follow the SQLite standard [here](https://www.sqlite.org/conflict.html).

### Integrity Checking

Databases can get corrupted or in an invalid state at some point. If you specify `consistencyChecksEnabled=true` It runs a `PRAGMA quick_check(1)` whenever the database is opened. If it fails, you should provide a backup database that it will copy over. If not, **we wipe the internal database**. Note that during this time in case of failure we create a **third copy of the database** in case transfer fails.

### Custom FlowSQLiteOpenHelper

For variety of reasons, you may want to provide your own `FlowSQLiteOpenHelper` to manage database interactions. To do so, you must implement `OpenHelper`, but for convenience you should extend `AndroidSQLiteOpenHelper` \(for Android databases\), or `SQLCipherOpenHelper` for SQLCipher. Read more [here](../../advanced-usage/sqlciphersupport.md)

```kotlin
class CustomFlowSQliteOpenHelper(context: Contect, databaseDefinition: DatabaseDefinition, listener: DatabaseHelperListener) : FlowSQLiteOpenHelper(context, databaseDefinition, listener)
```

```java
public class CustomFlowSQliteOpenHelper extends FlowSQLiteOpenHelper {

    public CustomFlowSQliteOpenHelper(Context context, BaseDatabaseDefinition databaseDefinition, @Nullable DatabaseCallback callback) {
        super(context, databaseDefinition, callback);
    }
}
```

Then in your `DatabaseConfig`:

```kotlin
FlowManager.init(FlowConfig.builder(context)
  .database(DatabaseConfig.Builder(CipherDatabase::class.java)
      .openHelper(::CustomFlowSQliteOpenHelper)
      .build())
  .build())
```

```java
FlowManager.init(FlowConfig.builder(context)
  .database(
      DatabaseConfig.builder(CipherDatabase.class)
          .openHelper(new DatabaseConfig.OpenHelperCreator() {
              @Override
              public OpenHelper createHelper(@NonNull DatabaseDefinition databaseDefinition, @Nullable DatabaseCallback callback) {
                  return new CustomFlowSQliteOpenHelper(context, databaseDefinition, callback);
              }
          })
      .build())
  .build());
```

