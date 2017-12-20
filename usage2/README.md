# DBFlow

DBFlow is a Kotlin SQLite library for Android that makes it ridiculously easy to interact and use databases. Built with Annotation Processing, code use within a DB is fast, efficient, and type-safe. It removes the tedious \(and tough-to-maintain\) database interaction code, while providing a very SQLite-like query syntax.

Creating a database is as easy as a few lines of code:

```kotlin
@Database(version = AppDatabase.VERSION)
object AppDatabase {
    const val VERSION = 1
}
```

The `@Database` annotation generates a `DatabaseDefinition` which now references your SQLite Database on disk in the file named "AppDatabase.db". You can reference it in code as:

```java
val db: DatabaseDefinition = database<AppDatabase>();
```

To ensure generated code in DBFlow is found by the library, initialize the library in your `Application` class:

```kotlin
class MyApp : Application {

  override fun onCreate() {
    super.onCreate()
    FlowManager.init(this)
  }
}
```

By default, DBFlow generates the `GeneratedDatabaseHolder` class, which is instantiated once by reflection, only once in memory.

Creating a table is also very simple:

```kotlin
@Table(database = AppDatabase::class, name = "User2")
class User(@PrimaryKey var id: Int = 0,
           @Column var firstName: String? = null,
           @Column var lastName: String? = null,
           @Column var email: String? = null)
```

Then to create, read, update, and delete the model:

```kotlin
val user = User(id = UUID.randomUUID(),
                name = "Andrew Grosner",
                age = 27)

val db = databaseForTable<User>()
user.insert(db)

user.name = "Not Andrew Grosner";
user.update(db)

user.delete(db)

// Db optional on extension method
user.insert()
user.update()
user.delete()
user.save()

// find adult users
val users = database<AppDatabase>()
              .(select from User::class
                       where (User_Table.age greaterThan 18))
              .list

// or asynchronous retrieval
database<AppDatabase>().executeTransactionAsync(
{ (select from User::class where User_Table.age.greaterThan(18)).list },
success = { transaction, result ->
  // use result here
},
error = { transaction, error ->
  // handle any errors
})
```
