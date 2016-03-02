# Query Models

Query Models or `@QueryModel` are simple objects that are used to map a specific,
non-standard query that returns some set of columns that don't belong to a `@Table`. Similar to `@ModelView` definitions, these cannot contain `@PrimaryKey`, but _must_ also extend `BaseQueryModel`.

To create one:

```java

@QueryModel(database = TestDatabase.class)
public class TestQueryModel extends BaseQueryModel {

    @Column
    String newName;

    @Column
    long average_salary;

    @Column
    String department;
}

```

Same rules apply to Tables and Views, the fields must be package private, public, or private with getters and setters. If you wish to not verbosely define all fields with an annotation, set `@QueryModel(allFields=true)`.
