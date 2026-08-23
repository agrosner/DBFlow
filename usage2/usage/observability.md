# Observability

`TableObserver` records table writes (via triggers) when a transaction finishes. `toFlow` re-runs a query when those tables change.

```kotlin
val users: Flow<List<User>> =
    userAdapter.select().toFlow(db) { list() }
```

```kotlin
(userAdapter.select() where (User_Table.name like "A%"))
    .toFlow(db, runQueryOnCollect = false) { singleOrNull() }
```

`runQueryOnCollect = true` (default) emits immediately on collect. Set `false` if the first `single()` would throw on an empty table.

Writes must go through a DBFlow transaction so the observer runs. For a one-shot coroutine transaction as a `Flow`:

```kotlin
db.beginTransactionAsync { userAdapter.select().list() }.toFlow()
```

Android: [LiveData](../android.md#livedata), [Paging](../android.md#paging), [RxJava](../android.md#rxjava-3).
