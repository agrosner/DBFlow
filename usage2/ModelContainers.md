# Model Containers

`ModelContainer` are mirrors to actual `Model` classes represented in another format,
such as `JSON` or as a key-value `Map`. Also they can be `ForeignKeyContainer` (see [here](/usage2/Relationships.md#foreign-key-containers)).

What they do is map the data of one format into the blueprint of `Model` and use that
to directly save the associated data into the database. This skirts the potentially
expensive process of converting JSON into `Model`, convert to `ContentValue` or
bind to `SQLiteStatement`, then finally save into the database.

**Note**: These objects are meant for simple operations and anything complex should be
avoided.

If you define `Model` with JSON or `Map`, you __must__ ensure that all `@PrimaryKey` specified
for the table resides in the data, otherwise something unexpected may occur (such as
  rows changed you did not intend to change).

## Declare a `Model` as supporting ModelContainer

All `Model` classes and associated `@ForeignKey` in the table must define the
`@ModelContainer` annotation, so that the necessary code gets generated. This
is to prevent unnecessary code generation.

```java

@Table(database = AppDatabase.class)
@ModelContainer // generates a ModelContainerAdapter
public class Employee extends BaseModel {

    @PrimaryKey
    String name;

    @Column
    boolean employed;

    @ForeignKey // also must declare annotation in class definition
    JobTitle jobTitle;

}

```

## Built-in Types

DBFlow comes with default implementations of `ModelContainer`. A `ModelContainer`
is simply an interface so you can create and roll your own. We recommend using `SimpleModelContainer`
as a base class, since it simplifies/implements many of the `ModelContainer` methods for you.

The list includes:
  1. `MapModelContainer` - `Map` implementation with default of `HashMap`
  2. `JSONModel` - `JSONObject` conversion
  3. `JSONArrayModel` - contains an array of `JSONModel`, useful for a `JSONArray` top-level object.

## How to Use

Create the object :

```java

JSONModel<MyTable> json = new JSONModel<>(jsonObject, MyTable.class);

```

Operate as if `Model`:

```java

json.save();
json.update();
json.insert();
json.delete();

```

Retrieve / Convert `Model` if needed :

```java

json.toModel(); // caches result
json.toModelForce(); // always converts result.

```

Load a query into a `ModelContainer` and use that data instead!

```java

JSONModel json = SQLite.select()
    .from(MyTable.class)
    .where(...)
    .queryModelContainer(new JSONModel());

json.getData(); // JSONObject prepopulated with data from Cursor

```

## Advanced Features


### Container Key

When you wish to specify a key different from the `Model` variable name, use
`@ContainerKey`:

```java

@Column
@ContainerKey("anotherName")
String someColumn;


```

### Put Default

you can also `putDefault()`, which places a default value for the column when loading
from the database. This is on by default. To turn this off and leave out the data,
set this to `false`. 

### Exclude From toModel() method

When used in a `ModelContainer`, we can exclude certain fields from getting converted
into a `Model` object. Set `@Column(excludeFromToModelMethod = true)` to enable this.
