# Properties, Condition, & ConditionGroup
DBFlow by the means of java annotation processing, generates a `_Table` class that represents your `Model` class in its table structure. Each field generated are  `Property`. Each `Property` represents that field and provides type-safe operations that turn it into a `Condition` or another `Property`.

`Condition` is a class that represents a condition statement within a SQL statement such as:

```sql

name = 'Test'

name = `SomeTable`.`Test`

name LIKE '%Test%'

name != 'Test'

salary BETWEEN 15000 AND 40000

name IN('Test','Test2','TestN')

((`name`='Test' AND `rank`=6) OR (`name`='Bob' AND `rank`=8))
```

## How to use conditions
It is recommended that we create `Condition` from `Property` in our queries.

We have a simple table:

```java
@Table(database = TestDatabase.class, name = "TestModel32")
public class TestModel3 {

    @Column
    @PrimaryKey
    public String name;

    @Column
    String type;
}
```

With this definition a `TestModel3_Table` class gets generated:

```java

public final class TestModel3_Table {
  public static final Property<String> name = new Property<String>(TestModel3.class, "name");

  public static final Property<String> type = new Property<String>(TestModel3.class, "type");

  public static BaseProperty getProperty(String columnName) {
    columnName = QueryBuilder.quoteIfNeeded(columnName);
    switch (columnName)  {
      case "`name`":  {
        return name;
      }
      case "`type`":  {
        return type;
      }
      default:  {
        throw new IllegalArgumentException("Invalid column name passed. Ensure you are calling the correct table's column");
      }
    }
  }
}
```

Using fields from the generated class file, we can now use the `Property` to generate `Condition` for our queries:

```java

TestModel3_Table.name.is("Test"); // name = 'Test'
TestModel3_Table.name.withTable().is("Test"); // TestModel3.name = 'Test'
TestModel3_Table.name.like("%Test%")
```

A whole set of `Condition` operations are supported for `Property` including:
1. `is()`, `eq()` -> =
2. `isNot()`, `notEq()` -> !=
3. `isNull()` -> IS NULL / `isNotNull()`IS NOT NULL
4. `like()`, `glob()`
5. `greaterThan()`, `greaterThanOrEqual()`, `lessThan()`, `lessThanOrEqual()`
6. `between()` -> BETWEEN
7. `in()`, `notIn()`

## ConditionGroup
The `ConditionGroup` is the successor to the `ConditionQueryBuilder`. It was flawed in that it conformed to `QueryBuilder`, yet contained `Condition`, and required a type-parameter that referenced the table it belonged in.

`ConditionGroup` are arbitrary collections of `Condition` that can combine into one SQlite statement _or_ be used as `Condition` within another `ConditionGroup`.
