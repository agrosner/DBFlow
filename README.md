# DBFlow

DBFlow is a SQLite library for Android that makes it ridiculously easy to interact and use databases. Built with Annotation Processing that generates most of the boilerplate code for you, code use within a DB is fast, efficient, and type-safe. It removes the tedious \(and tough-to-maintain\) database interaction code.

Creating a database is as easy as a few lines of code:

```java
@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

  public static final String NAME = "AppDatabase";

  public static final int VERSION = 1;
}
```

The `@Database` annotation generates a `DatabaseDefinition` which now references your SQLite Database on disk in the file named "AppDatabase.db". You can reference it in code as:

```java
DatabaseDefinition db = FlowManager.getDatabase(AppDatabase.class);
```

To ensure generated code in DBFlow is found by the library, initialize the library in your `Application` class:

```java
public class MyApp extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    FlowManager.init(this);
  }
}
```

By default, DBFlow generates the `GeneratedDatabaseHolder` class, which is instantiated once by reflection, only once in memory.

Creating a table is also very simple:

```java
@Table(database = AppDatabase.class)
public class User {

  @PrimaryKey // at least one primary key required
  UUID id; 

  @Column
  String name;

  @Column
  int age;
}
```

Then to create, read, update, and delete the model:

```java
User user = new User();
user.id = UUID.randomUUID();
user.name = "Andrew Grosner";
user.age = 27;

ModelAdapter<User> adapter = FlowManager.getModelAdapter(User.class);
adapter.insert(user);

user.name = "Not Andrew Grosner";
adapter.update(user);

adapter.delete(user); 

// if you extend BaseModel or implement Model
user.insert();
user.update();
user.delete();
user.save();

// find adult users
List<User> users = SQLite.select()
                    .from(User.class)
                    .where(User_Table.age.greaterThan(18))
                    .queryList();

// or asynchronous retrieval
SQLite.select()
  .from(User.class)
  .where(User_Table.age.greaterThan(18))
  .async()
  .queryListCallback((QueryTransaction transaction, @NonNull CursorResult<User> result) -> {
              // called when query returns on UI thread
              try {
                List<User> users = result.toList();
                // do something with users
              } finally {
                result.close();
              }
            })
  .error((Transaction transaction, Throwable error) -> {
              // handle any errors
         })
  .execute();
```



