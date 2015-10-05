# DBFlow 3.0 Migration Guide
DBFlow has undergone the most _significant_ changes in its lifetime in 3.0. This guide is meant to assist you in migrating from 2.1.x and above and may not be fully inclusive of all changes. This doc will mention the most glaring and significant changes.

The changes that are not visible to the app developer using this library is the _complete_ overhaul of the underlying annotation processor, leading to wonderful improvements in maintainability of that code, readability, and stability of the generated code. Now it uses the updated [JavaPoet](https://github.com/square/javapoet) vs the outdated JavaWriter. The changes in this library alone _significantly_ helps out the stability of the generated code.

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

will restore your generated "Table" and more classes to normalcy.

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
@Table have some significant changes. Added was an `IndexGroup[]` of `indexGroups()`.

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
`@ForeignKey` fields no longer need to specify it's references!!! The old way still works, but is no longer necessary for `Model`-based FKs. The annotation processor takes the primary keys of the referenced table and generates a column with {fieldName}_{referencedColumnName} that represents the same SQLite Type of the field. Note that is not backwards compatible with apps already with references, however going forward its not necessary anymore.

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
                                          foreignKeyColumnName = "name",
                                          columnType = String.class),
                            @ForeignKeyReference(columnName = "testmodel_type",
                                                 foreignKeyColumnName = "type",
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

## Properties, Conditions, Queries, Replacement of ConditionQueryBuilder and more
Perhaps the most significant internal changes to this library is making queries, conditions, and interactions with the database much stricter and more type-safe.

### Properties
Properties replace `String` column names generated in the "$Table" classes. They are also matching exact case to the column name.

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
  new Select(name.as("employee_name"),
    avg(salary).as("average_salary"),
    order,
    sum(salary).as("sum_salary"))
  .from(SomeTable.class)
  .where(salary.greaterThan(150000))
  .queryCustomList(SomeQueryTable.class);
```

The code instantly becomes cleaner, and reads more like an actual query.
