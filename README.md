[![Android Weekly](http://img.shields.io/badge/Android%20Weekly-%23129-2CB3E5.svg?style=flat)](http://androidweekly.net/issues/issue-129)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-DBFlow-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1134)
[![Raizlabs Repository](http://img.shields.io/badge/Raizlabs%20Repository-1.8.0-blue.svg?style=flat)](https://github.com/Raizlabs/maven-releases)

DBFlow
======

A robust, powerful, and very simple ORM android database library with **annotation processing**.

The library is built on speed, performance, and approachability. It not only eliminates most boiler-plate code for dealing with databases, but also provides a powerful and simple API to manage interactions.

Let DBFlow make SQL code _flow_ like a _steady_ stream so you can focus on writing
amazing apps.

What sets this library apart:
  1. Many, many unit tests on nearly __every__ feature.
  2. Built on maximum performance using **annotation processing**, lazy-loading, and speed-tests [here](https://github.com/Raizlabs/AndroidDatabaseLibraryComparison)
  3. Built-in model caching for blazing fast retrieval and ability to define own
  cache.
  3. Powerful and fluid SQL-wrapping statements
  4. Triggers, Views, Indexes, and many more SQLite features.
  5. Seamless multi-database support.
  6. Direct-to-database parsing for data such as JSON
  7. Flexibility in the API enabling you to override functionality to suit your needs.
  8. ```ContentProvider``` generation using annotations

## Applications That Use DBFlow

If you wish to have your application featured here, please file an [issue](https://github.com/Raizlabs/DBFlow/issues).

1. Anonymous 1: An application that has over 1.5 million active installs
2. Anonymous 2: An application that will have over 1 million active installs
3. [University of Oslo DHIS2 Android SDK](https://github.com/dhis2/dhis2-android-sdk)

## Changelog

#### 1.8.0
1. Fixes bug where foreign key reference table is not quoted
2. Added validation to prevent specifying `name()` for a `Column.FOREIGN_KEY`
3. Added a `$ViewTable` static constant fields for `ModelView` so incase the view column is different from model for easy reference.

for older changes, from other xx.xx versions, check it out [here](https://github.com/Raizlabs/DBFlow/wiki)

## Usage Docs

For more detailed usage, check out these sections:

[Getting Started](https://github.com/Raizlabs/DBFlow/blob/master/usage/GettingStarted.md)

[Content Provider Generation](https://github.com/Raizlabs/DBFlow/blob/master/usage/ContentProviderGenerators.md)

[Conditions](https://github.com/Raizlabs/DBFlow/blob/master/usage/Conditions.md)

[Creating Tables and Database Structure](https://github.com/Raizlabs/DBFlow/blob/master/usage/DBStructure.md)

[Powerful Model Caching](https://github.com/Raizlabs/DBFlow/blob/master/usage/ModelCaching.md)

[Migrations](https://github.com/Raizlabs/DBFlow/blob/master/usage/Migrations.md)

[Model Containers](https://github.com/Raizlabs/DBFlow/blob/master/usage/ModelContainers.md)

[Observing Models](https://github.com/Raizlabs/DBFlow/blob/master/usage/ObservableModels.md)

[SQL Statements Using the Wrapper Classes](https://github.com/Raizlabs/DBFlow/blob/master/usage/SQLQuery.md)

[Tables as Lists](https://github.com/Raizlabs/DBFlow/blob/master/usage/TableList.md)

[Transactions](https://github.com/Raizlabs/DBFlow/blob/master/usage/Transactions.md)

[Type Converters](https://github.com/Raizlabs/DBFlow/blob/master/usage/TypeConverters.md)

[Triggers, Indexes, and More](https://github.com/Raizlabs/DBFlow/blob/master/usage/TriggersIndexesAndMore.md)

### Screencasts

Listed here are tutorial screen casts for DBFlow. If more are created, they may go into the usage docs.

1. [DFlow-Installing](https://www.youtube.com/watch?v=UveI8_wfEoU) by @tsuharesu


## Including in your project

Add the maven repo url to your root build.gradle in the ```allProjects{}``` blocks:

```groovy
  ...

  allProjects {
    repositories {
        maven { url "https://raw.github.com/Raizlabs/maven-releases/master/releases" }
    }
  }


```

Add the library to the project-level build.gradle, using the [apt plugin](https://bitbucket.org/hvisser/android-apt) to enable Annotation Processing:

```groovy

  apply plugin: 'com.neenbedankt.android-apt'

  dependencies {
    apt 'com.raizlabs.android:DBFlow-Compiler:1.8.0'
    compile "com.raizlabs.android:DBFlow-Core:1.8.0"
    compile "com.raizlabs.android:DBFlow:1.8.0"
  }

```

## Pull Requests

I welcome and encourage all pull requests. It usually will take me within 24-48 hours to respond to any issue or request. Here are some basic rules to follow to ensure timely addition of your request:
  1. Match coding style (braces, spacing, etc.) This is best achieved using CMD+Option+L (Reformat code) on Mac (not sure for Windows) with Android Studio defaults.
  2. If its a feature, bugfix, or anything please only change code to what you specify.
   **DO NOT** do this: Ex: Title "Fixes Crash Related to Bug" includes other files that were changed without explanation or doesn't relate to the bug you fixed. Or another example is a non-descriptive title "Fixes Stuff".
  3. Pull requests must be made against ```develop``` branch.
  4. Have fun!


## Maintainers

[agrosner](https://github.com/agrosner) ([@agrosner](https://www.twitter.com/agrosner))

## Contributors

[wongcain](https://github.com/wongcain)

[mozarcik](https://github.com/mozarcik)

[mickele](https://github.com/mickele)

[intrications](https://github.com/intrications)

[mcumings](https://github.com/mcumings)

[ktzouno](https://github.com/ktzouno)
