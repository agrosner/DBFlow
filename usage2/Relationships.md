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
  1. `Model`
  2. Any field not requiring a `TypeConverter`. If not a `Model` or a table class, you _must_ specify the `tableClass` it points to.
  3. Cannot inherit `@ForeignKey` from non-model classes (see [Inherited Columns](Models.md#inherited-columns))

If you create a circular reference (i.e. two tables with strong references to `Model` as `@ForeignKey` to each other), read on.

## Stubbed Relationships

For efficiency reasons we recommend specifying `@ForeignKey(stubbedRelationship = true)`. What this will do is only _preset_ the primary key references into a table object. All other fields will not be set. If you need to access the full object, you will have to call `load()` for `Model`, or use the `ModelAdapter` to load the object from the DB.

From our previous example of `Dog`, instead of using a  `String` field for **breed**
we recommended by using a `Breed`. It is nearly identical, but the difference being
we would then only need to call `load()` on the reference and it would query the `Breed`
table for a row with the `breed` id. This also makes it easier if the table you
reference has multiple primary keys, since DBFlow will handle the work for you.

Multiple calls to `load()` will query the DB every time, so call when needed. Also if you don't specify `@Database(foreignKeyConstraintsEnforced = true)`, calling `load()` may not have any effect. Essentially without enforcing `@ForeignKey` at a SQLite level, you can end up with floating key references that do not exist in the referenced table.

In normal circumstances, for every load of a `Dog` object from the database,
we would also do a load of related `Owner`. This means that even if multiple `Dog` say (50)
all point to same owner we end up doing 2x retrievals for every load of `Dog`. Replacing
that model field of `Owner` with a stubbed relationship prevents the extra N lookup time,
leading to much faster loads of `Dog`.

__Note__: using stubbed relationships also helps to prevent circular references that can
get you in a `StackOverFlowError` if two tables strongly reference each other in `@ForeignKey`.

Our modified example now looks like this:

```java
@Table(database = AppDatabase.class)
public class Dog extends BaseModel {

    @PrimaryKey
    String name;

    @ForeignKey(stubbedRelationship = true)
    @PrimaryKey
    Breed breed; // tableClass only needed for single-field refs that are not Model.

    @ForeignKey(stubbedRelationship = true)
    Owner owner;
}

```

## One To Many

In DBFlow, `@OneToMany` is an annotation that you provide to a method in your `Model` class that will allow management of those objects during CRUD operations.
This can allow you to combine a relationship of objects to a single `Model` to happen together on load, save, insert, update, and deletion.

```java

@Table(database = ColonyDatabase.class)
public class Queen extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;

    @ForeignKey(saveForeignKeyModel = false)
    Colony colony;

    List<Ant> ants;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "ants")
    public List<Ant> getMyAnts() {
        if (ants == null || ants.isEmpty()) {
            ants = SQLite.select()
                .from(Ant.class)
                .where(Ant_Table.queen_id.eq(id))
                .queryList();
        }
        return ants;
    }
}

```

### Efficient Methods
When using `@ManyToMany`, by default we skip the `Model` methods in each retrieved `Ant` (in this example).
If you have nested `@ManyToMany` (which should strongly be avoided), you can turn off the efficient operations.
Call `@OneToMany(efficientMethods = false)` and it will instead loop through each model and perform `save()`, `delete()`, etc
when the parent model is called.

### Kotlin Support

Also, with DBFlow Kotlin extensions, we can make this much more concise:
```kotlin

@get:OneToMany(methods = arrayOf(OneToMany.Method.ALL))
var ants by oneToMany { select from Ant::class where (Ant_Table.queen_id.eq(id)) }
```

This creates a delegated property which does the same operation as above. The only difference is the
variable is nullable.

### Custom ForeignKeyReferences
When simple `@ForeignKey` annotation is not enough, you can manually specify references for your table:

```java

@ForeignKey(saveForeignKeyModel = false,
references = {@ForeignKeyReference(columnName = "colony", foreignKeyColumnName = "id")})
Colony colony;

```

By default not specifying references will take each field and append "${foreignKeyFieldName}_${ForeignKeyReferenceColumnName}" to make the reference column name. So by default the previous example would use `colony_id` without references. With references it becomes `colony`.

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
    @ManyToMany(referencedTable = com.dbflow5.test.sql.TestModel3.class)})
public class ManyToManyModel extends BaseModel {

    @PrimaryKey
    String name;

    @PrimaryKey
    int id;

    @Column
    char anotherColumn;
}
```
