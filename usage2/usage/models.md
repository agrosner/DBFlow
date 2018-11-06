# Models

In DBFlow we dont have any restrictions on what your table class is. We do, however if you use Java, we recommend you subclass `BaseModel` on your highest-order base-class, which provides a default implementation for you. Otherwise utilize a kotlin extension method on `Any`.

```kotlin
myTableObject.save()
```

## Columns

By default, DBFlow inclusdes all properties as columns. For other kinds of fields, they must contain either `@PrimaryKey` or `@ForeignKey` to be used in tables. However this still requires you to specify at least one `@PrimaryKey` field. You can then explicitly ignore fields via the `@ColumnIgnore` annotation if necessary. You can turn off all fields and make it explicit using `@Table(allFields = false)`

In Kotlin, Column properties must be public and `var` for now. In future versions, we hope to support Kotlin constructors without default arguments. For now, all must be `var` and provide a default constructor. We respect nullability of the properties and won't assign `null` to them if they're not nullable.

In Java, Columns can be `public`, package-private, or `private`. `private` fields **must** come with `public` java-bean-style getters and setters. Package private used in other packages generate a `_Helper` class which exposes a method to call these fields in an accessible way. This has some overhead, so consider making them with `public` get/set or public.

Here is an example of a "nice" `Table`:

```kotlin
@Table(database = AppDatabase.class)
public class Dog(@PrimaryKey var id: Int = 0, var name: String? = null)
```

Columns have a wide-range of supported types in the `Model` classes: **Supported Types**:

   1. all primitives including `Char`,`Byte`, `Short`, and `Boolean`.
   2. All Kotlin nullable primitives (java boxed).
   3. String, Date, java.sql.Date, Calendar, Blob, Boolean
   4. Custom data types via a [TypeConverter](typeconverters.md)
   5. `Model` as fields, but only as `@PrimaryKey` and/or `@ForeignKey`
   6. `@ColumnMap` objects that flatten an object into the current table. Just like a `@ForeignKey`, but without requiring a separate table. \(4.1.0+\). Avoid nesting more than one object, as the column count could get out of control.

**Unsupported Types**:

  1. `List<T>` : List columns are not supported and not generally proper for a relational database. However, you can get away with a non-generic `List` column via a `TypeConverter`. But again, avoid this if you can.
  2. Anything that is generically typed \(even with an associated `TypeConverter`\). If you need to include the field, subclass the generic object and provide a `TypeConverter`.

## Inherited Columns

Since we don't require extension on `BaseModel` directly, tables can extend non-model classes and inherit their fields directly \(given proper accessibility\) via the `@InheritedColumn` annotation \(or `@InheritedPrimaryKey` for primary keys\):

```java
@Table(database = AppDatabase.class,
        inheritedColumns = {@InheritedColumn(column = @Column, fieldName = "name"),
                @InheritedColumn(column = @Column, fieldName = "number")},
        inheritedPrimaryKeys = {@InheritedPrimaryKey(column = @Column,
                primaryKey = @PrimaryKey,
                fieldName = "inherited_primary_key")})
public class InheritorModel extends InheritedModel implements Model {
```

Generally, this should be avoided and if you control the source, just place your model objects / db objects in same module as a `db` module.

## Primary Keys

DBFlow supports multiple primary keys, right out of the box. Simply create a table with multiple `@PrimaryKey`:

```kotlin
@Table(database = AppDatabase::class)
class Dog(@PrimaryKey var name: String = "", @PrimaryKey var breed: String = "")
```

If we want an auto-incrementing key, you specify `@PrimaryKey(autoincrement = true)`, but only one of these kind can exist in a table and you cannot mix with regular primary keys.

## Unique Columns

DBFlow has support for SQLite `UNIQUE` constraint \(here for documentation\)\[[http://www.tutorialspoint.com/sqlite/sqlite\_constraints.htm](http://www.tutorialspoint.com/sqlite/sqlite_constraints.htm)\].

Add `@Unique` annotation to your existing `@Column` and DBFlow adds it as a constraint when the database table is first created. This means that once it is created you should not change or modify this.

We can _also_ support multiple unique clauses in order to ensure any combination of fields are unique. For example:

To generate this in the creation query:

```text
UNIQUE('name', 'number') ON CONFLICT FAIL, UNIQUE('name', 'address') ON CONFLICT ROLLBACK
```

We declare the annotations as such:

```kotlin
@Table(database = AppDatabase::class,
  uniqueColumnGroups = {@UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.FAIL),
                        @UniqueGroup(groupNumber = 2, uniqueConflict = ConflictAction.ROLLBACK))
class UniqueModel(
  @PrimaryKey @Unique(unique = false, uniqueGroups = {1,2})
  var name: String = "",
  @Column @Unique(unique = false, uniqueGroups = 1)
  var number: String = "",
  @Column @Unique(unique = false, uniqueGroups = 2)
  var address: String = "")
```

The `groupNumber` within each defined `uniqueColumnGroups` with an associated `@Unique` column. We need to specify `unique=false` for any column used in a group so we expect the column to be part of a group. If true as well, the column will _also_ alone be unique.

## Default Values
**Not to be confused with Kotlin default values** This only applies when fields are marked as `nullable`. When fields are non null in kotlin, we utilize the default constructor value when it is set, so when the column data is `null` from a `Cursor`, we do not override the initial assignment.

DBFlow supports default values in a slighty different way that SQLite does. Since we do not know exactly the intention of missing data when saving a `Model`, since we group all fields, `defaultValue` specifies a value that we replace when saving to the database when the value of the field is `null`.

This feature only works on Boxed primitive and the `DataClass` equivalent of objects \(such as from TypeConverter\), such as String, Integer, Long, Double, etc. **Note**: If the `DataClass` is a `Blob`, unfortunately this will not work. For `Boolean` classes, use "1" for true, "0" for false.

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
