# Android extras

These artifacts are Android-only. Shared code should use `toFlow` from `lib`.

## LiveData

```kotlin
androidMain.dependencies {
    implementation(libs.dbflow.livedata)
}
```

```kotlin
val users: LiveData<List<User>> =
    userAdapter.select().toLiveData(db) { list() }

users.observe(owner) { list -> }
```

Re-queries when `TableObserver` reports a change.

## Paging

```kotlin
androidMain.dependencies {
    implementation(libs.dbflow.paging)
}
```

```kotlin
val factory = userAdapter.select()
    .toDataSourceFactory(db)
```

Use with `LivePagedListBuilder` / Paging 2 `PositionalDataSource`. Invalidates on table changes.

## RxJava 3

```kotlin
androidMain.dependencies {
    implementation(libs.dbflow.reactive-streams)
}
```

```kotlin
db.beginTransactionAsync { userAdapter.select().list() }
    .asSingle()
```

Table-change streams use `TableChangeOnSubscribe` with the same observer as `toFlow`.
