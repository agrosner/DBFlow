# Proguard

Since DBFlow uses annotation processing, which is run pre-proguard phase, the configuration is highly minimal. Also since we combine all generated files into the `GeneratedDatabaseHolder`, any other class generated can be obfuscated.

```
-keep class * extends com.raizlabs.android.dbflow.config.DatabaseHolder { *; }
```



