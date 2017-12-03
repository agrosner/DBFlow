# DBFlow
## Outdated - use https://agrosner.gitbooks.io/dbflow/content

DBFlow for Android lets you write very efficient database code while remaining
expressive and concise.
DBFlow fully supports Kotlin and public API reflects that via `@NonNull` and `@Nullable` annotations.

```kotlin

@Table(database = AppDatabase::class)
class Automobile(@PrimaryKey var vin: String? = null,
                 @Column var make: String? = null,
                 @Column var model: String = "", // nullability of kotlin fields respected
                 @Column var year: Int = 0) // default constructor required

val venza = new Automobile(vin = "499499449",
                           make = "Toyota",
                           model = "Venza",
                           year = 2013);
venza.save();
// inserts if not exists by primary key, updates if exists.
// Kotlin extensions add methods found in BaseModel

// querying
// SELECT * FROM `Automobile` WHERE `year`=2001 AND `model`='Camry'
// we autogen a "_Table" class that contains convenience Properties which provide easy SQL ops.
(select from Automobile:::class
  where Automobile_Table.year.is(2001)
  and Automobile_Table.model.is("Camry")).async()

(select from Automobile::class
        where Automobile_Table.year.`is`(2001)
        and Automobile_Table.model.`is`("Camry")).async()
 .queryResultCallback { transaction, tResult ->
   // called when query returns on UI thread
   val autos = tResult.toListClose()
   // do something with results
 }
 .error { transaction, error ->
     // handle any errors
 }.execute()

 // run a transaction synchronous easily.
 val database = database<AppDatabase>()
 database.executeTransaction {
     // do something here
 }

 // run asynchronous transactions easily, with expressive builders
 database.beginTransactionAsync {
     // do something in BG
 }.success { transaction ->  

 }.error {  transaction, error ->  

 }.build().execute()

```

## Proguard

Since DBFlow uses annotation processing, which is run pre-proguard phase,
the configuration is highly minimal:

```
-keep class * extends com.raizlabs.android.dbflow.config.DatabaseHolder { *; }
```

## Sections

For migrating from 3.x to 4.0, read [here](/usage2/Migration4Guide.md)

The list of documentation is listed here:

  [Getting Started](/usage2/GettingStarted.md)

  [Databases](/usage2/Databases.md)

  [Models](/usage2/Models.md)

  [Relationships](/usage2/Relationships.md)

  [Storing Data](/usage2/StoringData.md)

  [Retrieval](/usage2/Retrieval.md)

  [The SQLite Wrapper Language](/usage2/SQLiteWrapperLanguage.md)

  [Caching](/usage2/Caching.md)

  [List-Based Queries](/usage2/ListBasedQueries.md)

  [Migrations](/usage2/Migrations.md)

  [Observability](/usage2/Observability.md)

  [Type Converters](/usage2/TypeConverters.md)

For advanced DBFlow usages:

  [Kotlin Support](/usage2/KotlinSupport.md)

  [RX Java Support](/usage2/RXSupport.md)

  [Multiple Modules](/usage2/MultipleModules.md)

  [Views](/usage2/ModelViews.md)

  [Query Models](/usage2/QueryModels.md)

  [Indexing](/usage2/Indexing.md)

  [SQLCipher](/usage2/SQLCipherSupport.md)
