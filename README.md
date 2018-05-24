![Image](https://github.com/agrosner/DBFlow/blob/develop/dbflow_banner.png?raw=true)

[![JitPack.io](https://img.shields.io/badge/JitPack.io-5.0.0alpha1-red.svg?style=flat)](https://jitpack.io/#Raizlabs/DBFlow) [![Android Weekly](http://img.shields.io/badge/Android%20Weekly-%23129-2CB3E5.svg?style=flat)](http://androidweekly.net/issues/issue-129) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-DBFlow-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1134)

DBFlow is fast, efficient, and feature-rich Kotlin database library built on SQLite for Android. DBFlow utilizes annotation processing to generate SQLite boilerplate for you and provides a powerful SQLite query language that makes using SQLite a joy.

DBFlow is built from a collection of some of the best features of many database libraries.  Don't let an ORM or library get in your way, let the code you write in your applications be the best as possible.

A few reasons to use DBFlow:

- **Extensibility**: No restrictions on inheritance of your table classes. They can be plain POJOs or extend our convenience `BaseModel` class. You can extend other DB table classes to reuse columns from the same project or other packages.
- **Speed**: Built with java's annotation processing code generation, there's almost zero runtime performance hit by using this library (only reflection is creation of the main, generated database module's constructor). This library saves hours of boilerplate code and maintenance by generating the code for you. We also have support for lazy-loading relationships on-demand such as `@ForeignKey` or `@OneToMany`.
- **SQLite Query Flow**: The queries in this library adhere as closely as possible to SQLite native queries. `(select(name, screenSize) from(Android::class) where name.is("Nexus 5x") and version.is(6.0)).result`
- **Open Source**: This library is fully open source and contributions are not only welcomed, but encouraged.
- **Robust**: We support `Trigger`, `ModelView`, `Index`, `Migration`, built-in ways to manage database access, and many more features. SQLCipher, RXJava1/2, Android Architecture Paging and more!
- **Multiple Databases, Multiple Modules**: we seamlessly support multiple database files, databaseForTable modules using DBFlow in other dependencies, simultaneously.
- **Built On SQLite**: SQLite is the most widely used database engine in world and using it as your base, you are not tied to a limited set of platforms or libraries.

# Changelog

Changes exist in the [releases tab](https://github.com/Raizlabs/DBFlow/releases).

# Usage Docs
For more detailed usage, check out it out [here](https://agrosner.gitbooks.io/dbflow/content/)

# Including in your project

```groovy

allProjects {
  repositories {
    // required to find the project's artifacts
    maven { url "https://www.jitpack.io" }
  }
}
```

Add the library to the project-level build.gradle, using the apt plugin to enable Annotation Processing:

```groovy

  apply plugin: 'kotlin-kapt' // only required for kotlin consumers.

  def dbflow_version = "5.0.0-alpha1"
  // or 10-digit short-hash of a specific commit. (Useful for bugs fixed in develop, but not in a release yet)

  dependencies {

    // Annotation Processor
    // if Java use this. If using Kotlin do NOT use this.
    annotationProcessor "com.github.Raizlabs.DBFlow:dbflow-processor:${dbflow_version}"

    // Use if Kotlin user.
    kapt "com.github.Raizlabs.DBFlow:dbflow-processor:${dbflow_version}"

    // core set of libraries
    compile "com.github.Raizlabs.DBFlow:dbflow-core:${dbflow_version}"

    // main dbflow project
    compile "com.github.Raizlabs.DBFlow:dbflow:${dbflow_version}"

    // sql-cipher database encryption (optional)
    compile "com.github.Raizlabs.DBFlow:dbflow-sqlcipher:${dbflow_version}"
    compile "net.zetetic:android-database-sqlcipher:${sqlcipher_version}@aar"

    // RXJava 1 support
    compile "com.github.Raizlabs.DBFlow:dbflow-rx:${dbflow_version}"

    // RXJava 2 support
    compile "com.github.Raizlabs.DBFlow:dbflow-rx2:${dbflow_version}"

    // Kotlin Coroutines
    compile "com.github.Raizlabs.DBFlow:coroutines:${dbflow_version}"

    // Android Architecture Components Paging Library
    compile "com.github.Raizlabs.DBFlow:dbflow-paging:${dbflow_version}"

  }

```

# Pull Requests
I welcome and encourage all pull requests. Here are some basic rules to follow to ensure timely addition of your request:
  1. Match coding style (braces, spacing, etc.) This is best achieved using **Reformat Code** shortcut, <kbd>command</kbd>+<kbd>option</kbd>+<kbd>L</kbd> on Mac and <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>L</kbd> on Windows, with Android Studio defaults.
  2. If its a feature, bugfix, or anything please only change code to what you specify.
  3. Please keep PR titles easy to read and descriptive of changes, this will make them easier to merge :)
  4. Pull requests _must_ be made against `develop` branch. Any other branch (unless specified by the maintainers) will get **rejected**.
  5. Have fun!

# Maintained By
[agrosner](https://github.com/agrosner) ([@agrosner](https://www.twitter.com/agrosner))
