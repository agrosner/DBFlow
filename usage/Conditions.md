# Properties, Condition, & ConditionGroup
DBFlow, by the means of java annotation processing, generates a `_Table` class that represents your `Model` class in its table structure. Each field generated are subclasses of `IProperty`. Each `IProperty` represents a column in the corresponding table and provides type-safe conditional operations that turn it into a `SQLCondition` or mutate into another `Property`.

`SQLCondition` is an interface that represents a condition statement within a SQL statement. It's an interface so other types of condition can be used, as well as allowing maximum flexibility to suit your needs.

For example, written in raw SQLite:

```sql

`name` = 'Test'

`name` = `SomeTable`.`Test`

`name` LIKE '%Test%'

`name` != 'Test'

`salary` BETWEEN 15000 AND 40000

`name` IN('Test','Test2','TestN')

((`name`='Test' AND `rank`=6) OR (`name`='Bob' AND `rank`=8))
```

## How to use conditions
It is recommended that we create `Condition` from `Property` in our queries.

We have a simple table:

```java
@Table(database = TestDatabase.class)
public class TestModel3 {

    @PrimaryKey
    String name;

    @Column
    String type;
}
```

With this definition a `TestModel3_Table` class gets generated:

```java

public final class TestModel3_Table {
  public static final PropertyConverter PROPERTY_CONVERTER = new PropertyConverter(){
  public IProperty fromName(String columnName) {
  return com.raizlabs.android.dbflow.test.sql.TestModel3_Table.getProperty(columnName);
  }
  };

  public static final Property<String> type = new Property<String>(TestModel3.class, "type");

  public static final Property<String> name = new Property<String>(TestModel3.class, "name");

  public static final IProperty[] getAllColumnProperties() {
    return new IProperty[]{type,name};
  }

  public static BaseProperty getProperty(String columnName) {
    columnName = QueryBuilder.quoteIfNeeded(columnName);
    switch (columnName)  {
      case "`type`":  {
        return type;
      }
      case "`name`":  {
        return name;
      }
      default:  {
        throw new IllegalArgumentException("Invalid column name passed. Ensure you are calling the correct table's column");
      }
    }
  }
}
```

Using fields from the generated class file, we can now use the `Property` to generate `SQLCondition` for our queries:

```java

TestModel3_Table.name.is("Test"); // `name` = 'Test'
TestModel3_Table.name.withTable().is("Test"); // `TestModel3`.`name` = 'Test'
TestModel3_Table.name.like("%Test%");

// `name`=`AnotherTable`.`name`
TestModel3_Table.name.eq(AnotherTable_Table.name);
```

A whole set of `SQLCondition` operations are supported for `Property` generated for a Table including:
1. `is()`, `eq()` -> =
2. `isNot()`, `notEq()` -> !=
3. `isNull()` -> IS NULL / `isNotNull()`IS NOT NULL
4. `like()`, `glob()`
5. `greaterThan()`, `greaterThanOrEqual()`, `lessThan()`, `lessThanOrEqual()`
6. `between()` -> BETWEEN
7. `in()`, `notIn()`

Also we can do adds and subtractions:

```java

SomeTable_Table.latitude.plus(SomeTable_Table.longitude).lessThan(45.0); // `latitude` + `longitude` < 45.0

SomeTable_Table.latitude.minus(SomeTable_Table.longitude).greaterThan(45.0); // `latitude` - `longitude` > 45.0

```

## ConditionGroup
The `ConditionGroup` is the successor to the `ConditionQueryBuilder`. That was flawed in that it conformed to `QueryBuilder`, yet contained `Condition`, and required a type-parameter that referenced the table it belonged in.

`ConditionGroup` are arbitrary collections of `SQLCondition` that can combine into a single conditional, SELECT projection, _or_ be used as `SQLCondition` within another `ConditionGroup`.

This used in wrapper query statements, backing for all kinds of other queries and classes.

```java

SQLite.select()
  .from(MyTable.class)
  .where(MyTable_Table.someColumn.is("SomeValue"))
  .and(MyTable_Table.anotherColumn.is("ThisValue"));

  // SELECT * FROM `MyTable` WHERE `someColumn`='OneValue' OR (`someColumn`='SomeValue' AND `anotherColumn`='ThisValue')
  SQLite.select()
    .from(MyTable.class)
    .where(MyTable.someColumn.is("OneValue"))
    .or(ConditionGroup.clause()
      .and(MyTable_Table.someColumn.is("SomeValue")
      .AND(MyTable_Table.anotherColumn.is("ThisValue"));


```
