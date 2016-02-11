# Tables and Database Properties
## Creating Your Database
Creating databases are _dead-simple_ with DBFlow. Simply define a placeholder `@Database` class:

```java

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

  public static final String NAME = "AppDatabase";

  public static final int VERSION = 1;
}
```

_P.S._ you can define as many `@Database` as you like as long as the names are unique.

### Prepackaged Databases
To include a prepackaged database for your application, simply include the ".db" file in `src/main/assets/{databaseName}.db`. On creation of the database, we copy over the file into the application for usage. Since this is prepackaged within the APK, we cannot delete it once it's copied over, leading to a large APK size (depending on the database file size).

### Configurable Properties
**Global Conflict Handling**: By specifying `insertConflict()` and `updateConflict()` here, any `@Table` that does not explicitly define either itself will use the corresponding one from the associated `@Database`.

**Kotlin**: As of 3.0, Kotlin support is out of the box.

Previously you needed to define a  `generatedClassSeparator()` that works for it.

If you wish to change the default of `_` Simply add some string:

```java

@Database(generatedClassSeparator = "$$")
```

**Integrity Checks on open of the database**: `consistencyChecksEnabled()` will run a `PRAGMA quick_check(1)` whenever the database is opened. If it fails, it will attempt to copy a prepackaged database.

**Easy back up of the database**: `backupEnabled()` enables back up on the database by calling

```
FlowManager.getDatabaseForTable(table).backupDB()
```

Please Note: This creates a temporary _third_ database in case of a failed backup.

**Turn on Foreign Key Constrants**: use the `foreignKeysSupported()=true` to have the database enforce foreign keys. By default this is turned off. We can still define `@ForeignKey`, but their relationships aren't enforced.

**Custom Implementation of OpenHelper**: They must match the constructor params of the `FlowSQLiteOpenHelper`:

```java
public FlowSQLiteOpenHelper(BaseDatabaseDefinition flowManager, DatabaseHelperListener listener)
```

be `public`, and point the `sqlHelperClass()` to the custom class in the `@Database` annotation.

## Model & Creation
All standard tables must use the `@Table` annotation and implement `Model`. As a convenience, `BaseModel` provides a default implementation.

**Models Support**:
  1. Fields: Any default java class is supported such as the primitives, boxed primitives, and `String`.
  2. `TypeConverter`: you can define for a non-standard column classes such as `Date`, `Calendar`, etc. These can be configured on a column-by-column basis.
  3. Composite `@PrimaryKey`
  4. Composite `@ForeignKey`. Nested `Model`, `ModelContainer`, `ForeignKeyContainer` or standard `@Column`.
  5. Combining `@PrimaryKey` and `@ForeignKey`, and those can have complex primary keys.
  6. Inner classes

**Rules And Tips For Models**:
  1. `Model` must have an accessible default constructor. This can be public or package private.
  2. Subclassing is fully supported. DBFlow will gather and combine each subclass' annotations and combine them for the current class.
  3. Fields can be public, package private (we generate package helpers to get access), or private with getters and setters specified. Private fields need to have a getter with `get{Name}` and setter with `set{Name}`. These also can be configured.
  4. We can inherit columns from non-`Model` classes that a class may extend via the `inheritedColumns()` (or `inheritedPrimaryKeys()`). These must be accessible via package-private, public, or private with corresponding getters and setters.
  5. To enable caching, set `cachingEnabled = true`, which will speed up retrieval in most scenarios.

### Sample Model
This is an example of a `Model` class with a primary key (at least one is required) and another field.

```java
@Table(database = AppDatabase.class)
public class TestModel extends BaseModel {

    // All tables must have a least one primary key
    @PrimaryKey
    String name;

    // By default the column name is the field name
    @Column
    int randomNumber;

}
```

## Advanced Table Features
### Custom Type Converter for a specific Column Only
As of 3.0, you can now specify a `TypeConverter` for a specific `@Column` to provide a one-time or more-specific need on a case-by-case basis. To define a `TypeConverter` for a `Column` :

```java

@Column(typeConverter = SomeTypeConverter.class)
SomeObject someObject;
```

It will override the usual conversion/access methods (EXCEPT for if the field is private, it retains the private-based access methods).

### All Fields as Columns
As other libraries do, you can set `@Table(allFields = true)` to turn on the ability to use all public/package private, non-final, and non-static fields as `@Column`. You still are required to provide at least one `@PrimaryKey` field.

If you need to ignore a field, use the `@ColumnIgnore` annotation.

### Private Columns
If you wish to use private fields, simply specify a getter and setter that follow the format of: `name` -> `getName()` + `setName(columnFieldType)`

```java

@Table(database = TestDatabase.class)
public class PrivateModelTest extends BaseModel {

    @PrimaryKey
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

`boolean` fields can use "is"

```java

@Table(database = TestDatabase.class, useIsForPrivateBooleans = true)
public class PrivateModelTest extends BaseModel {

    @PrimaryKey
    private String name;

    @Column
    private boolean selected;

    public boolean isSelected() {
      return selected;
    }

    public void setSelected(boolean selected) {
      this.selected = selected;
    }

    //... etc
}
```

### Default values
When a value of a field is missing or left out, you wish to provide a default "fallback" in the database. SQLite provides this as the `DEFAULT` command in a creation statement.

However in DBFlow it is not so easy to respect this since we rely on precompiled `INSERT` statements with all primary keys in it. So as a compromise, these values are inserted as such:

```java
@Table(database = TestDatabase.class)
public class DefaultModel extends TestModel1 {

    @Column(defaultValue = "55")
    Integer count;

}
```

In the `_Adapter`:

```java
@Override
  public final void bindToInsertValues(ContentValues values, DefaultModel model) {
    if (model.count != null) {
      values.put("`count`", model.count);
    } else {
      values.put("`count`", 55);
    }
    //...
  }
```

We insert its value at runtime. This has some limitations:
  1. Constant, pure value strings
  2. No `Model`, `ModelContainer`, or primitive can use this.
  3. Must be of same type as column.
  4. `String` types need to be escaped via: `"\"something\""`

### Unique Groups
In Sqlite you can define any number of columns as having a "unique" relationship, meaning the combination of one or more rows must be unique across the whole table.

```SQL

UNIQUE('name', 'number') ON CONFLICT FAIL, UNIQUE('name', 'address') ON CONFLICT ROLLBACK
```

To make use:

```java

@Table(database = AppDatabase.class,
  uniqueColumnGroups = {@UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.FAIL),
                        @UniqueGroup(groupNumber = 2, uniqueConflict = ConflictAction.ROLLBACK))
public class UniqueModel extends BaseModel {

  @PrimaryKey
  @Unique(unique = false, uniqueGroups = {1,2})
  String name;

  @Column
  @Unique(unique = false, uniqueGroups = 1)
  String number;

  @Column
  @Unique(unique = false, uniqueGroups = 2)
  String address;

}
```

### Many-To-Many Source Gen

Making an "association" table between two tables that are related via many-to-many
has become drastically easier in DBFlow 3.0+.

#### How It works

1. Annotate your target `Model` class with `@ManyToMany(referencedTable = MyRefTable.class)`
2. DBFlow generates the associated `@Table` class named `{target}{dbflow class separator}{referencedTable}`, with all of its other `_Adapter` and `_Table` counterparts. So for a target of `Shop` with a reference of `Customer`, by default the name of the `Table` generated is `Shop_Customer`.
3. The generated `Model` uses the `@PrimaryKey` of each table as `@ForeignKey` fields along with a single auto-incrementing `Long` `@PrimaryKey`.
3. It also generates getters and setters for the joined table (except a setter on the "_id" field).


#### Example

To define a relationship,
simply define the `@ManyToMany` annotation on a class pointing to another `Model` table:
```java
@Table(database = TestDatabase.class)
@ManyToMany(referencedTable = TestModel1.class)
public class ManyToManyModel extends BaseModel {

    @PrimaryKey
    String name;

    @PrimaryKey
    int id;

    @Column
    char anotherColumn;
}
```

With the associated table as follows:
```java

@Table(database = TestDatabase.class)
public class TestModel1 extends BaseModel {
    @Column
    @PrimaryKey
    String name;
}

```

Which generates a `Model` class as:
```java
@Table(
    database = TestDatabase.class
)
public final class ManyToManyModel_TestModel1 extends BaseModel {
  @PrimaryKey(
      autoincrement = true
  )
  long _id;

  @ForeignKey
  TestModel1 testModel1;

  @ForeignKey
  ManyToManyModel manyToManyModel;

  public final long getId() {
    return _id;
  }

  public final TestModel1 getTestModel1() {
    return testModel1;
  }

  public final void setTestModel1(TestModel1 param) {
    testModel1 = param;
  }

  public final ManyToManyModel getManyToManyModel() {
    return manyToManyModel;
  }

  public final void setManyToManyModel(ManyToManyModel param) {
    manyToManyModel = param;
  }
}
```

Which saves you even more code to maintain or write!
