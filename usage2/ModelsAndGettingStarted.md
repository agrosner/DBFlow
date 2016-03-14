# Models and Getting Started

This section describes how Models and tables are constructed via DBFlow. first
let's describe how to get a database up and running.

## Creating a Database

In DBFlow, creating a database is as simple as only a few lines of code. DBFlow
supports any number of databases, however individual tables and other related files
can only be associated with one database.

```java

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

  public static final String NAME = "AppDatabase"; // we will add the .db extension

  public static final String VERSION = 1;
}


```

Writing this file generates (by default) a `AppDatabaseAppDatabase_Database.java`
file, which contains tables, views, and more all tied to a specific database. This
class is automatically placed into the main `GeneratedDatabaseHolder`, which holds
potentially many databases.

To learn more about what you can configure in a database, read [here](/usage2/Databases.md)

## Create Models

All your database tables __must__ impement `Model`, which is simply an interface:

```java

public interface Model {

    /**
     * Saves the object in the DB.
     */
    void save();

    /**
     * Deletes the object in the DB
     */
    void delete();

    /**
     * Updates an object in the DB. Does not insert on failure.
     */
    void update();

    /**
     * Inserts the object into the DB
     */
    void insert();

    /**
     * @return true if this object exists in the DB. It combines all of it's primary key fields
     * into a SELECT query and checks to see if any results occur.
     */
    boolean exists();

```

As a convenience (and recommended for most uses), you should extend `BaseModel`, which provides the default implementation. You can also reference this file if for some reason you must implement `Model`. **Also** you don't need to directly extend `BaseModel`, in fact you can extend other tables to combine their columns. However those fields must be package-private, public, or private with accessible java-bean getters and setters.

An example:

```java


@Table(database = TestDatabase.class)
public class Currency extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id; // package-private recommended, not required

    @Column
    @Unique
    String symbol;

    @Column
    String shortName;

    @Column
    @Unique
    private String name; // private with getters and setters

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
}

```

## Columns

We, by default lazy look for columns, meaning they all must contain either `@PrimaryKey`, `@Column`, or `@ForeignKey` to be used in tables. If you wish to include all fields, set `@Table(allFields = true)`.

Columns can be `public`, package-private, or `private` with java-bean-style getters and setters.

**Supported Types**:
1. all java primitives including `char`,`byte`, `short`, and `boolean`.
2. All java boxed primitive classes
3. String, Date, java.sql.Date, Calendar, Blob, Boolean
4. Custom data types via a [TypeConverter](/usage2/TypeConverters.md)
5. `Model`/`ModelContainer` as fields, but only as `@PrimaryKey` and/or `@ForeignKey`

**Unsupported Types**:
1. `List<T>` : List columns are not supported and not generally proper for a relational database.
2. Anything that is generically typed (even with an associated `TypeConverter`), **except** `ModelContainer` and `ForeignKeyContainer` fields.

Since we don't require extension on `BaseModel` directly, tables can extend non-model classes in inherit their fields directly (given proper accessibility) via the `@InheritedColumn` annotation (or `@InheritedPrimaryKey` for primary keys):

```java

@Table(database = AppDatabase.class,
        inheritedColumns = {@InheritedColumn(column = @Column, fieldName = "name"),
                @InheritedColumn(column = @Column, fieldName = "number")},
        inheritedPrimaryKeys = {@InheritedPrimaryKey(column = @Column,
                primaryKey = @PrimaryKey,
                fieldName = "inherited_primary_key")})
public class InheritorModel extends InheritedModel implements Model {

```

## Primary keys

DBFlow supports multiple primary keys, right out of the box. Simply create a table with multiple `@PrimaryKey`:

```java

public class Dog extends BaseModel {

  @PrimaryKey
  String name;

  @PrimaryKey
  String breed;

}

```

If we want an autoincrementing key, you specify `@PrimaryKey(autoincrement = true)`, but only one of these kind can exist in a table and you cannot mix with regular primary keys.

## Foreign keys

DBFlow supports multiple `@ForeignKey` right out of the box as well (and for the most part, they can also be `@PrimaryKey`).

```java

public class Dog extends BaseModel {

  @PrimaryKey
  String name;

  @ForeignKey(tableClass = Breed.class)
  @PrimaryKey
  String breed;

  @ForeignKey
  Owner owner;
}

```

### Foreign Key Containers

For efficiency reasons we recommend using `ForeignKeyContainer<>`. A `ForeignKeyContainer`
is foreign key that only contains the foreign key reference data within itself.

From our previous example of `Dog`, instead of using a  `String` field for **breed**
we recommended by using a `ForeignKeyContainer<Breed>`. It is nearly identical, but the difference being
we would then only need to call `load()` on the reference and it would query the `Breed`
table for a row with the `breed` id. This also makes it easier if the table you
reference has multiple primary keys, since DBFlow will handle the work for you.

Multiple calls to `load()` will have no performance impact,
as the reference will cache the relationship. If you need to get up-to-date data, use `reload()`.

Second, for every load of a `Dog` object from the database,
we would also do a load of related `Owner`. This means that even if multiple `Dog` say (50)
all point to same owner we end up doing 2x retrievals for every load of `Dog`. Replacing
that model field of `Owner` with `ForeignKeyContainer<Owner>` prevents the extra N lookup time,
leading to much faster loads of `Dog`.

Our modified example now looks like this:

```java

public class Dog extends BaseModel {

  @PrimaryKey
  String name;

  @ForeignKey
  @PrimaryKey
  ForeignKeyContainer<Breed> breed; // tableClass only needed for single-field refs that are not Model.

  @ForeignKey
  ForeignKeyContainer<Owner> owner;

  public void associateOwner(Owner owner) {
    owner = FlowManager.getContainerAdapter(Owner.class).toForeignKeyContainer(owner); // convenience conversion
  }

  public void associateBreed(Breed breed) {
    owner = FlowManager.getContainerAdapter(Breed.class).toForeignKeyContainer(breed); // convenience conversion
  }
}

```

Since `ForeignKeyContainer` only contain fields that are relevant to the relationship,
a handy method in `ModelContainerAdapter` converts an object to the `ForeignKeyContainer` via
`toForeignKeyContainer()`.
