# Indexing

In SQLite, an `Index` is a pointer to specific columns in a table that enable super-fast retrieval.

**Note**: The database size can increase significantly, however if performance is more important, the tradeoff is worth it.

Indexes are defined using the `indexGroups()` property of the `@Table` annotation. These operate similar to how `UniqueGroup` work: 1. specify an `@IndexGroup` 2. Add the `@Index` 3. Build and an `IndexProperty` gets generated. This allows super-easy access to the index so you can enable/disable it with ease.

**Note**: `Index` are not explicitly enabled unless coupled with an `IndexMigration`. \([read here](../usage/migrations.md#index-migrations)\).

You can define as many `@IndexGroup` you want within a `@Table` as long as one field references the group. Also individual `@Column` can belong to any number of groups:

```kotlin
@Table(database = TestDatabase::class,
       indexGroups = [
               IndexGroup(number = 1, name = "firstIndex"),
               IndexGroup(number = 2, name = "secondIndex"),
               IndexGroup(number = 3, name = "thirdIndex")
       ])
class IndexModel2 {

   @Index(indexGroups = {1, 2, 3})
   @PrimaryKey
   var id: Int = 0

   @Index(indexGroups = 1)
   @Column
   var firstName: String = ""

   @Index(indexGroups = 2)
   @Column
   var lastName: String = ""

   @Index(indexGroups = {1, 3})
   @Column
   var createdDate: Date? = null

   @Index(indexGroups = {2, 3})
   @Column
   var isPro: Boolean = false
}
```

By defining the index this way, we generate an `IndexProperty`, which makes it very easy to enable, disable, and use it within queries:

```kotlin
IndexModel2_Table.firstIndex.createIfNotExists(database);

(select from IndexModel2::class
  indexedBy IndexModel2_Table.firstIndex
  where ...)

IndexModel2_Table.firstIndex.drop(database); // turn it off when no longer needed.
```

## SQLite Index Wrapper

For flexibility, we also support the SQLite `Index` wrapper object, in which the `IndexProperty` uses underneath.

```kotlin
val index = indexOn<SomeTable>("MyIndex", SomeTable_Table.name, SomeTable_Table.othercolumn)
index.createIfNotExists(database)

index.drop(database)
```

