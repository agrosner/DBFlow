# Live Data

The [LiveData](https://developer.android.com/topic/libraries/architecture/livedata#kotlin) artifact for DBFlow 
provides a simple way to extend the database `Transaction` into a `LiveData` object.

## How to Use

Construct a database transaction and then utilize the Kotlin extension function `liveData { db, queriable -> }` 
to map it to a `LiveData instance`. 

```kotlin

@Table(database = TestDatabase::class)
data class LiveDataModel(@PrimaryKey var id: String = "",
                         var name: Int = 0)
                         
fun registerObserver(owner: LifecycleOwner) {
  val data: LiveData<MutableList<LiveDataModel>> = (select from LiveDataModel::class)
                  .liveData { db, queriable -> queriable.queryList(db) }
  
  data.observe(owner) { list -> 
    // called whenever the tables change.
  }
  
  database<TestDatabase>()
    .beginTransactionAsync { db ->
        (0..2).forEach {
          LiveDataModel(id = "$it", name = it).insert(db)
        }
    }
    .execute()
    // any modification to db using model objects or SQLite wrapper will trigger LiveData to update 
    // and requery the DB.
  
}

```

