# Views

```kotlin
@ModelView("SELECT `id` AS `authorId`, `first_name` || ' ' || `last_name` AS `authorName` FROM `Author`")
data class AuthorView(
    val authorId: Int,
    val authorName: String,
)
```

```kotlin
@Database(version = 1, tables = [Author::class], views = [AuthorView::class])
abstract class AppDatabase : DBFlowDatabase<AppDatabase>() {
    abstract val authorViewAdapter: ViewAdapter<AuthorView>
}
```

```kotlin
db.readableTransaction {
    authorViewAdapter.select().list()
}
```

Views are read-only. `priority` on `@ModelView` orders `CREATE VIEW` when one view depends on another. `createWithDatabase = false` skips creation at open.
