# Query language

Infix builders on adapters. Call `.list()`, `.single()`, or `.execute()` in a transaction.

## Select

```kotlin
userAdapter.select()
userAdapter.select(User.id, User.name)
userAdapter.select().distinct()

userAdapter.select() where (User.name eq "Ada")
userAdapter.select() where (User.age greaterThan 18)
userAdapter.select() where (User.name like "A%")
userAdapter.select() where (User.id `in` listOf(1, 2, 3))
userAdapter.select() where (
    (User.name eq "Ada") or (User.name eq "Grace")
)

userAdapter.select() orderBy User.name.asc()
userAdapter.select() limit 10 offset 20
userAdapter.select() groupBy User.team
```

## Join

```kotlin
(postAdapter.select()
    innerJoin userAdapter
    on (Post.author_id eq User.id))
    .list()
```

Also `leftOuterJoin`, `crossJoin`, `naturalJoin`, `using`.

## Update / delete / insert

```kotlin
(userAdapter.update()
    set (User.name eq "Grace")
    where (User.name eq "Ada")).execute()

(userAdapter.delete() where (User.name eq "Ada")).execute()

userAdapter.insert(User.name.eq("Lin")).execute()
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
