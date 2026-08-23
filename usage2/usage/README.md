# Main usage

You work with three generated pieces:

1. **`{Db}_Database`** — `DBCreator` plus the concrete `DBFlowDatabase`
2. **Adapters** — `userAdapter.save()`, `userAdapter.select()`
3. **`{Table}_Table`** — `User_Table.name` in `where` / `set`

```kotlin
db.writableTransaction {
    userAdapter.save(User(name = "Ada"))
    (userAdapter.select() where (User_Table.name eq "Ada")).single()
    (userAdapter.update() set (User_Table.name eq "Grace") where (User_Table.name eq "Ada")).execute()
}
```

Equivalent SQL:

```sql
INSERT OR REPLACE INTO User(name) VALUES ('Ada');
SELECT * FROM User WHERE name = 'Ada';
UPDATE User SET name = 'Grace' WHERE name = 'Ada';
```

Also covered here: [migrations](migrations.md), [views](modelviews.md), [relationships](relationships.md), [type converters](typeconverters.md), [indexes](../advanced-usage/indexing.md).
