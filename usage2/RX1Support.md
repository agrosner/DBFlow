# RXJava 1 Support

RXJava support in DBFlow is an _incubating_ feature and likely to change over time.
Currently it supports
    1. `Insert`, `Update`, `Delete`, `Set`, `Join`, and all wrapper query mechanisms by wrapping them in `rx()`
    2. Single + `List` model `save()`, `insert()`, `update()`, and `delete()`.
    3. Streaming a set of results from a query
    4. Observing on table changes for specific `ModelQueriable` and providing ability to query from that set repeatedly as needed.
    5. Kotlin extension methods in a separate artifact.

## Getting Started

Add the separate package to your project:
```groovy

dependencies {
  compile "com.github.Raizlabs.DBFlow:dbflow-rx:${dbflow_version}"
  compile "com.github.Raizlabs.DBFlow:dbflow-rx-kotlinextensions:${dbflow_version}"
}

```

## Wrapper Language
Using the classes is as easy as wrapping all SQL wrapper calls with `RXSQLite.rx()` (Kotlin we supply extension method):

Before:
```kotlin

val list = SQLite.select()
  .from(MyTable.class)
  .queryList()

```

After:

```kotlin

RXSQLite.rx(select().from(MyTable.class))
  .list { list ->

  }

```

or with Kotlin extension method:
```kotlin

  select.from(MyTable.class)
  .rx()
  .list { list ->

  }

```

## Model operations
To make the transition as smoothest as possible, we've provided a `BaseRXModel` which replaces `BaseModel` for convenience in the RX space.

```kotlin

class Person(@PrimaryKey var id: Int = 0, @Column var name: String? = "") : BaseRXModel

```

Operations are as easy as:
```kotlin

Person(5, "Andrew Grosner").insert()
  .subscribe { rowId ->

  }

```

## Query Stream

We can use RX to stream the result set, one at a time from the `ModelQueriable` using
the method `queryStreamResults()`:

```kotlin

RXSQLite.wrap(select().from(TestModel1::class.java))
   .rx()
   .queryStreamResults()
   .subscribe { model ->

   }

```

## Kotlin Support

Most of the support mirrors [kotlin support](/usage2/KotlinSupport.md) with a few
minor changes.

Extension properties/methods include:
  1. `rx()` extension method making it super easy to integrate RX.
  2. `RXModelQueriable.streamResults`  - stream results one at time to a `Subscription`
  3.  `list`, `result`,`streamResults`, `cursorResult`,`statement`, `hasData`, `cursor`, and `count` all provide a method lambda that is called within a `Subscription`.

```kotlin

select from MyTable::class
  where (MyTable.name `is` "Good")
  list { list -> //

  }

```

which is the same with RX as:

```kotlin

(select.from(MyTable::class.java)
  .where(MyTable.name `is` "Good"))
  .rx()
  .list { list ->

  }

```


Or if we want to get pretty with `BaseRXModel` + extensions:

```kotlin

Person("Somebody").save { success ->
  // do something
}

Person("Somebody").update { success ->
  // do something
}

Person("Somebody").insert { rowId ->
  // do something
}

Person("Somebody").delete { success ->
  // do something
}

```
