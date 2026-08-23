# Query language

Infix builders on adapters. Call `.list()`, `.single()`, or `.execute()` in a transaction.

## Select

```kotlin
userAdapter.select()
userAdapter.select(User_Table.id, User_Table.name)
userAdapter.select().distinct()

userAdapter.select() where (User_Table.name eq "Ada")
userAdapter.select() where (User_Table.age greaterThan 18)
userAdapter.select() where (User_Table.name like "A%")
userAdapter.select() where (User_Table.id `in` listOf(1, 2, 3))
userAdapter.select() where (
    (User_Table.name eq "Ada") or (User_Table.name eq "Grace")
)

userAdapter.select() orderBy User_Table.name.asc()
userAdapter.select() limit 10 offset 20
userAdapter.select() groupBy User_Table.team
```

## Join

```kotlin
(postAdapter.select()
    innerJoin userAdapter
    on (Post_Table.author_id eq User_Table.id))
    .list()
```

Also `leftOuterJoin`, `crossJoin`, `naturalJoin`, `using`.

## Update / delete / insert

```kotlin
(userAdapter.update()
    set (User_Table.name eq "Grace")
    where (User_Table.name eq "Ada")).execute()

(userAdapter.delete() where (User_Table.name eq "Ada")).execute()

userAdapter.insert(User_Table.name.eq("Lin")).execute()
```

Conflict:

```kotlin
userAdapter.insert() or ConflictAction.REPLACE
```

## Operators

| Kotlin | SQL |
| --- | --- |
| `eq` / `notEq` | `=` / `!=` |
| `greaterThan` / `greaterThanOrEq` | `>` / `>=` |
| `lessThan` / `lessThanOrEq` | `<` / `<=` |
| `like` / `notLike` / `glob` / `match` | `LIKE` / `GLOB` / `MATCH` |
| `` `in` `` / `notIn` | `IN` |
| `between` | `BETWEEN` |
| `and` / `or` | `AND` / `OR` |
| `concatenate` | `\|\|` |
| `` `as` `` | `AS` |

Print SQL with `.query` on the builder (no execution).
