# Queries

Prefer `adapter.select()` on the generated database. Terminal calls are `suspend` and need a transaction scope.

```kotlin
db.readableTransaction {
    val all = userAdapter.select().list()
    val ada = (userAdapter.select() where (User.name eq "Ada")).single()
    val maybe = (userAdapter.select() where (User.id eq 1)).singleOrNull()
}
```

`single()` applies a one-row read and throws `SQLiteException` if missing. `list()` never throws on empty.

## Columns

```kotlin
userAdapter.select(User.id, User.name)
```

## Count / exists

```kotlin
db.readableTransaction {
    val count = userAdapter.selectCountOf().execute()
    val anyAda = (userAdapter.selectCountOf() where (User.name eq "Ada")).hasData()
}
```

## Map onto another type

Use a `@Query` model and pass its adapter:

```kotlin
db.readableTransaction {
    userAdapter.select().list(userNameQueryAdapter)
}
```

See [query models](../advanced-usage/querymodels.md).

## `select from`

```kotlin
(select from userAdapter where (User.name eq "Ada")).list()
(select from User where (User.name eq "Ada")).list()

// after createDB, KClass lookup also works
(select from User::class where (User.name eq "Ada")).list()
```

`createDB` registers adapters so KClass lookups work after open.

## Raw cursor

```kotlin
db.readableTransaction {
    userAdapter.select().cursor().use { cursor ->
        // close it
    }
}
```

`@DelicateDBFlowApi`.

## Observe

```kotlin
userAdapter.select().toFlow(db) { list() }
```

See [observability](observability.md).
