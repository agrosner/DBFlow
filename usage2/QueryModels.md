# Query Models

A `QueryModel` is purely an ORM object that maps rows from a `Cursor` into
a `Model` such that when loading from the DB, we can easily use the data from it.

We use a different annotation, `@QueryModel`, to define it separately.

We _must_ extend `BaseQueryModel` in this case, in order to enforce that it
does not allow modifications. Since `BaseQueryModel` implements `Model`, we
enforce the restriction of _no modifications_. This is because the result set
does not always correspond to a table.

## Define a QueryModel

For this example, we have a list of employees that we want to gather the average salary
for each position in each department from our company.

We defined an `Employee` table:

```java

@Table(database = AppDatabase.class)
public class EmployeeModel extends BaseModel {

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
dealing with the `Cursor` directly:

```java

SQLite.select(EmployeeModel_Table.department,
                Method.avg(EmployeeModel_Table.salary.as("average_salary")),
                EmployeeModel_Table.title)
      .from(EmployeeModel.class)
      .groupBy(EmployeeModel_Table.department, EmployeeModel_Table.title);

```

Now we define a `QueryModel`:

```java
@QueryModel(database = AppDatabase.class)
public class AverageSalary extends BaseQueryModel {

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

Modifications such as `insert()`, `update()`, `save()`, and `delete()` will throw
an `InvalidSqlViewOperationException` if called.

They support `allFields` and inheritance and visibility modifiers as defined by [Models](/usage2/Models.md).

`QueryModel` **do not** support:
  1. `InheritedField`/`InheritedPrimaryKey`
  2. `@PrimaryKey`/`@ForeignKey`
  3. caching
  4. use "is" for private boolean fields.
