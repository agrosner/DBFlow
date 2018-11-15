# RXJavaSupport

RXJava support in DBFlow is an _incubating_ feature and likely to change over time. We support both RX1 and RX2 and have made the extensions + DBFlow compatibility almost identical - save for the changes and where it makes sense in each version.

Currently it supports 1. Any `ModelQueriable` can be wrapped in a `Single`, `Maybe`, or `Flowable` \(to continuously observe changes\). 2. Single + `List` model `save()`, `insert()`, `update()`, and `delete()`. 3. Streaming a set of results from a query 4. Observing on table changes for specific `ModelQueriable` and providing ability to query from that set repeatedly as needed.

## Getting Started

Add the separate packages to your project:

```groovy
dependencies {

  // RXJava2
  compile "com.github.agrosner.dbflow:reactive-streams:${dbflow_version}"

}
```

## Wrapper Language

Using the classes is as easy as wrapping all SQL wrapper calls with `RXSQLite.rx()` \(Kotlin we supply extension method\):

Before:

```kotlin
val list = (select 
  from MyTable::class)
  .queryList(db)
```

After:

```kotlin
val list = (select 
  from MyTable::class)
  .asSingle()
  .subscribeBy { list ->  
    // use list
  }
```

## Model operations

To make the transition as smoothest as possible, we've provided a `BaseRXModel` which replaces `BaseModel` for convenience in the RX space. Or if you use Kotlin, we provide an extension on `Any`.

```kotlin
class Person(@PrimaryKey var id: Int = 0, @Column var name: String? = "") : BaseRXModel
```

Operations are as easy as:

```kotlin
Person(5, "Andrew Grosner")
  .rxinsert(db)
  .subscribeBy { rowId -> 

  }
```

## Query Stream

We can use RX to stream the result set, one at a time from the `ModelQueriable` using the method `queryStreamResults()`:

```kotlin
(select from TestModel::class)
   .queryStreamResults(db)
   .subscribeBy { model -> 

   }
```

