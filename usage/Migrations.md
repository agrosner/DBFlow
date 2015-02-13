# DB Migrations made easy!

Whenever you modify the DB schema you need to increment the DB version through within the ```@Database``` class it corresponds to. Also you need to add a ```Migration``` to the configuration or define the migration via ```/assets/migrations/{DatabaseName}/{versionName.sql}```. 

**Note** any provided subclass such as ```AlterTableMigration``` and ```UpdateTableMigration``` should only override ```onPreMigrate()``` and **call super.onPreMigrate()** so it's instantiated properly. 

## Migration Classes

The base class, ```BaseMigration``` provides a very simple class to execute your migration:

```java

@Migration(version = 2, databaseName = AppDatabase.NAME)
public class Migration1 extends BaseMigration {

    @Override
    public void migrate(SQLiteDatabase database) {

    }
}

```

## Adding Columns

Here is an example of adding a column to the DB: 

Say we have the original example class:

```java

@Table
public class TestModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY))
    String name;

    @Column
    int randomNumber;
}

```

Now we want to **add** a column to this table. We have two ways:

1. Put the SQL statement 
```ALTER TABLE TestModel ADD COLUMN timestamp INTEGER;``` in a {dbVersion.sql} file in the assets directory. If we need to add any other column, we have to add more lines. 

2. Through a ```Migration```:

```java

@Migration(version = 2, databaseName = AppDatabase.NAME)
public class Migration1 extends AlterTableMigration<TestModel> {

    @Override
    public void onPreMigrate() {
      super.onPreMigrate();
      // Simple ALTER TABLE migration wraps the statements into a nice builder notation
      addColumn(Long.class, "timestamp");
    }
}

```

## Updating Columns

```java

@Migration(version = 2, databaseName = AppDatabase.NAME)
public class Migration1 extends UpdateTableMigration<TestModel> {

    @Override
    public void onPreMigrate() {
      // UPDATE TestModel SET deviceType = "phablet" WHERE screenSize > 5.7 AND screenSize < 7;
      set(Condition.column(TestModel$Table.DEVICETYPE)
        .is("phablet"))
        .where(Condition.column(TestModel$Table.SCREENSIZE).greaterThan(5.7),
               Condition.column(TestModel$Table.SCREENSIZE).lessThan(7))
    }
}


```
