[![Android Weekly](http://img.shields.io/badge/Android%20Weekly-%23129-2CB3E5.svg?style=flat)](http://androidweekly.net/issues/issue-129)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-DBFlow-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1134) 
[![Raizlabs Repository](http://img.shields.io/badge/Raizlabs%20Repository-1.3.1-blue.svg?style=flat)](https://github.com/Raizlabs/maven-releases)

DBFlow
======

A robust, powerful, and very simple ORM android database library with **annotation processing**.

The library eliminates the need for writing most SQL statements, writing ``ContentValues`` for every table, converting cursors into models, and so much more. 

Let DBFlow make SQL code _flow_ like a _steady_ stream so you can focus on your complex problem and not be hindered by repetitive code writing. 

This library is based on [Active Android](https://github.com/pardom/ActiveAndroid), [Schematic](https://github.com/SimonVT/schematic), [Ollie](https://github.com/pardom/ollie/), and [Sprinkles](https://github.com/emilsjolander/sprinkles), but takes the **best** of each while offering much more functionality and extensibility. 

What sets this library apart: **every** feature has been unit tested to ensure functionality, baked in support for **multiple** databases seamlessly, powerful and fluid builder logic in expressing SQL statements, **annotation processing** to enable blistering speed, ```ModelContainer``` classes that enable direct to database parsing for data such as JSON, and rich interface classes that enable powerful flexibility.

## Changelog

### 1.3.1
  1. Fixes issue where ```$ContainerAdapter``` generated the incorrect ```updateAutoIncrement()``` method.
  2. Also updated ```ModelContainerUtils``` to utilize the more efficient usage of ```save()```, ```insert()```, and ```update()``` just like ```Model``` already do.

### 1.3.0

  1. Adds in ```ConflictAction``` you can specify for a ```@Table``` insert and update methods. This will not work on earlier than FROYO (API 8) for update statements.
  2. Added ```InsertTransaction``` and ```InsertModelTransaction``` that handle insert queries with the ```TransactionManager```
  3. Now ```BaseModel``` will directly call ```UPDATE``` and ```INSERT``` methods in the ```update()``` and ```insert()``` instead of using ```save()``` when calling those methods directly for a small performance gain.
  4. Added checking and attempts. Thanks [wongcain](https://github.com/wongcain)
  5. Now supports ```ForeignKeyContainer``` which when placed in a field of a ```Model```, will hold onto the foreign key cursor data and provide an on-demand lazy loading of foreign key objects by calling ```toModel()```.
  6. Added support for ```LoadFromCursorListener```, ```SQLiteStatementListener```, and ```ContentValuesListener``` within a model to implement to perform some custom action during use of those methods.
  7. Added a ```saveForeignKeyModel()``` option in ```Column``` to disable ```save()``` on a contained foreign key ```Model``` or ```ModelContainer``` from within its associated Adapter class. Thanks [davidschreiber](https://github.com/davidschreiber)

for older changes, from other xx.xx versions, check it out [here](https://github.com/Raizlabs/DBFlow/wiki)

## Usage Docs

For more detailed usage, check out these sections:

[Conditions](https://github.com/Raizlabs/DBFlow/blob/master/usage/Conditions.md)

[Creating Tables and Database Structure](https://github.com/Raizlabs/DBFlow/blob/master/usage/DBStructure.md)

[Migrations](https://github.com/Raizlabs/DBFlow/blob/master/usage/Migrations.md)

[Model Containers](https://github.com/Raizlabs/DBFlow/blob/master/usage/ModelContainers.md)

[Observing Models](https://github.com/Raizlabs/DBFlow/blob/master/usage/ObservableModels.md)

[SQL Statements Using the Wrapper Classes](https://github.com/Raizlabs/DBFlow/blob/master/usage/SQLQuery.md)

[Tables as Lists](https://github.com/Raizlabs/DBFlow/blob/master/usage/TableList.md)

[Transactions](https://github.com/Raizlabs/DBFlow/blob/master/usage/Transactions.md)

[Type Converters](https://github.com/Raizlabs/DBFlow/blob/master/usage/TypeConverters.md)




## Including in your project

### Gradle

Add the maven repo url to your build.gradle:

```groovy

  repositories {
        maven { url "https://raw.github.com/Raizlabs/maven-releases/master/releases" }
  }

```

Add the library to the project-level build.gradle, using the [apt plugin](https://bitbucket.org/hvisser/android-apt) and the 
[AARLinkSources](https://github.com/xujiaao/AARLinkSources) plugin::

```groovy

  dependencies {
    apt 'com.raizlabs.android:DBFlow-Compiler:1.2.0'
    aarLinkSources 'com.raizlabs.android:DBFlow-Compiler:1.2.0:sources@jar'
    compile 'com.raizlabs.android:DBFlow-Core:1.2.0'
    aarLinkSources 'com.raizlabs.android:DBFlow-Core:1.2.0:sources@jar'
    compile 'com.raizlabs.android:DBFlow:1.2.0'
    aarLinkSources 'com.raizlabs.android:DBFlow:1.2.0:sources@jar'
  }

```

### Eclipse

Not supported as google is no longer supporting it.

## Pull Requests

I welcome and encourage all pull requests. It usually will take me within 24-48 hours to respond to any issue or request. Here are some basic rules to follow to ensure timely addition of your request:
  1. Match coding style (braces, spacing, etc.) This is best achieved using CMD+Option+L (Reformat code) on Mac (not sure for Windows) with Android Studio defaults.
  2. If its a feature, bugfix, or anything please only change code to what you specify. 
   **DO NOT** do this: Ex: Title "Fixes Crash Related to Bug" includes other files that were changed without explanation or doesn't relate to the bug you fixed. Or another example is a non-descriptive title "Fixes Stuff".
  3. Pull requests must be made against ```develop``` branch.
  4. Have fun!

## Configuration

We need to configure the ```FlowManager``` properly. Instead of passing in a ```Context``` wherever it is used,
we hold onto the ```Application``` context instead and reference it. 

You will need to extend the ```Application``` class for proper configuration:

```java

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FlowManager.destroy();
    }
}

```

Lastly, add the definition to the manifest (with the name that you chose for your custom application):

```xml

<application
  android:name="{packageName}.ExampleApplication"
  ...>
</application>

```

First class you need to define is the ```@Database```. It is recommended you store the name and version as static final fields.
The database name is not required for singular databases, however it is good practice to include it here.


```java

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION, foreignKeysSupported = true)
public class AppDatabase {

    public static final String NAME = "App";

    public static final int VERSION = 1;
}

```

Second, you need to define at least one ```@Table``` class. The ```databaseName``` is only required when dealing with multiple
databases. You can either implement the ```Model``` interface or extend ```BaseModel```.

```java

@Table(databaseName = TestDatabase.NAME)
public class TestModel1 extends BaseModel {
    @Column(columnType = Column.PRIMARY_KEY)
    public
    String name;
}

```

## Prepackaged Databases

So you have an existing DB you wish to include in your project. Just name the database the same as the database to copy, and put it in the ```app/src/main/assets/``` directory.

## Migrations

in DBFlow migrations are separate, public classes that contain both the ```@Migration``` and ```Migration```interface. If you are using multiple databases, you're required to specify it for the migration. For more information, check it out [here](https://github.com/Raizlabs/DBFlow/blob/master/usage/Migrations.md)

```java

@Migration(version = 2, databaseName = TestDatabase.NAME)
public class Migration1 extends BaseMigration {

    @Override
    public void onPreMigrate() {
      // called before migration, instantiate any migration query here
    }

    @Override
    public void migrate(SQLiteDatabase database) {
      // call your migration query
    }

    @Override
    public void onPostMigrate() {
      // release migration resources here
    }
}

```

## Basic Query Wrapping

The SQL language is wrapped in a nice builder notation. DBFlow generates a ```$Table``` containing static final column strings to use in your queries!

```java

new Select().from(DeviceObject.class)
                             .where(Condition.column(DeviceObject$Table.NAME).is("Samsung-Galaxy S5"))
                             .and(Condition.column(DeviceObject$Table.CARRIER).is("T-MOBILE"))
                             .and(Condition.column(DeviceObject$Table.LOCATION).is(location);

```

To see more go to the full [tutorial](https://github.com/Raizlabs/DBFlow/blob/master/usage/SQLQuery.md)

## Model Containers

Model containers are classes that __imitate__ and use the blueprint of ```Model``` classes in order to save data such as JSON, Hashmap, or your own kind of data to the database. To create your own, extend the ```BaseModelContainer``` class or implement the ```ModelContainer``` interface.  More info [here](https://github.com/Raizlabs/DBFlow/blob/master/usage/ModelContainers.md)

For example here is the ```JSONModel``` implementation:

```java

public class JSONModel<ModelClass extends Model> extends BaseModelContainer<ModelClass, JSONObject> implements Model {

    public JSONModel(JSONObject jsonObject, Class<ModelClass> table) {
        super(table, jsonObject);
    }

    public JSONModel(Class<ModelClass> table) {
        super(table, new JSONObject());
    }

    @Override
    @SuppressWarnings("unchecked")
    public BaseModelContainer getInstance(Object inValue, Class<? extends Model> columnClass) {
        return new JSONModel((JSONObject) inValue, columnClass);
    }

    @Override
    public JSONObject newDataInstance() {
        return new JSONObject();
    }

    @Override
    public Object getValue(String columnName) {
        return getData().opt(columnName);
    }

    @Override
    public void put(String columnName, Object value) {
        try {
            getData().put(columnName, value);
        } catch (JSONException e) {
            FlowLog.logError(e);
        }
    }
}

```

And then in **every** ```Model``` class you wish to use this class, you need to add the annotation ```@ContainerAdapter```. This generates the definition required to save objects correctly to the DB.

## Type Converters

```TypeConverter``` allows non-Model objects to save to the database by converting it from its ```Model``` value to its ```Database``` value. These are statically allocated accross all databases. More info [here](https://github.com/Raizlabs/DBFlow/blob/master/usage/TypeConverters.md)

```java
@com.raizlabs.android.dbflow.annotation.TypeConverter
public class CalendarConverter extends TypeConverter<Long, Calendar> {

    @Override
    public Long getDBValue(Calendar model) {
        return model.getTimeInMillis();
    }

    @Override
    public Calendar getModelValue(Long data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(data);
        return calendar;
    }
}

```

## Model Views

```ModelView``` are a special kind of ```Model``` that creates a database **VIEW** based on a special SQL statement. They must reference another ```Model``` class currently. More info [here](https://github.com/Raizlabs/DBFlow/blob/master/usage/DBStructure.md)

```java

@ModelView(query = "SELECT * FROM TestModel2 WHERE model_order > 5", databaseName = TestDatabase.NAME)
public class TestModelView extends BaseModelView<TestModel2> {
    @Column
    long model_order;
}

```

## Triggers

```Trigger``` are actions that are automatically performed before or after some action on the database. For example, we want to log changes for all updates to the name on the ```Friend``` table. 

```java

CompletedTrigger<Friend> trigger = new Trigger<Friend>("NameTrigger")
                                    .after().update(Friend.class, Friend$Table.NAME)
                                    .begin(
                                        new Insert<FriendLog>(FriendLog.class)
                                          .columns(FriendLog$Table.OLDNAME, FriendLog$Table.NEWNAME, FriendLog$Table.DATE)
                                          .values("old.Name", "new.Name", System.currentTimeMillis())
                                          };
 // starts a trigger                                         
 trigger.enable();
 
 // stops a trigger
 trigger.disable();

```

## Maintainers

[agrosner](https://github.com/agrosner) ([@agrosner](https://www.twitter.com/agrosner))

## Contributors

[wongcain](https://github.com/wongcain)

[mozarcik](https://github.com/mozarcik)

[mickele](https://github.com/mickele)

