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



