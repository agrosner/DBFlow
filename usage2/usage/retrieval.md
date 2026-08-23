# Queries

Prefer `adapter.select()` on the generated database. Terminal calls are `suspend` and need a transaction scope.

```kotlin
db.readableTransaction {
    val all = userAdapter.select().list()
    val ada = (userAdapter.select() where (User_Table.name eq "Ada")).single()
    val maybe = (userAdapter.select() where (User_Table.id eq 1)).singleOrNull()
}
```

`single()` applies a one-row read and throws `SQLiteException` if missing. `list()` never throws on empty.

## Columns

```kotlin
userAdapter.select(User_Table.id, User_Table.name)
```

## Count / exists

```kotlin
db.readableTransaction {
    val count = userAdapter.selectCountOf().execute()
    val anyAda = (userAdapter.selectCountOf() where (User_Table.name eq "Ada")).hasData()
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
(select from userAdapter where (User_Table.name eq "Ada")).list()

// needs DatabaseObjectLookup.loadHolder(...)
(select from User::class where (User_Table.name eq "Ada")).list()
```

Adapters on the database instance do not need the holder.

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
