# SQLCipher

Android only. Add the SQLCipher artifact and swap the open helper.

```toml
dbflow-sqlcipher = { module = "com.dbflow5:sqlcipher", version.ref = "dbflow" }
```

```kotlin
commonMain.dependencies {
    implementation(libs.dbflow.lib)
}
androidMain.dependencies {
    implementation(libs.dbflow.sqlcipher)
}
```

```kotlin
val db = createDB<CipherDatabase>(context) {
    copy(
        name = "Secure",
        openHelperCreator = SQLCipherOpenHelper.createHelperCreator(context, secret = "your-secret"),
    )
}
```

Use the generated database the same as a normal one.

R8:

```
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**
```
