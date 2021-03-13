# QueryModels

A `QueryModel` maps any kind of custom query to a type object. These types are virtual and do not get represented by any real DB construct such as a `Table`

We use a different annotation, `@QueryModel`, to define it separately. These do not allow for modifications in the DB, rather act as a marshal agent out of the DB.

## Define a QueryModel

For this example, we have a list of employees that we want to gather the average salary for each position in each department from our company.

We defined an `Employee` table:

```kotlin
@Table(database = AppDatabase::class)
class EmployeeModel(@PrimaryKey var uid: String = "",
    var salary: Long = 0L,
    var name: String = "",
    var title: String = "",
    var department: String = "")
```

We need someway to retrieve the results of this query, since we want to avoid dealing with the `Cursor` directly. We can use a SQLite query with our existing models, but we have no way to map it currently to our tables, since the query returns new Columns that do not represent any existing table:

```kotlin
(select(EmployeeModel_Table.department,
                avg(EmployeeModel_Table.salary.as("average_salary")),
                EmployeeModel_Table.title)
  from EmployeeModel::class)
  .groupBy(EmployeeModel_Table.department, EmployeeModel_Table.title)
```

So we must define a `QueryModel`, representing the results of the query:

```kotlin
@QueryModel(database = AppDatabase::class)
class AverageSalary(var title: String = "",
    var average_salary: Long = 0L,
    var department: String = "")
```

And adjust our query to handle the new output:

```kotlin
(select(EmployeeModel_Table.department,
                avg(EmployeeModel_Table.salary.as("average_salary")),
                EmployeeModel_Table.title)
  from EmployeeModel::class)
  .groupBy(EmployeeModel_Table.department, EmployeeModel_Table.title)
  .async(database) { it.customList<AverageSalary>() }
  .execute { transaction, list ->
      // utilize list
  }
```

## Query Model Support

`QueryModel` are read-only. We can only retrieve from DB into a cursor.

They support inheritance and visibility modifiers as defined by [Models](../usage/models.md).

`QueryModel` **do not** support: 

1. `InheritedField`/`InheritedPrimaryKey` 

2. `@PrimaryKey`/`@ForeignKey` 

3. direct caching. Can cache queries using the `withModelCache`  on a SQLite query, which is a read-only cache.

4. changing `useBooleanGetterSetters` for private boolean fields.

