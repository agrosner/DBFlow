# DBFlow 3.0 Migration Guide
DBFlow has undergone the most _significant_ changes in its lifetime in 3.0. This guide is meant to assist you in migrating from 2.1.x and above and _may not_ be fully inclusive of all changes. This doc will mention the most glaring and significant changes.

A significant portion of the changes include the _complete_ overhaul of the underlying annotation processor, leading to wonderful improvements in maintainability of the code, readability, and stability of the generated code. Now it uses the updated [JavaPoet](https://github.com/square/javapoet) vs the outdated JavaWriter. The changes in this library alone _significantly_ helps out the stability of the generated code.

_note:_
1. `update` no longer attempts to `insert` if it fails.
2. Package private fields from other packages are now automatically accessible via generated `_Helper` classes. The referenced fields must be annotated with `@Column`. if its a legacy `ForeignKeyReference`, `referendFieldIsPackagePrivate()` must be set to true.

## Table Of Contents
1. Database + Table Structure
2. Properties, Conditions, Queries, Replacement of ConditionQueryBuilder and more
3. ModelContainers
4. ModelViews
5. Indexes

## Database + Table Structure
### Database changes
The default `generatedClassSeparator` is now `_` instead of `$` to play nice with Kotlin by default. A simple addition of:

```java

@Database(generatedClassSeparator = "$")
```

will keep your generated "Table" and other classes the same name.

Globally, we no longer reference what `@Database` any database-specific element (Table, Migration, etc) by `String` name, but by `Class` now.

Before:

```java

@Table(databaseName = AppDatabase.NAME)
@Migration(databaseName = AppDatabase.NAME)
```

After:

```java

@Table(database = AppDatabase.class)
@Migration(database = AppDatabase.class)
```

Why: We decided that referencing it directly by class name enforces type-safety and direct enforcement of the database placeholder class. Previously,

```java

@Table(databaseName = "AppDatabase")
```

was a valid specifier.

## Table Changes
@Table have some significant changes.

Instead of generating just `String` column name within a corresponding `$Table` class, it now generates `Property` fields. These fields are significantly smarter and more powerful. They considerably aid in the simplification of many complex queries and make the code in general more readable, type-safe, and just overall better. _NOTE: the properties are no longer capitalized, rather they match exact casing of  the Column name._

Previously, when you defined a class as:

```java

@Table(databaseName = TestDatabase.NAME)
@ModelContainer
public class TestModel2 extends BaseModel {

  @Column
  @PrimaryKey
  String name;

  @Column(name = "model_order")
  int order;
}
```

It generated a `TestModel2$Table` class:

```java

public final class TestModel2_Table {

  public static final String NAME = "name";

  public static final String MODEL_ORDER = "model_order";
}
```

Now when you define a class, it generates a definition as follows:

```java
public final class TestModel2_Table {
  public static final Property<String> name = new Property<String>(TestModel2.class, "name");

  public static final IntProperty model_order = new IntProperty(TestModel2.class, "model_order");

  public static BaseProperty getProperty(String columnName) {
    columnName = QueryBuilder.quoteIfNeeded(columnName);
    switch (columnName)  {
      case "`name`":  {
        return name;
      }
      case "`model_order`":  {
        return model_order;
      }
      default:  {
        throw new IllegalArgumentException("Invalid column name passed. Ensure you are calling the correct table's column");
      }
    }
  }
}
```

Each `Property` now is used for each all references to a column in query statements.

The `getProperty()` method allows to keep compatibility with the old format as well as solve some `ContentProvider` compatibility issues.

To read on how these properties interact read "Properties, Conditions, Queries, Replacement of ConditionQueryBuilder and more".

### Index changes
Added was an `IndexGroup[]` of `indexGroups()`.

Now we can generate `IndexProperty` (see properties for more information), which provide us a convenient generated `Index` to use for the table. This then is used in a queries that rely on indexes and make it dead simple to activate and deactivate indexes.

A class written like:

```java
@Table(database = TestDatabase.class,
        indexGroups = {
                @IndexGroup(number = 1, name = "firstIndex"),
                @IndexGroup(number = 2, name = "secondIndex"),
                @IndexGroup(number = 3, name = "thirdIndex")
        })
public class IndexModel2 extends BaseModel {

    @Index(indexGroups = {1, 2, 3})
    @PrimaryKey
    @Column
    int id;

    @Index(indexGroups = 1)
    @Column
    String first_name;

    @Index(indexGroups = 2)
    @Column
    String last_name;

    @Index(indexGroups = {1, 3})
    @Column
    Date created_date;

    @Index(indexGroups = {2, 3})
    @Column
    boolean isPro;
}
```

Generates in its "Table" class:

```java
public final class IndexModel2_Table {
  //...previous code omitted

  public static final IndexProperty<IndexModel2> index_firstIndex = new IndexProperty<>("firstIndex", false, IndexModel2.class, id, first_name, created_date);

  public static final IndexProperty<IndexModel2> index_secondIndex = new IndexProperty<>("secondIndex", false, IndexModel2.class, id, last_name, isPro);

  public static final IndexProperty<IndexModel2> index_thirdIndex = new IndexProperty<>("thirdIndex", false, IndexModel2.class, id, created_date, isPro);
```

### Foreign Key Changes
`@ForeignKey` fields no longer need to specify it's references!!! The old way still works, but is no longer necessary for `Model`-based ForeignKeys. The annotation processor takes the primary keys of the referenced table and generates a column with {fieldName}_{referencedColumnName} that represents the same SQLite Type of the field.<br>_Note: that is not backwards compatible_ with apps already with references.

Going forward with new tables, you can leave them out.

Previously:

```java
@Table(database = TestDatabase.class)
public class ForeignInteractionModel extends TestModel1 {

    @Column
    @ForeignKey(
            onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.CASCADE,
            references =
                    {@ForeignKeyReference(columnName = "testmodel_id",
                                          foreignColumnName = "name",
                                          columnType = String.class),
                            @ForeignKeyReference(columnName = "testmodel_type",
                                                 foreignColumnName = "type",
                                                 columnType = String.class)},
            saveForeignKeyModel = false)
    ForeignKeyContainer<ParentModel> testModel1;
}
```

Now:

```java
@Table(database = TestDatabase.class)
public class ForeignInteractionModel extends TestModel1 {

    @Column
    @ForeignKey(
            onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.CASCADE,
            saveForeignKeyModel = false)
    ForeignKeyContainer<ParentModel> testModel1;
}
```

The result is _significantly_ cleaner and less overhead to maintain.

If you wish to keep old references, please keep in mind that `foreignColumnName` is now `foreignKeyColumnName`.

## Properties, Conditions, Queries, Replacement of ConditionQueryBuilder and more
Perhaps the most significant external change to this library is making queries, conditions, and interactions with the database much stricter and more type-safe.

### Properties
Properties replace `String` column names generated in the "$Table" classes. They also match exact case to the column name. They have methods that generate `Condition` that drastically simplify queries. (Please note the `Condition` class has moved to the `.language` package).

Properties are represented by the interface `IProperty` which are subclassed into `Property<T>`, `Method`, and the primitive properties (`IntProperty`, `CharProperty`, etc).

It will become apparent why this change was necessary with some examples:

A relatively simple query by SQLite standards:

```sql
SELECT `name` AS `employee_name`, AVG(`salary`) AS `average_salary`, `order`, SUM(`salary`) as `sum_salary`
  FROM `SomeTable`
  WHERE `salary` > 150000
```

Before:

```java
List<SomeQueryTable> items =
  new Select(ColumnAlias.column(SomeTable$Table.NAME).as("employee_name"),
    ColumnAlias.columnsWithFunction("AVG", SomeTable$Table.SALARY).as("average_salary"),
    SomeTable$Table.ORDER,
    ColumnAlias.columnsWithFunction("SUM", SomeTable$Table.SALARY).as("sum_salary"))
  .from(SomeTable.class)
  .where(Condition.column(SomeTable$Table.SALARY).greaterThan(150000))
  .queryCustomList(SomeQueryTable.class);
```

Now (with static import on `SomeTable_Table` and `Method` ):

```java
List<SomeQueryTable> items =
  SQLite.select(name.as("employee_name"),
    avg(salary).as("average_salary"),
    order,
    sum(salary).as("sum_salary"))
  .from(SomeTable.class)
  .where(salary.greaterThan(150000))
  .queryCustomList(SomeQueryTable.class);
```

The code instantly becomes cleaner, and reads more like an actual query.

### Replacement of the ConditionQueryBuilder
ConditionQueryBuilder was fundamentally flawed. It represented a group of `Condition`, required a `Table`, yet extended `QueryBuilder`, meaning arbitrary `String` information could be appended to it, leading to potential for messy piece of query.

It has been replaced with the `ConditionGroup` class. This class represents an arbitrary group of `SQLCondition` in which it's sole purpose is to group together `SQLCondition`. Even better a `ConditionGroup` itself is a `SQLCondition`, meaning it can _nest_ inside of other `ConditionGroup` to allow complicated and insane queries.

Now you can take this:

```sql
SELECT FROM `SomeTable` WHERE `latitude` > 0 AND (`longitude` > 50 OR `longitude` < 25) AND `name`='MyHome'
```

and turn it into:

```java
SQLite.select()
  .from(SomeTable.class)
  .where(SomeTable_Table.latitude.greaterThan(0))
  .and(ConditionGroup.clause()
    .and(SomeTable_Table.longitude.greaterThan(50))
    .or(SomeTable_Table.longitude.lessThan(25)))
  .and(SomeTable_Table.name.eq("MyHome"))
```

## ModelContainer Changes
Now `ModelContainer` objects have a multitude of type-safe methods to ensure that they  can convert their contained object's data into the field they associate with. What  this means is that if our `Model` has a `long` field, while the data object for  the `ModelContainer` has a `Integer` object. Previously, we would get a classcastexception. Now what it does is "coerce" the value into the type you need.  Supported Types:
1. Integer/int
2. Double/Double
3. Boolean/boolean
4. Short/short
5. Long/long
6. Float/Float
7. String
8. Blob/byte[]/Byte[]
9. Byte/byte
10. Using TypeConverter to retrieve value safely.

You can now `queryModelContainer` from the database to retrieve a single `Model` into `ModelContainer` format instead of into `Model` and then `ModelContainer`:

```java

JSONModel model = SQLite.select().from(SomeTable.class).where(SomeTable_Table.id.eq(5)).queryModelContainer(new JSONModel());
// has data now
```
