# Relationships

We can link `@Table` in DBFlow via 1-1, 1-many, or many-to-many. For 1-1 we use
`@PrimaryKey`, for 1-many we use `@OneToMany`, and for many-to-many we use the `@ManyToMany` annotation.


## One To One

DBFlow supports multiple `@ForeignKey` right out of the box as well (and for the most part, they can also be `@PrimaryKey`).

```java
@Table(database = AppDatabase.class)
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
  3. Cannot inherit `@ForeignKey` from non-model classes (see [Inherited Columns](/usage2/Models.md#inherited-columns))


If you create a circular reference (i.e. two tables with strong references to `Model` as `@ForeignKey` to each other), read on.

## Foreign Key Containers

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
@Table(database = AppDatabase.class)
public class Dog extends BaseModel {

    @PrimaryKey
    String name;

    @ForeignKey
    @PrimaryKey
    ForeignKeyContainer<Breed> breed; // tableClass only needed for single-field refs that are not Model.

    @ForeignKey
    ForeignKeyContainer<Owner> owner;

    public void associateOwner(Owner owner) {
        this.owner = FlowManager.getContainerAdapter(Owner.class)
                        .toForeignKeyContainer(owner); // convenience conversion
    }

    public void associateBreed(Breed breed) {
        this.breed = FlowManager.getContainerAdapter(Breed.class)
                      .toForeignKeyContainer(breed); // convenience conversion
    }
}

```

Since `ForeignKeyContainer` only contain fields that are relevant to the relationship,
a handy method in `ModelContainerAdapter` converts an object to the `ForeignKeyContainer` via
`toForeignKeyContainer()`.

## One To Many

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

## Many To Many


In DBFlow many to many is done via source-gen. A simple table:

```java

@Table(database = TestDatabase.class)
@ManyToMany(referencedTable = Follower.class)
public class User extends BaseModel {

    @PrimaryKey
    String name;

    @PrimaryKey
    int id;

}

```

Generates a `@Table` class named `User_Follower`, which DBFlow treats as if you
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

This annotation makes it very easy to generate "join" tables for you to use in the app for a ManyToMany relationship. It only generates the table you need. To use it you must reference it in code as normal.

_Note_: This annotation is only a helper to generate tables that otherwise you
would have to write yourself. It is expected that management still is done by you, the developer.

### Custom Column Names

You can change the name of the columns that are generated. By default they are simply
lower case first letter version of the table name.

`referencedTableColumnName` -> Refers to the referenced table.
`thisTableColumnName` -> Refers to the table that is creating the reference.

### Multiple ManyToMany

You can also specify `@MultipleManyToMany` which enables you to define more
than a single `@ManyToMany` relationship on the table.

A class can use both:

```java
@Table(database = TestDatabase.class)
@ManyToMany(referencedTable = TestModel1.class)
@MultipleManyToMany({@ManyToMany(referencedTable = TestModel2.class),
    @ManyToMany(referencedTable = com.raizlabs.android.dbflow.test.sql.TestModel3.class)})
public class ManyToManyModel extends BaseModel {

    @PrimaryKey
    String name;

    @PrimaryKey
    int id;

    @Column
    char anotherColumn;
}
```
