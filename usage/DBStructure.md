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

Previously you needed to define a  `generatedClassSeparator()` that works for it. Simply add:

```java

@Database(generatedClassSeparator = "_")
```

**Integrity Checks on open of the database**: `consistencyChecksEnabled()` will run a `PRAGMA quick_check(1)` whenever the database is opened. If it fails, it will attempt to copy a prepackaged database.

**Easy back up of the database**: `backupEnabled()` enables back up on the database by calling

```
FlowManager.getDatabaseForTable(table).backupDB()
```

Please Note: This creates a temporary _third_ database in case of a failed backup.

**Turn on Foreign Key Constrants**: use the `foreignKeysSupported()=true` to have the database enforce foreign keys. By default this is turned off. We can still define `@ForeignKey`, but their relationships aren't enforced.

**Custom Subclasses of FlowSQLiteOpenHelper**: They must match the constructor of the `FlowSQLiteOpenHelper`:

```java
public FlowSQLiteOpenHelper(BaseDatabaseDefinition flowManager, DatabaseHelperListener listener)
```

be `public`, and point the `sqlHelperClass()` to the custom class in the `@Database` annotation.

## Model & Creation
All standard tables must use the `@Table` annotation and implement `Model`. As a convenience, `BaseModel` provides a default implementation.

**Models Support**:
  1. Any default java class is supported such as the primitives, boxed primitives, and `String`.
  2. A non-default object with a `TypeConverter` is also save-able. Objects with type-parameters are _not_ supported. Also `List`, `Map` or multi-value fields are not recommended. If you must, you have to leave out the type parameters.
  3. Composite primary keys
  4. Nested `Model` defined as a `@ForeignKey`, enabling 1-1 relationships.
  5. Any `ModelContainer` as a `@ForeignKey`
  6. Composite `@ForeignKey`
  7. Custom `TypeConverter` via `@Column(typeConverter = SomeTypeConverter.class)`

**Restrictions & Limitations**:
  1. All `Model` **MUST HAVE** an accessible default constructor. We will use the default constructor when querying the database.
  2. Subclassing works as one would expect: the library gathers all inherited fields annotated with `@Column` and count those as rows in the current class's database.
  3. Column names default to the field name as a convenience, but if the name of your fields change you will need to specify the column name.
  4. All fields must be `public` or package private as the `$Adapter` class needs access to them. _NOTE:_ Package private fields need _not_ be in the same package as DBFlow will generate the necessary access methods to get to them.
  5. or private ONLY when you specify `get{Name}()` and `set{Name}(columnType)` methods for a column named `{name}`. This can be configured.
  6. All model class definitions must be top-level (in their own file) and `public`.

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
