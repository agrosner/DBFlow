# Query Models

A `QueryModel` is purely an ORM object that maps rows from a `Cursor` into
a `Model` such that when loading from the DB, we can easily use the data from it.

We use a different annotation, `@QueryModel`, to define it separately. These
do not allow for modifications in the DB, rather act as a marshal agent out of the DB.

## Define a QueryModel

For this example, we have a list of employees that we want to gather the average salary
for each position in each department from our company.

We defined an `Employee` table:

```java

@Table(database = AppDatabase.class)
public class EmployeeModel {

    @Column
    @PrimaryKey
    String uid;

    @Column
    long salary;

    @Column
    String name;

    @Column
    String title;

    @Column
    String department;
}

```

We need someway to retrieve the results of this query, since we want to avoid
dealing with the `Cursor` directly. We can use a SQLite query with our existing models, but
we have no way to map it currently to our tables, since the query returns new Columns
that do not represent any existing table:

```java

SQLite.select(EmployeeModel_Table.department,
                Method.avg(EmployeeModel_Table.salary.as("average_salary")),
                EmployeeModel_Table.title)
      .from(EmployeeModel.class)
      .groupBy(EmployeeModel_Table.department, EmployeeModel_Table.title);

```

So we must define a `QueryModel`, representing the results of the query:

```java
@QueryModel(database = AppDatabase.class)
public class AverageSalary {

    @Column
    String title;

    @Column
    long average_salary;

    @Column
    String department;
}
```

And adjust our query to handle the new output:

```java

SQLite.select(EmployeeModel_Table.department,
                Method.avg(EmployeeModel_Table.salary.as("average_salary")),
                EmployeeModel_Table.title)
      .from(EmployeeModel.class)
      .groupBy(EmployeeModel_Table.department, EmployeeModel_Table.title)
      .async()
      .queryResultCallback(new QueryTransaction.QueryResultCallback<EmployeeModel>() {
          @Override
          public void onQueryResult(QueryTransaction transaction, @NonNull CursorResult<EmployeeModel> tResult) {
              List<AverageSalary> queryModels = tResult.toCustomListClose(AverageSalary.class);

            // do something with the result
          }
      }).execute();

```

## Query Model Support

`QueryModel` support only a limited subset of `Model` features.

If you use the optional base class of `BaseQueryModel`,
 Modifications such as `insert()`, `update()`, `save()`, and `delete()` will throw
 an `InvalidSqlViewOperationException`. Otherwise, `RetrievalAdapter` do not
 contain modification methods.

They support `allFields` and inheritance and visibility modifiers as defined by [Models](/usage2/Models.md).

`QueryModel` **do not** support:
  1. `InheritedField`/`InheritedPrimaryKey`
  2. `@PrimaryKey`/`@ForeignKey`
  3. caching
  4. changing "useBooleanGetterSetters" for private boolean fields.
