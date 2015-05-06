# Condition & ConditionQueryBuilder

```Condition``` is a class that represents a condition statement within a SQL statement such as:

```sql

name = 'Test'

name = `SomeTable`.`Test`

name LIKE '%Test%'

name != 'Test'

salary BETWEEN 15000 AND 40000

name IN('Test','Test2','TestN')

((`name`='Test' AND `rank`=6) OR (`name`='Bob' AND `rank`=8))

```

### How to use conditions

The ```Condition``` class can specify:
  1. **columnName**: the name of the column in the database. It is strongly recommended to use the
  column name from the generated ```$Table``` class for a ```Model``` as this will save you defining constant strings for the column names.
  2. **operator**: The valid SQL operator. Not "space separated" such that specifying ```operator("=")``` yields ```columnName=value```.
  3. **value**: The value of the data as represented by the ```Model```. This value will be converted into its database value when appending itself to a ```ConditionQueryBuilder```.
  4. **post argument**: The end of a condition, such as a ```Collate```
  5. **separator** (for within a ```ConditionQueryBuilder```): Specifies an optional SQLite condition separator that the ```ConditionQueryBuilder``` uses to separate its ```Condition```
  6. **between**: begins `Between` statement
  7. **in**: starts an `In` statement

We can nest conditions using the `CombinedCondition` class (which operates similar,
  but different from the `ConditionQueryBuilder`).

From our previous examples using this class:

```java

Condition.column(MyTable$Table.NAME).is("Test")

Condition.column(ColumnAlias.columnTable(SomeTable$Table.TABLE_NAME, SomeTable$Table.TEST))

Condition.column(MyTable$Table.NAME).like("%Test%")

Condition.column(MyTable$Table.NAME).isNot("Test")

Condition.column(MyTable$Table.SALARY).between(15000).and(40000)

Condition.column(MyTable$Table.NAME).in("Test").and("Test2").and("TestN")

// Nested conditions
CombinedCondition
  .begin(CombinedCondition
    .begin(Condition.column(MyTable$Table.NAME).is("Test"))
      .and(Condition.column(MyTable$Table.RANK).eq(8))
  .or(CominedCondition
    .begin(Condition.column(MyTable$Table.NAME).is("Bob"))
      .and(Condition.column(MyTable$Table.RANK).eq(6))))

```

### ConditionQueryBuilder

Very useful in constructing conditional pieces of a SQL statement, this class holds onto an ordered set of ```Condition``` and generates the proper SQL statement for them. It is mostly used in the ```WHERE``` and ```SET``` clauses of a statement as an easy way to chain conditions together. Also it handles converting any ```Model``` values that have a defined ```TypeConverter``` for it.

How to use:


```java

ConditionQueryBuilder<MyTable> queryBuilder = new ConditionQueryBuilder<MyTable>(MyTable.class,
    Condition.column(MyTable$NAME).is("test"))
    .or(Condition.column(MyTable$NAME).is("test2"))
    .and(Condition.column(MyTable$NUMBER).between(5).and(6));
String query = queryBuilder.getQuery();

// query = "name is 'test' OR name is 'test2' AND number BETWEEN 5 AND 6"

// In SELECT statments
// SELECT all based on the builder
List<MyTable> myList = new Select().from(MyTable.class).where(queryBuilder).queryList();

// IN UPDATE statements
new Update().table(MyTable.class).set(queryBuilder).where(anotherQueryBuilder).query();

```
