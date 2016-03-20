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
List<Automobile> autos = SQLite()
                            .select()
                            .from(Automobile.class)
                            .where(Automobile_Table.year.is(2001))
                            .and(Automobile_Table.model.is("Camry"))
                            .queryList();

// run a transaction synchronous easily.
TransactionManager.transact(AppDatabase.NAME, new Runnable() {
  @Override
  public void run() {
    // perform some transaction.
  }
})

```

## Proguard

Since DBFlow uses annotation processing, which is run pre-proguard phase,
the configuration is highly minimal:

```
-keep class * extends com.raizlabs.android.dbflow.config.DatabaseHolder { *; }
```

If you are using [multiple modules](/usage2/MultipleModules.md), you will need
to add additional rules here:

for argument:

```groovy

apt {
    arguments {
        targetModuleName 'Test'
    }
}

```

## Sections

The list of documentation is listed here:

[Models and Getting Started](/usage2/ModelsAndGettingStarted.md)
