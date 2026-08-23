# R8 / ProGuard

Generated Kotlin is compiled with your app. Keep the holder factory and SQLCipher if you use it.

```
-keep class * implements com.dbflow5.database.DatabaseHolderFactory { *; }
-keep class * extends com.dbflow5.database.DBFlowDatabase { *; }
```

SQLCipher:

```
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**
```
