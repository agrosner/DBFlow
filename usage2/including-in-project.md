# Including in your Project

DBFlow has a number of artifacts that you can include in the project.

**Annotation Processor**: Generates the necessary code that you don't need to write.

**Core:** Contains the main annotations and misc classes that are shared across all of DBFlow.

**DBFlow: **The main library artifact used in conjunction with the previous two artifacts.

**SCLCipher Support:** Replaces most of inner database interaction with an [Encrypted Database](https://www.zetetic.net/sqlcipher/) that's useful for sensitive information. DBFlow contains wrapper around most of the implementation, so adding it is minimal.

**Kotlin: **DBFlow has Kotlin extensions support for the library, enabling more concise syntax and tons of inline helper methods! Also most of the public library API is annotated with `@Nullable` or `@NonNull`, providing nice interop when used in Kotlin.

**RXJava: **RX1 and RX2 supported. Wraps around normal DB operations by providing RXJava support for Model CRUD updates and SQLite wrapper language. Also Kotlin extensions exist for RX-specific methods.

### Add the jitpack.io repository

This repo is used to publish the artifacts. It also enables [dynamic builds](https://jitpack.io/docs/), allowing you to specify specific branches or commit hashes of the project to include outside of normal releases.

```Groovy
allProjects {
  repositories {
    // required to find the project's artifacts
    maven { url "https://www.jitpack.io" }
  }
}
```

```Groovy

  apply plugin: 'kotlin-kapt' // required for Kotlin

  def dbflow_version = "xxxx" // reference the releases tab on Github for latest versions
  // or you can grab a 10-digit commit hash of any commit in the project that builds.

  dependencies {
    annotationProcessor "com.github.Raizlabs.DBFlow:dbflow-processor:${dbflow_version}"

    // use kapt for kotlin apt if you're a Kotlin user
    kapt "com.github.Raizlabs.DBFlow:dbflow-processor:${dbflow_version}"

    compile "com.github.Raizlabs.DBFlow:dbflow-core:${dbflow_version}"
    compile "com.github.Raizlabs.DBFlow:dbflow:${dbflow_version}"

    // sql-cipher database encryption (optional)
    compile "com.github.Raizlabs.DBFlow:dbflow-sqlcipher:${dbflow_version}"
    compile "net.zetetic:android-database-sqlcipher:${sqlcipher_version}@aar"

    // kotlin extensions
    compile "com.github.Raizlabs.DBFlow:dbflow-kotlinextensions:${dbflow_version}"

    // RXJava 2 support
    compile "com.github.Raizlabs.DBFlow:dbflow-rx2:${dbflow_version}"

    // RXJava 2 Kotlin Extensions Support
    compile "com.github.Raizlabs.DBFlow:dbflow-rx2-kotlinextensions:${dbflow_version}"

  }

```
