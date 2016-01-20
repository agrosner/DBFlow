# DB Migrations made easy!
Whenever you modify the DB schema you need to increment the DB version through within the `@Database` class it corresponds to. Also you need to add a `Migration` to the configuration or define the migration via `/assets/migrations/{DatabaseName}/{versionName.sql}`.

You can specify a migration to run when database is first created by using version 0!

**Note** any provided subclass such as `AlterTableMigration`, `UpdateTableMigration`, and `IndexMigration` should only override `onPreMigrate()` and **call super.onPreMigrate()** so it's instantiated properly.

**Note** All `Migration` must only have a `public` default constructor.

## Migration Classes
The base class, `BaseMigration` provides a very simple class to execute your migration:

```java

@Migration(version = 2, database = AppDatabase.class)
public class Migration1 extends BaseMigration {

    @Override
    public void migrate(DatabaseWrapper database) {

    }
}
```

## Adding Columns
Here is an example of adding a column to the DB:

Say we have the original example class:

```java

@Table
public class TestModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;

    @Column
    int randomNumber;
}
```

Now we want to **add** a column to this table. We have two ways:
- Put the SQL statement

  `ALTER TABLE TestModel ADD COLUMN timestamp INTEGER;` in a {dbVersion.sql} file in the assets directory. If we need to add any other column, we have to add more lines.

- Through a `Migration`:

```java

@Migration(version = 2, database = AppDatabase.class)
public class Migration1 extends AlterTableMigration<TestModel> {

    @Override
    public void onPreMigrate() {
      // Simple ALTER TABLE migration wraps the statements into a nice builder notation
      addColumn(Long.class, "timestamp");
    }
}
```

## Updating Columns

```java

@Migration(version = 2, database = AppDatabase.class)
public class Migration1 extends UpdateTableMigration<TestModel> {

    @Override
    public void onPreMigrate() {
      // UPDATE TestModel SET deviceType = "phablet" WHERE screenSize > 5.7 AND screenSize < 7;
      set(TestModel_Table.deviceType.is("phablet"))
        .where(TestModel_Table.screenSize.greaterThan(5.7), TestModel_Table.screenSize.lessThan(7));
    }
}
```
