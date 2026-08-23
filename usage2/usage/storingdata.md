# Writes

Use the generated `ModelAdapter` inside `writableTransaction`. Operations are `suspend`.

```kotlin
db.writableTransaction {
    val saved = userAdapter.save(User(name = "Ada"))
    userAdapter.insert(User(name = "Grace"))
    userAdapter.update(saved.copy(name = "Ada Lovelace"))
    userAdapter.delete(saved)
}
```

| Method | SQL-ish behavior |
| --- | --- |
| `save` / `saveAll` | `INSERT OR REPLACE` |
| `insert` / `insertAll` | `INSERT` |
| `update` / `updateAll` | `UPDATE` by primary key |
| `delete` / `deleteAll` | `DELETE` by primary key |

Autoincrement ids are filled in on the returned model.

Batch:

```kotlin
db.writableTransaction {
    userAdapter.saveAll(listOf(User(name = "A"), User(name = "B")))
}
```

## Query writes

```kotlin
db.writableTransaction {
    (userAdapter.update()
        set (User.name eq "Grace")
        where (User.name eq "Ada")).execute()

    (userAdapter.delete() where (User.name eq "Grace")).execute()

    userAdapter.insert(
        User.name.eq("Lin"),
    ).execute()
}
```

## Scopes

```kotlin
db.writableTransaction { /* reads + writes */ }
db.readableTransaction { /* reads only */ }
```

Both run on the database transaction dispatcher. Prefer a transaction over many ad-hoc writes.

## Async callbacks

For Android callbacks instead of `suspend`:

```kotlin
db.beginTransactionAsync {
    userAdapter.save(User(name = "Ada"))
}.execute(
    success = { _, user -> },
    error = { _, throwable -> },
)
```

`enqueue` queues without blocking. `cancel()` is a no-op after the work has started.

## Exists

```kotlin
db.writableTransaction {
    userAdapter.exists(user)
}
```
