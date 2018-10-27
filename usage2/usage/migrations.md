# Migrations

In this section we will discuss how migrations work, how each of the provided migration classes work, and how to create your own custom one.

There are two kinds of migrations that DBFlow supports: Script-based SQL files and class annotation-based migrations.

## How Migrations Work

In SQL databases, migrations are used to modify or change existing database schema to adapt to changing format or nature of stored data. In SQLite we have a limited ability compared to SQL to modify tables and columns of an existing database. There are only two kinds of modifications that exist: rename table and add a new column.

In DBFlow migrations are not only used to modify the _structure_ of the database, but also other operations such as insert data into a database \(for prepopulate\), or add an index on a specific table.

Migrations are only run on an existing database _except_ for the "0th" migration. Read [initial database setup](migrations.md#initial-database-setup)

### Migration Classes

We recommend placing any `Migration` inside an associated `@Database` class so it's apparent the migration is tied to it. An example migration class:

```java
@Database(version = 2)
public class AppDatabase {

    @Migration(version = 2, database = AppDatabase.class)
    public static class Migration2 extends BaseMigration {

        @Override
        public void migrate(DatabaseWrapper database) {
          // run some code here
          SQLite.update(Employee.class)
            .set(Employee_Table.status.eq("Invalid"))
            .where(Employee_Table.job.eq("Laid Off"))
            .execute(database); // required inside a migration to pass the wrapper
        }
    }
}
```

```kotlin
@Database(version = 2)
object AppDatabase {

    @Migration(version = 2, database = AppDatabase.class)
    class Migration2 : BaseMigration() {

        override fun migrate(database: DatabaseWrapper) {
          // run some code here
          (update<Employee>()
            set Employee_Table.status.eq("Invalid")
            where Employee_Table.job.eq("Laid Off"))
            .execute(database) // required to pass wrapper in migration
        }
    }
}
```

The classes provide the ability to set a `priority` on the `Migration` so that an order is 
established if more than one migration is to run on the same DB version upgrade.
They are in reverse order - lower the priority, that one will execute first.

`Migration` have three methods:
  1. `onPreMigrate()` - called first, do setup, and construction here.
  2. `migrate()` -&gt; called with the `DatabaseWrapper` specified, this is where the actual migration code should execute. 
  3. `onPostMigrate()` -&gt; perform some cleanup, or any notifications that it was executed.

### Migration files

DBFlow also supports `.sql` migration files. The rules on these follows must be followed: 1. Place them in `assets/migrations/{DATABASE_NAME}/{versionNumber}.sql`. So that an example `AppDatabase` migration for version 2 resides in `assets/migrations/AppDatabase/2.sql` 2. The file can contain any number of SQL statements - they are executed in order. Each statement must be on a single line or multiline and must end with `;` 3. Comments are allowed as long as they appear on an individual file with standard SQLite comment syntax `--`

### Prevent Recursive Access to the DB

Since `Migration` occur when the database is opening, we cannot recursively access the database object in our models, SQLite wrapper statements, and other classes in DBFlow that are inside a `Migration`.

To remedy that, DBFlow comes with support to pass the `DatabaseWrapper` into almost all places that require it: 1. All query language `BaseQueriable` objects such as `Select`, `Insert`, `Update`, `Delete`, etc have methods that take in the `DatabaseWrapper` 2. Any subclass of `BaseModel` \(`Model` does not provide the methods for simplicity\)

### Initial Database Setup

DBFlow supports `Migration` that run on version "0" of a database. When Android opens a `SQLiteDatabase` object, if the database is created, DBFlow calls on a `Migration` of -1 to 0th version. In this case, any `Migration` run at `version = 0` will get called. Once a database is created, this migration will not run again. So if you had an existent database at version 1, and changed version to 2, the "0th" `Migration` is not run because the old version the database would have been 1.

## Provided Migration Classes

In DBFlow we provide a few helper `Migration` subclasses to provide default and easier implementation: 1. `AlterTableMigration` 2. `IndexMigration/IndexPropertyMigration` 3. `UpdateTableMigration`

### AlterTableMigration

The _structural_ modification of a table is brought to a handy `Migration` subclass.

It performs both of SQLite supported operations: 1. Rename tables 2. Add columns.

For renaming tables, you should rename the `Model` class' `@Table(name = "{newName}")` before running this `Migration`. The reason is that DBFlow will know the new name only and the existing database will get caught up on it through this migration. Any new database created on a device will automatically have the new table name.

For adding columns, we only support `SQLiteType` \(all supported ones [here](https://www.sqlite.org/datatype3.html)\) operations to add or remove columns. This is to enforce that the columns are created properly. If a column needs to be a `TypeConverter` column, use the database value from it. We map the associated type of the database field to a `SQLiteType` in [SQLiteType.kt](https://github.com/agrosner/DBFlow/tree/fb3739caa4c894d50fd0d7873c70a33416c145e6/dbflow/src/main/java/com/dbflow5/sql/SQLiteType.kt). So if you have a `DateConverter` that specifies a `Date` column converted to `Long`, then you should look up `Long` in the `Map`. In this case `Long` converts to `INTEGER`.

```java
@Migration(version = 2, database = AppDatabase.class)
public class Migration2 extends AlterTableMigration<AModel> {

    public Migration2(Class<AModel> table) {
        super(table);
    }

    @Override
    public void onPreMigrate() {
        addColumn(SQLiteType.TEXT, "myColumn");
        addColumn(SQLiteType.REAL, "anotherColumn");
    }
}
```

```kotlin
@Migration(version = 2, database = AppDatabase.class)
class Migration2 : AlterTableMigration<AModel>(AModel::class.java) {

    override fun onPreMigrate() {
        addColumn(SQLiteType.TEXT, "myColumn")
        addColumn(SQLiteType.REAL, "anotherColumn")
    }
}
```

### Index Migrations

An `IndexMigration` \(and `IndexPropertyMigration`\) is used to structurally activate an `Index` on the database at a specific version. See [here](../advanced-usage/indexing.md) for information on creating them.

`IndexMigration` does not require an `IndexProperty` to run, while `IndexPropertyMigration` makes use of the property to run.

An `IndexMigration`:

```java
@Migration(version = 2, priority = 0, database = MigrationDatabase.class)
public static class IndexMigration2 extends IndexMigration<MigrationModel> {

  public IndexMigration2(@NonNull Class<MigrationModel> onTable) {
      super(onTable);
  }

  @NonNull
  @Override
  public String getName() {
      return "TestIndex";
  }
}
```

```kotlin
@Migration(version = 2, priority = 0, database = MigrationDatabase::class)
class IndexMigration2 : IndexMigration<MigrationModel>(MigrationModel::class.java) {

    override fun getName() = "TestIndex"
}
```

An `IndexPropertyMigration`:

```java
@Migration(version = 2, priority = 1, database = MigrationDatabase.class)
public static class IndexPropertyMigration2 extends IndexPropertyMigration {

   @NonNull
   @Override
   public IndexProperty getIndexProperty() {
       return IndexModel_Table.index_customIndex;
   }
}
```

```kotlin
@Migration(version = 2, priority = 1, database = MigrationDatabase.class)
class IndexPropertyMigration2 : IndexPropertyMigration {

   override fun getIndexProperty() = IndexModel_Table.index_customIndex
}
```

### Update Table Migration

A simple wrapper around `Update`, provides simply a default way to update data during a migration.

```java
@Migration(version = 2, priority = 2, database = MigrationDatabase.class)
public static class UpdateMigration2 extends UpdateTableMigration<MigrationModel> {

   /**
    * Creates an update migration.
    *
    * @param table The table to update
    */
   public UpdateMigration2(Class<MigrationModel> table) {
       super(table);
       set(MigrationModel_Table.name.eq("New Name"));
   }

}
```

