# ModelViews

A `ModelView` is a SQLite representation of a `VIEW`. Read official SQLite docs
[here](https://www.sqlite.org/lang_createview.html) for more information.

As with SQLite a `ModelView` cannot insert, update, or delete itself as it's
read-only. It is a virtual "view" placed on top of a regular table as a prepackaged
`Select` statement. In DBFlow using a `ModelView` should feel familiar and be very simple.

```java

@ModelView(database = TestDatabase.class)
public class TestModelView {

    @ModelViewQuery
    public static final Query QUERY = SQLite.select().from(TestModel2.class)
            .where(TestModel2_Table.model_order.greaterThan(5));

    @Column
    long model_order;
}

```

```kotlin
@ModelView(database = TestDatabase::class)
class TestModelView(@Column modelOrder: Long = 0L) {

  companion object {
    @ModelViewQuery @JvmField
    val query = (select from TestModel2::class where TestModel2_Table.model_order.greaterThan(5))
  }
}

```

To specify the query that a `ModelView` creates itself with, we _must_ define
a public static final field annotated with `@ModelViewQuery`. This tells DBFlow
what field is the query. This query is used only once when the database is created
(or updated) to create the view.

The full list of limitations/supported types are:
  1. Only `@Column`/`@ColumnMap` are allowed
  2. No `@PrimaryKey` or `@ForeignKey`
  3. Supports all fields, and accessibility modifiers that `Model` support
  4. Does not support `@InheritedField`, `@InheritedPrimaryKey`
  5. Basic, type-converted, non-model `@Column`.
  6. __Cannot__: update, insert, or delete

`ModelView` are used identical to `Model` when retrieving from the database:

```java

SQLite.select()
  .from(TestModelView.class)
  .where(...) // ETC

```

```kotlin
(select from TestModelView::class where (...))
```
