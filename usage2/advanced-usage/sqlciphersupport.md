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

Then in your application class when initializing DBFlow:

```kotlin
FlowManager.init(context) {
  database<CipherDatabase>(
    openHelperCreator = SQLCipherOpenHelper.createHelperCreator(DemoApp.context, secret = "dbflow-rules"))
}
```

And that's it. You're all set to start using SQLCipher!

