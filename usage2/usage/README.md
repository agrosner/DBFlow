# Main usage

You work with three generated pieces:

1. **`{Db}_Database`** — `DBCreator` plus the concrete `DBFlowDatabase`
2. **Adapters** — `userAdapter.save()`, `userAdapter.select()`
3. **Model companions** — `User.name` in `where` / `set`, `select from User`, and
   the runtime adapter (`userAdapter` is `User.Companion as ModelAdapter<User>`)

```kotlin
db.writableTransaction {
    userAdapter.save(User(name = "Ada"))
    (userAdapter.select() where (User.name eq "Ada")).single()
    (userAdapter.update() set (User.name eq "Grace") where (User.name eq "Ada")).execute()
}
```

Equivalent SQL:

```sql
INSERT OR REPLACE INTO User(name) VALUES ('Ada');
SELECT * FROM User WHERE name = 'Ada';
UPDATE User SET name = 'Grace' WHERE name = 'Ada';
```

Also covered here: [migrations](migrations.md), [views](modelviews.md), [relationships](relationships.md), [type converters](typeconverters.md), [indexes](../advanced-usage/indexing.md).
