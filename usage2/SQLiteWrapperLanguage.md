# SQLite Wrapper Language

DBFlow's SQLite wrapper language attempts to make it as easy as possible to
write queries, execute statements, and more.

We will attempt to make this doc comprehensive, but reference the SQLite language
for how to formulate queries, as DBFlow follows it as much as possible.

## SELECT

The way to query data, `SELECT` are started by:

```java

SQLite.select().from(SomeTable.class)

```

### Projections

By default if no parameters are specified in the `select()` query, we use the `*` wildcard qualifier,
meaning all columns are returned in the results.

To specify individual columns, you _must_ use `Property` variables.
These get generated when you annotate your `Model` with columns, or created manually.

```java

SQLite.select(Player_Table.name, Player_Table.position)
    .from(Player.class)

```

To specify methods such as `COUNT()` or `SUM()` (static import on `Method`):


```java

SQLite.select(count(Employee_Table.name), sum(Employee_Table.salary))
    .from(Employee.class)

```

Translates to:

```sqlite

SELECT COUNT(`name`), SUM(`salary`) FROM `Employee`;

```

There are more handy methods in `Method`.

### Conditions

DBFlow supports many kinds of conditions. They are formulated into a `ConditionGroup`,
which represent a set of `SQLCondition` subclasses combined into a SQLite conditional piece.
`Property` translate themselves into `SQLCondition` via their conditional methods such as
`eq()`, `lessThan()`, `greaterThan()`, `between()`, `in()`, etc.

They make it very easy to construct concise and meaningful queries:

```java

int taxBracketCount = SQLite.select(count(Employee_Table.name))
    .from(Employee.class)
    .where(Employee_Table.salary.lessThan(150000))
    .and(Employee_Table.salary.greaterThan(80000))
    .count();

```

Translates to:

```sqlite

SELECT COUNT(`name`) FROM `Employee` WHERE `salary`<150000 AND `salary`>80000;

```

DBFlow supports `IN`/`NOT IN` and `BETWEEN` as well.

A more comprehensive list of conditional operations DBFlow supports and what they translate to:

  1. is(), eq() -> =
  2. isNot(), notEq() -> !=
  3. isNull() -> IS NULL / isNotNull() -> IS NOT NULL
  4. like(), glob()
  5. greaterThan(), greaterThanOrEqual(), lessThan(), lessThanOrEqual()
  6. between() -> BETWEEN 7. in(), notIn()
