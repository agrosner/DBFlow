# SQLCipher Support

As of 3.0.0-beta2+, DBFlow now supports [SQLCipher](https://www.zetetic.net/sqlcipher/) fairly easily.

To add the library add the library to your `build.gradle` with same version you are using with the rest of the library.

```groovy
dependencies {
  compile "com.github.Raizlabs.DBFlow:dbflow-sqlcipher:${version}"
}
```

Next, you need to subclass the provided `SQLCipherOpenHelper` (taken from test files):

```java
public class SQLCipherHelperImpl extends SQLCipherOpenHelper {

    public SQLCipherHelperImpl(DatabaseDefinition databaseDefinition, DatabaseHelperListener listener) {
        super(databaseDefinition, listener);
    }

    @Override
    protected String getCipherSecret() {
        return "dbflow-rules";
    }
}
```

_Note:_ that the constructor with `DatabaseDefinition` and `DatabaseHelperListener` is required.

Then in your application class when initializing DBFlow:

```java

FlowManager.init(new FlowConfig.Builder(this)
  .addDatabaseConfig(
      new DatabaseConfig.Builder(CipherDatabase.class)
          .openHelper(new DatabaseConfig.OpenHelperCreator() {
              @Override
              public OpenHelper createHelper(DatabaseDefinition databaseDefinition, DatabaseHelperListener helperListener) {
                  return new SQLCipherHelperImpl(databaseDefinition, helperListener);
              }
          })
      .build())
  .build());

```

And that's it. You're all set to start using SQLCipher!
