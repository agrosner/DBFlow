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

    public SQLCipherHelperImpl(BaseDatabaseDefinition databaseDefinition, DatabaseHelperListener listener) {
        super(databaseDefinition, listener);
    }

    @Override
    protected String getCipherSecret() {
        return "dbflow-rules";
    }
}
```

_Note:_ that the constructor with `BaseDatabaseDefinition` and `DatabaseHelperListener` is required.

Replace the `sqlHelperClass` in your `@Database`:

```java
@Database(name = CipherDatabase.NAME, version = CipherDatabase.VERSION,
        sqlHelperClass = SQLCipherHelperImpl.class)
public class CipherDatabase {

    public static final String NAME = "CipherDatabase";
    public static final int VERSION = 1;
}
```

And that's it. You're all set to start using SQLCipher!
