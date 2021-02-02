# SQLCipher

As of 3.0.0-beta2+, DBFlow now supports [SQLCipher](https://www.zetetic.net/sqlcipher/) fairly easily.

To add the library add the library to your `build.gradle` with same version you are using with the rest of the library.

```groovy
dependencies {
  implementation "com.dbflow5:sqlcipher:${version}"
  implementation "net.zetetic:android-database-sqlcipher:${sqlcipher_version}"
}
```

You also need to add the Proguard rule:

```text
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**
```

Next, you need to subclass the provided `SQLCipherOpenHelper` \(taken from test files\):

```kotlin
class SQLCipherOpenHelperImpl(context: Context,
                              databaseDefinition: DBFlowDatabase,
                              callback: DatabaseCallback?)
    : SQLCipherOpenHelper(context, databaseDefinition, callback) {
    override val cipherSecret get() = "dbflow-rules"
}
```

_Note:_ that the constructor with `DatabaseDefinition` and `DatabaseHelperListener` is required.

Then in your application class when initializing DBFlow:

```kotlin
FlowManager.init(FlowConfig.Builder(context)
  .database(
      DatabaseConfig.Builder(CipherDatabase::class) { db, callback -> SQLCipherHelperImpl(context, databaseDefinition, callback))
      .build())
  .build())
```

And that's it. You're all set to start using SQLCipher!

