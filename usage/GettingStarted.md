# Getting Started
In this section we run through how to build a simple database, table, and establish relationship between `Model`.

**The Ant Queen**: We are curious in storing data about our ant colonies. We want to track and tag all ants for a specific colony as well as each queen Ant.

We have this relationship:

```

Colony (1..1) -> Queen (1...many)-> Ants
```

## Setting Up DBFlow
To initialize DBFlow, open databases, and begin migrations and creations, place this code in a custom `Application` class:

```java

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }

}
```

Lastly, add the definition to the manifest (with the name that you chose for your custom application):

```xml

<application
  android:name="{packageName}.ExampleApplication"
  ...>
</application>
```

## Defining our database
In DBFlow, databases are placeholder objects that generate interactions from which tables "connect" themselves to.

We need to define where we store our ant colony:

```java

@Database(name = ColonyDatabase.NAME, version = ColonyDatabase.VERSION)
public class ColonyDatabase {

  public static final String NAME = "Colonies";

  public static final int VERSION = 1;
}
```

For best practices, we create the constants `NAME` and `VERSION` as public, so other components we define for DBFlow can reference it later (if needed).

## Creating our tables and establishing relationships
Now that we have a place to store our data for the ant colonies, we need to explicitly define how the underlying SQL data is stored and what we get is a `Model` that represents that underlying data.

### The Queen Table
We will start and go top-down within the colony. There can be only one queen per colony. We define our database objects using the ORM (object-relational mapping) model. What we do is mark each field we want represented as a database column in a class that corresponds to an underlying database table.

In DBFlow, anything that represents an object that interacts with the database using ORM must implement `Model`. The reason for an interface vs. a baseclass ensures that other kinds of `Model` such as views/virtual tables can conform to the same protocol and not rely on one base class to rule them all. We extend `BaseModel` as a convenience for the standard table to `Model` class.

To properly define a table we must:
1. Mark the class with `@Table` annotation
2. Point the database to the correct database, in this case `ColonyDatabase`
3. Define at least one primary key
4. The class and all of its database columns must be package private or `public`
5. so the generated `_Adapter` class can access it. Note: Columns may be private with getter and setters specified.

The basic definition we can use is:

```java

@Table(database = ColonyDatabase.class)
public class Queen extends BaseModel {

  @Column
  @PrimaryKey(autoincrement = true)
  long id;

  @Column
  String name;

}
```

So we have a queen ant definition, and now we need to define a `Colony` for the queen.

### The Colony

```java
@ModelContainer
@Table(database = ColonyDatabase.class)
public class Colony extends BaseModel {

  @Column
  @PrimaryKey(autoincrement = true)
  long id;

  @Column
  String name;

}
```

Now that we have a `Queen` and `Colony` table, we want to establish a 1-1 relationship. We want the database to care when data is removed, such as if a fire occurs and destroys the `Colony`. When the `Colony` is destroyed, we assume the `Queen` no longer exists, so we want to "kill" the `Queen` for that `Colony` so it no longer exists.

### 1-1 Relationships
To establish the connection, we will define a Foreign Key that the child, `Queen` uses:

```java

@ModelContainer
@Table(database = ColonyDatabase.class)
public class Queen extends BaseModel {

  //...previous code here

  @Column
  @ForeignKey(saveForeignKeyModel = false)
  Colony colony;

}
```

Defining the Foreign Key as a `Model` will automatically load the relationship when loading from the database using a query on the value in that column. For performance reasons we use `saveForeignKeyModel=false` to not save the parent `Colony` when the `Queen` object is saved.

As of 3.0, we no longer need to explicitly define the `@ForeignKeyReference` for each referenced column. DBFlow will know and add them automatically to the table definitions.

The `@ModelContainer` annotation is _required_ for any `Model` referenced as a `ForeignKeyContainer`. This is to limit the generation of unnecessary code if not used.

### The Ant Table + 1-to-Many
Now that we have a `Colony` with a `Queen` that belongs to it, we need some ants to serve her!

```java

@Table(database = ColonyDatabase.class)
public class Ant extends BaseModel {

  @Column
  @PrimaryKey(autoincrement = true)
  long id;

  @Column
  String type;

  @Column
  boolean isMale;

  @Column
  @ForeignKey(saveForeignKeyModel = false)
  ForeignKeyContainer<Queen> queenForeignKeyContainer;

  /**
  * Example of setting the model for the queen.
  */
  public void associateQueen(Queen queen) {
    queenForeignKeyContainer = FlowManager.getContainerAdapter(Queen.class).toForeignKeyContainer(queen);
  }
}
```

We have the `type`, which can be "worker", "mater", or "other". Also if the ant is male or female.

We use a `ForeignKeyContainer` in this instance, since we can have thousands of ants. For performance reasons this will "lazy-load" the relationship of the `Queen` and only run the query on the DB for the `Queen` when we call `toModel()`.

Since `ModelContainer` usage is not generated by default, we must add the `@ModelContainer` annotation to the `Queen` class in order to use for a `ForeignKeyContainer`.

 Next, we establish the 1-to-many relationship by lazy-loading the ants for performance reasons:

```java

@ModelContainer
@Table(database = ColonyDatabase.class)
public class Queen extends BaseModel {
  //...

  // needs to be accessible for DELETE
  List<Ant> ants;

  @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "ants")
  public List<Ant> getMyAnts() {
    if(ants == null) {
      ants = new Select()
              .from(Ant.class)
              .where(Condition.column(Ant$Table.QUEENFOREIGNKEYCONTAINER_QUEEN_ID).is(id))
              .queryList();
    }
    return ants;
  }
}
```

If you wish to lazy-load the relationship yourself, specify `OneToMany.Method.DELETE` and `SAVE` instead of `ALL`. If you wish not to save them whenever the `Queen`'s data changes, specify `DELETE` and `LOAD` only.
