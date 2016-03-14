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
potentially many databases. The name, `AppDatabaseAppDatabase_Database.java`, is generated
via {DatabaseClassName}{DatabaseFileName}{GeneratedClassSepator, default = "\_"}Database

To learn more about what you can configure in a database (such as SQLCipher), read [here](/usage2/Databases.md)

## Initialize FlowManager

DBFlow needs an instance of `Context` in order to use it for a few features such
as reading from assets, content observing, and generating `ContentProvider`.

Initialize in your `Application` subclass. You can also initialize it from other
`Context` but we always grab the `Application` `Context` (this is done only once).

```java
public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }
}

```

Finally, add the definition to the manifest (with the name that you chose for your custom application):
```xml
<application
  android:name="{packageName}.ExampleApplication"
  ...>
</application>
```

A database within DBFlow is only initialized once you call `FlowManager.getDatabase(SomeDatabase.NAME).getWritableDatabase()`. If you
don't want this behavior or prefer it to happen immediately, modify your `ExampleApplication`:

```java

@Override
public void onCreate() {
    super.onCreate();
    FlowManager.init(this);
    // create database and begin migrations, etc.
    FlowManager.getDatabase(SomeDatabase.NAME).getWritableDatabase();
}

```


## Create Models

All your database tables _must_ implement `Model`, which is simply an interface:

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

As a convenience (and recommended for most uses), you should extend `BaseModel`, which provides the default implementation. If for some reason you must implement `Model`, you should reference its implementation. **Also** you don't need to directly extend `BaseModel`, in fact you can extend other tables to combine their columns. However those fields must be package-private, public, or private with accessible java-bean getters and setters.

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

Columns can be `public`, package-private, or `private` with java-bean-style getters and setters (must be public).

Here is an example of a "nice" `private` field:

```java

public class Dog extends BaseModel {

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

**Supported Types**:
1. all java primitives including `char`,`byte`, `short`, and `boolean`.
2. All java boxed primitive classes
3. String, Date, java.sql.Date, Calendar, Blob, Boolean
4. Custom data types via a [TypeConverter](/usage2/TypeConverters.md)
5. `Model`/`ModelContainer` as fields, but only as `@PrimaryKey` and/or `@ForeignKey`

**Unsupported Types**:
1. `List<T>` : List columns are not supported and not generally proper for a relational database. However, you can get away with a non-generic `List` column via a `TypeConverter`. But again, avoid this if you can.
2. Anything that is generically typed (even with an associated `TypeConverter`), **except** `ModelContainer` and `ForeignKeyContainer` fields. If you need to include the field, subclass the generic object and provide a `TypeConverter`.

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

public class Dog extends BaseModel {

  @PrimaryKey
  String name;

  @PrimaryKey
  String breed;

}

```

If we want an autoincrementing key, you specify `@PrimaryKey(autoincrement = true)`, but only one of these kind can exist in a table and you cannot mix with regular primary keys.

## Relationships

We can link `@Table` in DBFlow via 1-1, 1-many, or many-to-many. For 1-1 we use
`@PrimaryKey`, for 1-many we use `@OneToMany`, and for many-to-many we use the `@ManyToMany` annotation.


### One To One

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

`@ForeignKey` can only be a subset of types:
1. `Model` or `ModelContainer`
2. Any field not requiring a `TypeConverter`. If not a `Model`, you _must_ specify the `tableClass` it points to.
3. Cannot inherit `@ForeignKey` from non-model classes (see [Inherited Columns](#inherited-columns))


If you create a circular reference (i.e. two tables with strong references to `Model` as `@ForeignKey` to each other), read on.

### Foreign Key Containers

For efficiency reasons we recommend using `ForeignKeyContainer<>`. A `ForeignKeyContainer`
is foreign key that only contains the foreign key reference data within itself. If you
desire thread-safety and prefer this to happen immediately, replace with the `Model` object.

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

__Note__: using `ForeignKeyContainer` also helps to prevent circular references that can
get you in a `StackOverFlowError` if two tables strongly reference each other in `@ForeignKey`.

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

### One To Many

In DBFlow, `@OneToMany` is an annotation that you provide to a method in your `Model` class that will allow management of those objects during CRUD operations.
This can allow you to combine a relationship of objects to a single `Model` to happen together on load, save, insert, update, and deletion.

```java

@ModelContainer
@Table(database = ColonyDatabase.class)
public class Queen extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;

    @Column
    @ForeignKey(saveForeignKeyModel = false)
    Colony colony;

    List<Ant> ants;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "ants")
    public List<Ant> getMyAnts() {
        if (ants == null || ants.isEmpty()) {
            ants = SQLite.select()
                .from(Ant.class)
                .where(Ant_Table.queenForeignKeyContainer_id.eq(id))
                .queryList();
        }
        return ants;
    }
}

```

### Many To Many


In DBFlow many to many is done via source-gen. A simple table:

```java

@Table(database = TestDatabase.class)
@ManyToMany(referencedTable = Followers.class)
public class User extends BaseModel {

    @PrimaryKey
    String name;

    @PrimaryKey
    int id;

}

```

Generates a `@Table` class named `User_Followers`, which DBFlow treats as if you
coded the class yourself!:

```java

@Table(
    database = TestDatabase.class
)
public final class User_Follower extends BaseModel {
  @PrimaryKey(
      autoincrement = true
  )
  long _id;

  @ForeignKey(
      saveForeignKeyModel = false
  )
  Follower follower;

  @ForeignKey(
      saveForeignKeyModel = false
  )
  User user;

  public final long getId() {
    return _id;
  }

  public final Followers getFollower() {
    return follower;
  }

  public final void setFollower(Follower param) {
    follower = param;
  }

  public final Users getUser() {
    return user;
  }

  public final void setUser(User param) {
    user = param;
  }
}

```

This annotation makes it very easy to generate "join" tables for you to use in the app for a ManyToMany relationship.
