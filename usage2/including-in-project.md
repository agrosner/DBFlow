# Including In Project

DBFlow has a number of artifacts that you can include in the project.

**Annotation Processor**: Generates the necessary code that you don't need to write.

**Core:** Contains the main annotations and misc classes that are shared across all of DBFlow.

**DBFlow:** The main library artifact used in conjunction with the previous two artifacts.

**Coroutines:** Adds coroutine support for queries.

**RX Java:** Enable applications to be reactive by listening to DB changes and ensuring your subscribers are up-to-date.

**Paging:** Android architecture component paging library support for queries via `QueryDataSource`.

**SQLCipher:** Easy database encryption support in this library.

## Add the jitpack.io repository

This repo is used to publish the artifacts. It also enables [dynamic builds](https://jitpack.io/docs/), allowing you to specify specific branches or commit hashes of the project to include outside of normal releases.

```groovy
allProjects {
  repositories {
    google() 
    // required to find the project's artifacts
    // place last
    maven { url "https://www.jitpack.io" }
  }
}
```

Add artifacts to your project:

```groovy
  apply plugin: 'kotlin-kapt' // only required for kotlin consumers.

  def dbflow_version = "5.0.0-alpha1"
  // or 10-digit short-hash of a specific commit. (Useful for bugs fixed in develop, but not in a release yet)

  dependencies {

    // Use if Kotlin user.
    kapt "com.github.agrosner.dbflow:processor:${dbflow_version}"

    // Annotation Processor
    // if only using Java, use this. If using Kotlin do NOT use this.
    annotationProcessor "com.github.agrosner.dbflow:processor:${dbflow_version}"


    // core set of libraries
    compile "com.github.agrosner.dbflow:core:${dbflow_version}"
    compile "com.github.agrosner.dbflow:lib:${dbflow_version}"

    // sql-cipher database encryption (optional)
    compile "com.github.agrosner.dbflow:sqlcipher:${dbflow_version}"
    compile "net.zetetic:android-database-sqlcipher:${sqlcipher_version}@aar"

    // RXJava 2 support
    compile "com.github.agrosner.dbflow:reactive-streams:${dbflow_version}"

    // Kotlin Coroutines
    compile "com.github.agrosner.dbflow:coroutines:${dbflow_version}"

    // Android Architecture Components Paging Library Support
    compile "com.github.agrosner.dbflow:paging:${dbflow_version}"

    // adds generated content provider annotations + support.
    compile "com.github.agrosner.dbflow:contentprovider:${dbflow_version}"

  }
```

