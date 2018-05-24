![Image](https://github.com/agrosner/DBFlow/blob/develop/dbflow_banner.png?raw=true)

[![JitPack.io](https://img.shields.io/badge/JitPack.io-5.0.0alpha1-red.svg?style=flat)](https://jitpack.io/#Raizlabs/DBFlow) [![Android Weekly](http://img.shields.io/badge/Android%20Weekly-%23129-2CB3E5.svg?style=flat)](http://androidweekly.net/issues/issue-129) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-DBFlow-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1134)

DBFlow is fast, efficient, and feature-rich Kotlin database library built on SQLite for Android. DBFlow utilizes annotation processing to generate SQLite boilerplate for you and provides a powerful SQLite query language that makes using SQLite a joy.

DBFlow is built from a collection of some of the best features of many database libraries.  Don't let an ORM or library get in your way, let the code you write in your applications be the best as possible.

Supports:

**Kotlin:** Built using the language, the library is super-concise, null-safe and efficient.

**Coroutines:** Adds coroutine support for queries.

**RX Java:** Enable applications to be reactive by listening to DB changes and ensuring your subscribers are up-to-date.

**Paging:** Android architecture component paging library support for queries via `QueryDataSource`.

**SQLCipher:** Easy database encryption support in this library.

**SQLite Query Language:** Enabling autocompletion on sqlite queries combined with Kotlin language features means SQLite-like syntax. 

# Changelog

Changes exist in the [releases tab](https://github.com/Raizlabs/DBFlow/releases).

# Usage Docs
For more detailed usage, check out it out [here](https://agrosner.gitbooks.io/dbflow/content/)

# Including in your project

Add jitpack.io to your project's repositories:
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

# Pull Requests
I welcome and encourage all pull requests. Here are some basic rules to follow to ensure timely addition of your request:
  1. Match coding style (braces, spacing, etc.) This is best achieved using **Reformat Code** shortcut, <kbd>command</kbd>+<kbd>option</kbd>+<kbd>L</kbd> on Mac and <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>L</kbd> on Windows, with Android Studio defaults.
  2. If its a feature, bugfix, or anything please only change code to what you specify.
  3. Please keep PR titles easy to read and descriptive of changes, this will make them easier to merge :)
  4. Pull requests _must_ be made against `develop` branch. Any other branch (unless specified by the maintainers) will get **rejected**.
  5. Have fun!

# Maintained By
[agrosner](https://github.com/agrosner) ([@agrosner](https://www.twitter.com/agrosner))
