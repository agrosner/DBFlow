# DBFlow 3.0 Migration Guide
DBFlow has undergone the most _significant_ changes in its lifetime in 3.0. This guide is meant to assist you in migrating from 2.1.x and above and _may not_ be fully inclusive of all changes. This doc will mention the most glaring and significant changes. If in doubt, consult the usage2 docs.

A significant portion of the changes include the _complete_ overhaul of the underlying annotation processor, leading to wonderful improvements in maintainability of the code, readability, and stability of the generated code. Now it uses the updated [JavaPoet](https://github.com/square/javapoet) vs the outdated JavaWriter. The changes in this library alone _significantly_ helps out the stability of the generated code.

_Some Changes to Note:_
1. `update` no longer attempts to `insert` if it fails.
2. Package private fields from other packages are now automatically accessible via generated `_Helper` classes. The referenced fields must be annotated with `@Column`, `@PrimaryKey`, or `@ForeignKey`. if its a legacy `ForeignKeyReference`, `referendFieldIsPackagePrivate()` must be set to true.
3. `@Column` no longer required in conjunction with `@PrimaryKey` or `@ForeignKey`
4. Can now have DBFlow in multiple modules, libraries, etc via "Modules"!
5. `TransactionManager` has been replaced with a new per-database `BaseTransactionManager`. Each DB has its own `DBTransactionQueue` and you can replace the default with your own system. Also, no longer is this priority-based, but rather order-based. See more [here](/usage2/Transactions.md)

This doc is to provide some basic examples of what has changed, but read all of the new usage docs!
Starting with [Intro](/usage2/Intro.md)

## Table Of Contents
  1. [Initialization](/usage2/Migration3Guide.md#initialization)
  2. [Database + Table Structure](/usage2/Migration3Guide.md#database-and-table-structure)
  3. [Transactions Overhaul](/usage2/Migration3Guide.md#transactions-overhaul)
  4. [Properties](/usage2/Migration3Guide.md#properties)
  5. [ModelContainers](/usage2/Migration3Guide.md#modelcontainers)
  6. [ModelViews](/usage2/Migration3Guide.md#modelviews)
  7. [Caching](/usage2/Migration3Guide.md#caching)
  8. [Database Modules](/usage2/Migration3Guide.md#database-modules)

## Initialization

Previously DBFlow was intialized via:

```java
public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }
}

```

Now we use the `FlowConfig.Builder`:

```java
public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(new FlowConfig.Builder(this).build());
    }
}

```

See more of what you can customize [here](/usage2/GettingStarted.md)

## Database And Table Structure
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

was a valid specifier, which might lead to typos or errors.

## Table Changes
`@Table` have some significant changes.

Private boolean fields by default have changed.
`useIsForPrivateBooleans()` has changed to `useBooleanGetterSetters()`. By default
this is enabled, meaning `boolean` variables follow the convention:

```java

private boolean isEnabled;

public boolean isEnabled() {
  return isEnabled;
}

public void setEnabled(boolean isEnabled) {
  this.isEnabled = isEnabled;
}

```

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

  public static final IProperty[] getAllColumnProperties() {
   return new IProperty[]{name,model_order};
  }

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

The `getProperty()` method allows to keep compatibility with the old format, solve some `ContentProvider` compatibility issues, or allow looking up `Property` by key.

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
`@ForeignKey` fields no longer need to specify it's references or the `@Column` annotation!!! The old way still works, but is no longer necessary for `Model`-based ForeignKeys. The annotation processor takes the primary keys of the referenced table and generates a column with {fieldName}_{referencedColumnName} that represents the same SQLite Type of the field.<br>_Note: that is not backwards compatible_ with apps already with references.

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

    @ForeignKey(
            onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.CASCADE,
            saveForeignKeyModel = false)
    ForeignKeyContainer<ParentModel> testModel1;
}
```

The result is _significantly_ cleaner and less overhead to maintain.

If you wish to keep old references, please keep in mind that `foreignColumnName` is now `foreignKeyColumnName`.

## Transactions Overhaul

In 3.0, Transactions got a serious facelift and should be easier to use and handle.
Also their logic and use are much more consolidated a focused. There is no longer
just one `TransactionManager`, rather each database has its own instance so that
operations between databases don't interfere.

### Inserting Data
The format of how to declare them has changed:
Previously to run a transaction, you had to set it up as so:

```java
ProcessModelInfo<SomeModel> processModelInfo = ProcessModelInfo<SomeModel>.withModels(models)
                                                                          .result(resultReceiver)
                                                                          .info(myInfo);
TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(processModelInfo))
TransactionManager.getInstance().addTransaction(new UpdateModelListTransaction<>(processModelInfo))
TransactionManager.getInstance().addTransaction(new DeleteModelListTransaction<>(processModelInfo))


```

In 3.0, we have dropped the individual transaction types, use a new builder notation,
and with _every_ `Transaction` you get completion and error handling:

```java

FlowManager.getDatabase(AppDatabase.class)
          .beginTransactionAsync(new ProcessModelTransaction.Builder<>(
          new ProcessModelTransaction.ProcessModel<Model>() {
              @Override
              public void processModel(Model model) {

              }
          }).build())
          .error(new Transaction.Error() {
              @Override
              public void onError(Transaction transaction, Throwable error) {

              }
          })
          .success(new Transaction.Success() {
              @Override
              public void onSuccess(Transaction transaction) {

              }
          }).build().execute();

```

One thing to note about the `Transaction.Error` is that if specified, _all_ exceptions
are caught and passed to the callback, otherwise any exception that happens in the Transaction system
gets thrown.

You still can use the `DBBatchSaveQueue` for batch saves:

Previously:

```java

TransactionManager.getInstance().saveOnSaveQueue(models);

```

In 3.0:

```java

FlowManager.getDatabase(AppDatabase.class).getTransactionManager()
  .getSaveQueue().addAll(models);


```

### Querying Data

Previously when you queried data you have a few different classes that did almost same thing
such as `SelectListTransaction`, `BaseResultTransaction`, `QueryTransaction`, etc.
3.0 consolidates these into much simpler operations via:

Previously:

```java

TransactionManager.getInstance().addTransaction(new SelectListTransaction<>(new TransactionListenerAdapter<TestModel.class>() {
     @Override
    public void onResultReceived(List<TestModel> testModels) {

    }
  }, TestModel.class, condition1, condition2,..);


```
In 3.0:

```java

database.beginTransactionAsync(
            new QueryTransaction.Builder<>(
                SQLite.select().from(TestModel1.class))
                .queryResult(new QueryTransaction.QueryResultCallback<TestModel1>() {
                    @Override
                    public void onQueryResult(QueryTransaction transaction, @NonNull CursorResult<TestModel1> result) {

                    }
                }).build()).build();

```

The `QueryResultCallback` gives back a `CursorResult`, which is a wrapper around abstract
`Cursor` that lets you retrieve easily `Models` from that cursor:

```java
List<TestModel1> models = result.toListClose();
TestModel1 singleModel = result.toModelClose();
```

Just ensure that you close the `Cursor`.

### Callback Changes

With 3.0, we modified the callback for a `Transaction`. Instead of having the
3 methods:

```java
public interface TransactionListener<ResultClass> {

    void onResultReceived(ResultClass result);

    boolean onReady(BaseTransaction<ResultClass> transaction);

    boolean hasResult(BaseTransaction<ResultClass> transaction, ResultClass result);

}
```

Each `Transaction` automatically gives you ability to handle callbacks:

```java


FlowManager.getDatabase(AppDatabase.class)
          .beginTransactionAsync(new ITransaction() {
                @Override
                public void execute(DatabaseWrapper databaseWrapper) {
                    // do anything you want here.
                }
            }).build())
          .error(new Transaction.Error() {
              @Override
              public void onError(Transaction transaction, Throwable error) {

              }
          })
          .success(new Transaction.Success() {
              @Override
              public void onSuccess(Transaction transaction) {

              }
          }).build().execute();

```

For more usage on the new system, including the ability to roll your own `TransactionManager`,
visit [Transactions](/usage2/Transactions.md)


## Properties
Perhaps the most significant external change to this library is making queries, conditions, and interactions with the database much stricter and more type-safe.

### Property
Properties replace `String` column names generated in the "$Table" classes. They also match exact case to the column name. They have methods that generate `Condition` that drastically simplify queries. (Please note the `Condition` class has moved to the `.language` package).

Properties are represented by the interface `IProperty` which are subclassed into `Property<T>`, `Method`, and the primitive properties (`IntProperty`, `CharProperty`, etc).

Properties can also be represented by values via the `PropertyFactory` class, enabling  values to appear first in queries:

```java
PropertyFactory.from(5l) // generates LongProperty
PropertyFactory.from(5d) // generates DoubleProperty
PropertyFactory.from("Hello") // generates Property<String>
PropertyFactory.from(Date.class, someDate) // generates Property<Date>
```

It will become apparent why this change was necessary with some examples:

A non-simple query by SQLite standards:

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
SELECT FROM `SomeTable` WHERE 0 < `latitude` AND (`longitude` > 50 OR `longitude` < 25) AND `name`='MyHome'
```

and turn it into:

```java
SQLite.select()
  .from(SomeTable.class)
  .where(PropertyFactory.from(0).lessThan(SomeTable_Table.latitude))
  .and(ConditionGroup.clause()
    .and(SomeTable_Table.longitude.greaterThan(50))
    .or(SomeTable_Table.longitude.lessThan(25)))
  .and(SomeTable_Table.name.eq("MyHome"))
```

## ModelContainers
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
JSONObject json = model.getData();
// has data now
```

For the `toModel()` conversion/parse method from `ModelContainer` to `Model`, you can now:
1. Have `@Column` excluded from it via `excludeFromToModelMethod()`
2. include other fields in the method as well by adding the `@ContainerKey` annotation to them.

## ModelViews
No longer do we need to specify the query for the `ModelView` in the annotation without ability to use the wrappper classes. We define a `@ModelViewQuery` field to use and then it simply becomes:

```java
@ModelViewQuery
    public static final Query QUERY = new Select(AModel_Table.time)
            .from(AModel.class).where(AModel_Table.time.greaterThan(0l));
```

What this means is that its easier than before to use Views.

## Caching
I significantly revamped model caching in this release to make it easier, support more tables, and more consistent. Some of the significant changes:

Previously you needed to extend `BaseCacheableModel` to enable model caching. No longer! The code that was there now generates in the corresponding `ModelAdapter` by setting `cachingEnabled = true` in the `@Table` annotation.

Before

```java
@Table(databaseName = TestDatabase.NAME)
public class CacheableModel extends BaseCacheableModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;

    @Override
    public int getCacheSize() {
        return 1000;
    }
}
```

After:

```java
@Table(database = TestDatabase.class, cachingEnabled = true, cacheSize = 1000)
public class CacheableModel extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;
}
```

Also, you can now have caching objects with _multiple_ primary keys!!!

Simply in your model class define a `@MultiCacheField` and now you can cache objects with multiple primary keys:

```java
@Table(database = TestDatabase.class, cachingEnabled = true)
public class MultipleCacheableModel extends BaseModel {

    @MultiCacheField
    public static IMultiKeyCacheConverter<String> multiKeyCacheModel = new IMultiKeyCacheConverter<String>() {

        @Override
        @NonNull
        public String getCachingKey(@NonNull Object[] values) {
            return "(" + values[0] + "," + values[1] + ")";
        }
    };

    @PrimaryKey
    double latitude;

    @PrimaryKey
    double longitude;

}
```

Please note that the field must be of type `IMultiKeyCacheConverter` in order to compile and convert correctly. You must provide one, otherwise caching will not work. Also the return caching key _must_ be unique, otherwise inconsistent results may occur from within the cache.

## Database Modules
Now in DBFlow we have support for libraries, other subprojects, and more in general to all use DBFlow at the same time. The only requirement is that they specify an argument to `apt` in order to prevent clashes and the library loads the class during the initialization phase. To read on how to do this (fairly simply), please check it out here: ([Database Modules](https://github.com/Raizlabs/DBFlow/blob/master/usage/DatabaseModules.md))
