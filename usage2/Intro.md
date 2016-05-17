# DBFlow

DBFlow for Android lets you write very efficient database code while remaining
expressive and concise.

```java

@Table(database = AppDatabase.class)
public class Automobile extends BaseModel { // extending BaseModel not required, you can also implement Model

  @PrimaryKey
  String vin;

  @Column
  String make;

  @Column
  String model;

  @Column
  int year;

}

Automobile venza = new Automobile();
venza.vin = "499499449";
venza.make = "Toyota";
venza.model = "Venza";
venza.year = 2013;
venza.save(); // inserts if not exists by primary key, updates if exists.

// querying
// SELECT * FROM `Automobile` WHERE `year`=2001 AND `model`='Camry'
// we autogen a "_Table" class that contains convenience Properties which provide easy SQL ops.
SQLite().select()
        .from(Automobile.class)
        .where(Automobile_Table.year.is(2001))
        .and(Automobile_Table.model.is("Camry"))
        .async()
        .queryResultCallback(new QueryTransaction.QueryResultCallback<TestModel1>() {
            @Override
            public void onQueryResult(QueryTransaction transaction, @NonNull CursorResult<TestModel1> tResult) {
              // called when query returns on UI thread
              List<Automobile> autos = tResult.toListClose();
              // do something with results
            }
        }, new Transaction.Error() {
            @Override
            public void onError(Transaction transaction, Throwable error) {
              // handle any errors
            }
        }).execute();

// run a transaction synchronous easily.
DatabaseDefinition database = FlowManager.getDatabase(AppDatabase.class);
database.executeTransaction(new ITransaction() {
            @Override
            public void execute(DatabaseWrapper databaseWrapper) {
              // do something here
            }
});

// run asynchronous transactions easily, with expressive builders
database.beginTransactionAsync(new ITransaction() {
            @Override
            public void execute(DatabaseWrapper databaseWrapper) {
                // do something in BG
            }
        }).success(successCallback).error(errorCallback).build().execute();

```

## Proguard

Since DBFlow uses annotation processing, which is run pre-proguard phase,
the configuration is highly minimal:

```
-keep class * extends com.raizlabs.android.dbflow.config.DatabaseHolder { *; }
```

## Sections

For migrating from 2.x to 3.0, read [here](/usage2/Migration3Guide.md)

The list of documentation is listed here:

  [Getting Started](/usage2/GettingStarted.md)

  [Databases](/usage2/Databases.md)

  [Models](/usage2/Models.md)

  [Relationships](/usage2/Relationships.md)

  [Storing Data](/usage2/StoringData.md)

  [Retrieval](/usage2/Retrieval.md)

  [Storing and querying date](/usage2/StoringDate.md)
  
  [The SQLite Wrapper Language](/usage2/SQLiteWrapperLanguage.md)

  [Caching](/usage2/Caching.md)

  [List-Based Queries](/usage2/ListBasedQueries.md)

  [Migrations](/usage2/Migrations.md)

  [Observability](/usage2/Observability.md)

  [Type Converters](/usage2/TypeConverters.md)

For advanced DBFlow usages:

  [Kotlin Support](/usage2/KotlinSupport.md)

  [Multiple Modules](/usage2/MultipleModules.md)

  [Views](/usage2/ModelViews.md)

  [Query Models](/usage2/QueryModels.md)

  [Model Containers](/usage2/ModelContainers.md)

  [Indexing](/usage2/Indexing.md)
  
  [SQLCipher](/usage2/SQLCipherSupport.md)
