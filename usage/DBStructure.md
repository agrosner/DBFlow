# Tables and Database Properties

## Creating Your Database

Creating databases are _dead-simple_ with DBFlow. Simply define a placeholder ```@Database``` class:

```java

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

  public static final String NAME = "AppDatabase";

  public static final int VERSION = 1;
}


```

_P.S._ you can define as many ```@Database``` as you like as long as the names are unique.

### Configurable Properties

**Global Conflict Handling**: By specifying `insertConflict()` and `updateConflict()` here,
any `@Table` that does not explicitly define either itself will use the corresponding one from the associated `@Database`.


**Kotlin**: To support Kotlin, define a ```generatedClassSeparator()```
that works for it. Simply add:

```java

@Database(generatedClassSeparator = "_")

```

**Integrity Checks on open of the database**: ```consistencyChecksEnabled()``` will run a ```PRAGMA quick_check(1)``` whenever the database is opened. If it fails, it will attempt to copy a prepackaged database.

**Easy back up of the database**: ```backupEnabled()``` enables back up on the database by calling
```
FlowManager.getDatabaseForTable(table).backupDB()
```

Please Note: This creates a temporary _third_ database in case of a failed backup.

**Turn on Foreign Key Constrants**: use the `foreignKeysSupported()=true` to have the database enforce foreign keys. By default this is turned off.


## Model & Creation

All standard tables must use the `@Table` annotation and implement `Model`. As a convenience, `BaseModel` provides a default implementation.

**Models Support**:

  1. Any default java class is supported such as the primitives, boxed primitives, and ```String```.
  2. A non-default object with a ```TypeConverter``` is also save-able.
  3. Composite primary keys
  4. Nested ```Model``` defined as a ```Column.FOREIGN_KEY```, enabling 1-1 relationships
  5. Composite foreign keys

**Restrictions & Limitations**:
  1. All ```Model``` **MUST HAVE** an accessible default constructor. We will use the default constructor when querying the database.
  2. Subclassing works as one would expect: the library gathers all inherited fields annotated with ```@Column``` and count those as rows in the current class's database.
  3. Column names default to the field name as a convenience, but if the name of your fields change you will need to specify the column name.
  4. All fields must be ```public``` or package private as the ```$Adapter``` class needs access to them.
  5. All model class definitions must be top-level (in their own file) and ```public``` or package private.

### Sample Model

This is an example of a ```Model``` class with a primary key (at least one is required) and another field.

```java
@Table(databaseName = AppDatabase.NAME)
public class TestModel extends BaseModel {

    // All tables must have a least one primary key
    @Column(columnType = Column.PRIMARY_KEY)
    String name;

    // By default the column name is the field name
    @Column
    int randomNumber;

}

```

## Advanced Table Features

### All Fields as Columns

As other libraries do, you can set ```@Table(allFields = true)``` to turn on the ability to use all public/package private, non-final, and non-static fields as ```@Column```. You still are required to provide a primary key `@Column` field.


### Unique Groups

In Sqlite you can define any number of columns as having a "unique" relationship, meaning the combination of one or more rows must be unique accross the whole table.

```SQL

UNIQUE('name', 'number') ON CONFLICT FAIL, UNIQUE('name', 'address') ON CONFLICT ROLLBACK

```

To make use:

```java

@Table(databaseName = AppDatabase.NAME,
  uniqueColumnGroups = {@UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.FAIL),
                        @UniqueGroup(groupNumber = 2, uniqueConflict = ConflictAction.ROLLBACK))
public class UniqueModel extends BaseModel {

  @Column(columnType = Column.PRIMARY_KEY, uniqueGroups = {1,2})
  String name;

  @Column(uniqueGroups = 1)
  String number;

  @Column(uniqueGroups = 2)
  String address;

}

```
