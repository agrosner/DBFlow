![Image](https://github.com/agrosner/DBFlow/blob/develop/dbflow_banner.png?raw=true)

[![JitPack.io](https://img.shields.io/badge/JitPack.io-3.0.0beta1-red.svg?style=flat)](https://jitpack.io/#Raizlabs/DBFlow) [![Android Weekly](http://img.shields.io/badge/Android%20Weekly-%23129-2CB3E5.svg?style=flat)](http://androidweekly.net/issues/issue-129) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-DBFlow-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1134)

A robust, powerful, and very simple ORM android database library with **annotation processing**.

The library is built on speed, performance, and approachability. It not only eliminates most boiler-plate code for dealing with databases, but also provides a powerful and simple API to manage interactions.

Let DBFlow make SQL code _flow_ like a _steady_ stream so you can focus on writing amazing apps.

# Why Use DBFlow vs other solutions
DBFlow was built with the intention of bringing the best of all features from other ORM database libraries and to do it even better. It was also built to not limit how you can code your problems, but make it _significantly_ easier to make amazing applications. Don't let an ORM or library get in your way, let the code you write in your applications be the best as possible.

  1. **Extensibility**: `Model` is just an interface, no subclass required, but as a convenience we recommend using `BaseModel`. You can extend non-`Model` classes in different packages and use them as your DB tables. Also you can subclass other `Model` to join the `@Column` together, and again they can be in different packages. _Also, subclass objects in this library to suit your needs_.
  2. **Speed**: Built with java's annotation processing code generation, there's zero runtime performance hit by using this library. This library saves hours of boilerplate code and maintenance by generating the code for you. With powerful model caching (multiple primary key `Model` too), you can surpass the speed of SQLite by reusing where possible. We have support for lazy-loading relationships on-demand such as `@ForeignKey` or `@OneToMany` that make queries happen super-fast.
  3. **SQLite Query Flow**: The queries in this library adhere as closely as possible to SQLite native queries. `select(name, screenSize).from(Android.class).where(name.is("Nexus 5x")).and(version.is(6.0)).querySingle()`
  4. **Open Source**: This library is fully open source and contributions are not only welcomed, but encouraged.
  5. **Robust**: We support `Trigger`, `ModelView`, `Index`, `Migration`, built-in database request queue to perform operations on same thread, and many more features.
  6. **Multiple Databases, Multiple Modules**: we seamlessly support multiple database files, database modules using DBFlow in other dependencies, simultaneously.
  7. **Built On SQLite**: SQLite is the most widely used database engine in world and using it as your base, you are not tied to a limited set of platforms or libraries.

## Applications That Use DBFlow
If you wish to have your application featured here, please file a [ticket](https://github.com/Raizlabs/DBFlow/issues).
1. Anonymous 1: An application that has over 1.5 million active installs
2. Anonymous 2: An application that will have over 1 million active installs
3. [University of Oslo DHIS2 Android SDK](https://github.com/dhis2/dhis2-android-sdk)

# Changelog
# 3.0-beta1
Many large updates to the library. Probably the most significant changes since the library  was written. Most major changes are [here](https://github.com/Raizlabs/DBFlow/blob/master/usage/Migration3Guide.md)

for older changes, from other xx.xx versions, check it out [here](https://github.com/Raizlabs/DBFlow/wiki)

# Usage Docs
For more detailed usage, check out these sections:

[Getting Started](https://github.com/Raizlabs/DBFlow/blob/master/usage/GettingStarted.md)

[Tables and Database Properties](https://github.com/Raizlabs/DBFlow/blob/master/usage/DBStructure.md)

[Multiple Instances of DBFlow / Database Modules](https://github.com/Raizlabs/DBFlow/blob/master/usage/DatabaseModules.md)

[SQL Statements Using the Wrapper Classes](https://github.com/Raizlabs/DBFlow/blob/master/usage/SQLQuery.md)

[Properties & Conditions](https://github.com/Raizlabs/DBFlow/blob/master/usage/Conditions.md)

[Transactions](https://github.com/Raizlabs/DBFlow/blob/master/usage/Transactions.md)

[Type Converters](https://github.com/Raizlabs/DBFlow/blob/master/usage/TypeConverters.md)

[Powerful Model Caching](https://github.com/Raizlabs/DBFlow/blob/master/usage/ModelCaching.md)

[Content Provider Generation](https://github.com/Raizlabs/DBFlow/blob/master/usage/ContentProviderGenerators.md)

[Migrations](https://github.com/Raizlabs/DBFlow/blob/master/usage/Migrations.md)

[Model Containers](https://github.com/Raizlabs/DBFlow/blob/master/usage/ModelContainers.md)

[Observing Models](https://github.com/Raizlabs/DBFlow/blob/master/usage/ObservableModels.md)

[Queries as Lists](https://github.com/Raizlabs/DBFlow/blob/master/usage/TableList.md)

[Triggers, Indexes, and More](https://github.com/Raizlabs/DBFlow/blob/master/usage/TriggersIndexesAndMore.md)

# Including in your project
We need to include the [apt plugin](https://bitbucket.org/hvisser/android-apt) in our classpath to enable Annotation Processing:

```groovy

buildscript {
    repositories {
      // required for this library, don't use mavenCentral()
        jcenter()
    }
    dependencies {
         'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}
```

Add this maven url to your project.

```groovy
allProjects {
  repositories {
    maven { url "https://jitpack.io" }
  }
}
```

Add the library to the project-level build.gradle, using the  to enable Annotation Processing:

```groovy

  apply plugin: 'com.neenbedankt.android-apt'

  dependencies {
    apt 'com.raizlabs.android:dbflow-processor:3.0.0-beta1'
    compile "com.raizlabs.android:dbflow-core:3.0.0-beta1"
    compile "com.raizlabs.android:dbflow:3.0.0-beta1"
  }
```

If you wish to grab the latest develop branch in your project, use JitPack dependencies:

```groovy

  dependencies {
    apt 'com.github.Raizlabs.DBFlow:dbflow-processor:develop-SNAPSHOT'
    compile "com.github.Raizlabs.DBFlow:dbflow-core:develop-SNAPSHOT"
    compile "com.github.Raizlabs.DBFlow:dbflow:develop-SNAPSHOT"
  }
```

You can also specify a commit hash instead of `develop-SNAPSHOT` to grab a specific commit.

# Pull Requests
I welcome and encourage all pull requests. It usually will take me within 24-48 hours to respond to any issue or request. Here are some basic rules to follow to ensure timely addition of your request:
  1. Match coding style (braces, spacing, etc.) This is best achieved using CMD+Option+L (Reformat code) on Mac (not sure for Windows) with Android Studio defaults.
  2. If its a feature, bugfix, or anything please only change code to what you specify.
  3. Please keep PR titles easy to read and descriptive of changes, this will make them easier to merge :)
  4. Pull requests _must_ be made against `develop` branch. Any other branch (unless specified by the maintainers) will get rejected.
  5. Have fun!

# Maintainers
[agrosner](https://github.com/agrosner) ([@agrosner](https://www.twitter.com/agrosner))

# Contributors
[wongcain](https://github.com/wongcain)

[mozarcik](https://github.com/mozarcik)

[mickele](https://github.com/mickele)

[intrications](https://github.com/intrications)

[mcumings](https://github.com/mcumings)

[ktzouno](https://github.com/ktzouno)
