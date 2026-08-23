# Indexes

```kotlin
@Table(
    indexGroups = [
        IndexGroup(number = 1, name = "name_index"),
        IndexGroup(number = 2, name = "created_index"),
    ]
)
data class Person(
    @Index(indexGroups = [1, 2]) @PrimaryKey val id: Int,
    @Index(indexGroups = [1]) val firstName: String?,
    @Index(indexGroups = [2]) val createdAt: Long?,
)
```

Generated index properties live on `Person_Table`. Use them with `indexedBy` when SQLite should pick that index:

```kotlin
personAdapter.select() indexedBy Person_Table.name_index where (Person_Table.firstName eq "Ada")
```
