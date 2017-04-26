# Usage

DBFlow supports a number of database features that will enhance and decrease time you need to spend coding with databases. We support multiple databases at the same time \(and in separate modules\) as long as there's no shared models.

What is covered in these docs are not all inclusive, but should give you an idea of how to operate with DBFlow on databases.

There are a few concepts to familiarize yourself with. We will go more in depth in other sections in this doc.

**SQLite Wrapper Language:** DBFlow provides a number of convenience methods, extensions, and generated helpers that produce a concise, flowable query syntax. A few examples below:

```
List<User> users = SQLite.select()
  .from(User.class)
  .where(name.is("Andrew Grosner"))
  .queryList();

SQLite.update(User.class)
  .set(name.eq("Andrew Grosner"))
  .where(name.eq("Andy Grosner"))
  .executeUpdateDelete()

FlowManager.getDatabase(AppDatabase.class).beginTransactionAsync((DatabaseWrapper wrapper) -> {
  // wraps in a SQLite transaction, do something on BG thread.
});

CursorResult<User> results = SQLite.select().from(User.class).queryResults();
try {
  for (User user: results) { // memory efficient iterator

  }
} finally {
  results.close()
}
```

Or in Kotlin:

    val users = (select from User::class where (name `is` "Andrew Grosner")).list

    (update<User>() set (name eq "Andrew Grosner") where (name eq "Andy Grosner")).executeUpdateDelete()

    database<AppDatabase>().beginTransactionAsync {

    }

    (select from User::class).queryResults().use { results ->
      for (user in results) { 

      }
    }

**Caching: **DBFlow supports caching in models. Caching them greatly increases speed, but cache carefully as it can lead to problems such as stale data.

```
@Table(cachingEnabled = true)
public class User
```

**Migrations: **Migrations are made very simple in DBFlow. We only support the kinds that [SQLite provide](https://sqlite.org/lang_altertable.html), but also allow you to modify the data within the DB in a structured way during these. They are also run whenever the `SQLiteOpenHelper` detects a version change in the order of version they specify.





