# Models

In DBFlow we dont have any restrictions on what your table class is. We do, however if you use Java, we recommend you subclass `BaseModel` on
your highest-order base-class, which provides a default implementation for you.

When using regular models:
```java
FlowManager.getModelAdapter(MyTable.class).save(myTableObject);
```

When using `BaseModel`, it is much cleaner:

```java
myTableObject.save();
```

if you use Kotlin, you can add the [kotlin extensions](/KotlinSupport.md) to your project use them as extension methods:
```kotlin
myTableObject.save()
```

## Columns

By default, DBFlow lazily looks for columns. This means that they all must contain either `@PrimaryKey`, `@Column`, or `@ForeignKey` to be used in tables.

If you wish to make it simpler and include all fields in a class, set `@Table(allFields = true)`.
However this still requires you to specify at least one `@PrimaryKey` field. You
can then explicitly ignore fields via the `@ColumnIgnore` annotation.

Columns can be `public`, package-private, or `private`.
`private` fields __must__ come with `public` java-bean-style getters and setters.

Here is an example of a "nice" `private` field:

```java
@Table(database = AppDatabase.class)
public class Dog {

  @PrimaryKey
  private String name;

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}

```

```kotlin
@Table(database = AppDatabase.class)
public class Dog(@Primary var name: String? = null)
```

Columns have a wide-range of supported types in the `Model` classes:
**Supported Types**:
  1. all java primitives including `char`,`byte`, `short`, and `boolean`.
  2. All java boxed primitive classes
  3. String, Date, java.sql.Date, Calendar, Blob, Boolean
  4. Custom data types via a [TypeConverter](TypeConverters.md)
  5. `Model` as fields, but only as `@PrimaryKey` and/or `@ForeignKey`
  6. `@ColumnMap` objects that flatten an object into the current table. Just like a `@ForeignKey`, but without requiring a separate table. (4.1.0+). Avoid nesting more than one object, as the column count could get out of control.

**Unsupported Types**:
  1. `List<T>` : List columns are not supported and not generally proper for a relational database. However, you can get away with a non-generic `List` column via a `TypeConverter`. But again, avoid this if you can.
  2. Anything that is generically typed (even with an associated `TypeConverter`). If you need to include the field, subclass the generic object and provide a `TypeConverter`.

## Inherited Columns

Since we don't require extension on `BaseModel` directly, tables can extend non-model classes and inherit their fields directly (given proper accessibility) via the `@InheritedColumn` annotation (or `@InheritedPrimaryKey` for primary keys):

```java

@Table(database = AppDatabase.class,
        inheritedColumns = {@InheritedColumn(column = @Column, fieldName = "name"),
                @InheritedColumn(column = @Column, fieldName = "number")},
        inheritedPrimaryKeys = {@InheritedPrimaryKey(column = @Column,
                primaryKey = @PrimaryKey,
                fieldName = "inherited_primary_key")})
public class InheritorModel extends InheritedModel implements Model {

```

## Primary Keys

DBFlow supports multiple primary keys, right out of the box. Simply create a table with multiple `@PrimaryKey`:

```java
@Table(database = AppDatabase.class)
public class Dog extends BaseModel {

  @PrimaryKey
  String name;

  @PrimaryKey
  String breed;

}

```

```kotlin
@Table(database = AppDatabase::class)
class Dog(@PrimaryKey var name: String? = null,
          @PrimaryKey var breed: String? = null)

```


If we want an auto-incrementing key, you specify `@PrimaryKey(autoincrement = true)`, but only one of these kind can exist in a table and you cannot mix with regular primary keys.

## Unique Columns

DBFlow has support for SQLite `UNIQUE` constraint (here for documentation)[http://www.tutorialspoint.com/sqlite/sqlite_constraints.htm].

Add `@Unique` annotation to your existing `@Column` and DBFlow adds it as a constraint when
the database table is first created. This means that once it is created you should not change or modify this.

We can _also_ support multiple unique clauses in order to ensure any combination of fields are unique. For example:

To generate this in the creation query:
```sqlite
UNIQUE('name', 'number') ON CONFLICT FAIL, UNIQUE('name', 'address') ON CONFLICT ROLLBACK
```
We declare the annotations as such:

```java

@Table(database = AppDatabase.class,
  uniqueColumnGroups = {@UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.FAIL),
                        @UniqueGroup(groupNumber = 2, uniqueConflict = ConflictAction.ROLLBACK))
public class UniqueModel {

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

The `groupNumber` within each defined `uniqueColumnGroups` with an associated `@Unique` column. We need to specify `unique=false` for any column used in a group so we expect the column to be part of a group. If true as well, the column will _also_ alone be unique.

## Default Values

DBFlow supports default values in a slighty different way that SQLite does. Since we do not know
exactly the intention of missing data when saving a `Model`, since we group all fields, `defaultValue` specifies
a value that we replace when saving to the database when the value of the field is `null`.

This feature only works on Boxed primitive and the `DataClass` equivalent of objects (such as from TypeConverter), such as String, Integer, Long, Double, etc.
__Note__: If the `DataClass` is a `Blob`, unfortunately this will not work.
For `Boolean` classes, use "1" for true, "0" for false.

```java

@Column(defaultValue = "55")
Integer count;

@Column(defaultValue = "\"this is\"")
String test;

@Column(defaultValue = "1000L")
Date date;

@Column(defaultValue = "1")
Boolean aBoolean;

```

DBFlow inserts it's literal value into the `ModelAdapter` for the table so any `String` must be escaped.
