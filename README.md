[![Android Weekly](http://img.shields.io/badge/Android%20Weekly-%23129-2CB3E5.svg?style=flat)](http://androidweekly.net/issues/issue-129)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-DBFlow-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1134) 
[![Raizlabs Repository](http://img.shields.io/badge/Raizlabs%20Repository-1.1.7-blue.svg?style=flat)](https://github.com/Raizlabs/maven-releases)

DBFlow
======

A robust, powerful, and very simple ORM android database library with **annotation processing**.

The library eliminates the need for writing most SQL statements, writing ``ContentValues`` for every table, converting cursors into models, and so much more. 

Let DBFlow make SQL code _flow_ like a _steady_ stream so you can focus on your complex problem and not be hindered by repetitive code writing. 

This library is based on [Active Android](https://github.com/pardom/ActiveAndroid), [Schematic](https://github.com/SimonVT/schematic), [Ollie](https://github.com/pardom/ollie/), and [Sprinkles](https://github.com/emilsjolander/sprinkles), but takes the **best** of each while offering much more functionality and extensibility. 

What sets this library apart: **every** feature has been unit tested to ensure functionality, baked in support for **multiple** databases seamlessly, powerful and fluid builder logic in expressing SQL statements, **annotation processing** to enable blistering speed, ```ModelContainer``` classes that enable direct to database parsing for data such as JSON, and rich interface classes that enable powerful flexibility.

## Changelog

#### 1.1.7
  1. ```ConditionQueryBuilder``` no longer maps ```columnName``` to a ```Condition``` since if we wanted to do ```OR``` operation on the same column multiple times, it would only take the last condition since it was a ```Map``` of columnNames.
  2. Added ```Condition.Operation``` constant class for operation and SQL method constants
  3. Fixed the ```IS NULL``` and ```IS NOT NULL``` conditions and wrote test to ensure working

#### 1.1.6
  1. Fixes issue where boxed primitive values such as Long, when null, were throwing ```NullPointerException``` in ```bindToStatement()```. Added a test to prevent any future issues.
  2. ```From.as()``` wasn't using type parameters in return, thus a warning would be thrown.
  3. Added two new methods to ```Queriable```: ```queryCursorList()``` and ```queryTableList()```. These corresponding methods will make constructing a ```FlowCursorList``` or ```FlowTableList``` from a completed query much simpler.

#### 1.1.5
  1. Fixed issue where using non-string foreign keys caused a build error.
  2. Optimized loading foreign key objects from the DB by checking using the ```Cursor.isNull()``` method before calling a SELECT query (thanks [Michal](https://github.com/mozarcik))
  3. Made ```FlowCursorList``` and ```FlowTableList``` more robust and flexible by enabling ```Queriable``` objects to be used to generate it's internal cursor. Added ```Condition...``` parameter to ```FlowTableList``` as well.
  4. Added two new methods to ```Queriable```: ```queryClose()``` will execute a query on the DB and close the ```Cursor``` if needed. ```getTable()``` simply returns the table that the query comes from.
  5. Made ```TransactionListenerAdapter``` both the class and the ```onResultReceived()``` method no longer abstract to make it useful in situations other than results.

#### 1.1.4
  1. Fixed issue where ```Collate``` enum was not appending ```COLLATE``` to the SQL query
  2. Added the ability to chain ```Condition``` together using different separators. Just call ```separator()``` on a ```Condition``` used in a ```ConditionQueryBuilder```. 
  3. Added tests for ```Between``` and these fixes.

#### 1.1.3
  1. Fixes an issue with Boolean converter throwing a ```NullPointerException``` when a ```ModelContainer``` does not contain the field.
  2. Added null checks in the ```toModel()``` method of a ```ContainerAdapter``` definition class.
  3. We ```bindNull()``` and ```putNull()``` for missing foreign key fields in the ````$Adapter``` definition, previously this bug did not allow the removal of foreign key object fields.
  4. Added a  ```purgeQueue()``` and the ability to set the priority of the batches in the ```DBBatchSaveQueue```
  5. Added the ```Between``` method for SQLite statements
  6. Added  a method in ```Delete``` for clearing multiple tables

#### 1.1.2
  1. Added support for SQLite ```COLLATE``` in ```@Column``` and ```Condition``` classes
  2. Added support for ```DEFAULT``` values in column creation.
  3. Deprecated ```ResultReceiver``` to replace it with ```TransactionListenerAdapter``` which provides a base implementation of ```TransactionListener```. ```TransactionListener``` provides more callback methods to the state of a DB transaction. As a result ```ResultReceiver``` is no longer an interface, rather an abstract class. ***NOTE:*** ```BaseResultTransaction```'s ```TransactionListener``` must return true from ```hasResult(BaseResultTransaction, ResultClass)``` to have ```onResultReceived()``` called.
  4. ```FlowCursorList``` is more flexible by adding methods to clear the cache, dynamically enable/disable the cache, and set a custom ```Where``` for the cursor to use.

#### 1.1.1

Fixed an issue where ```TypeConverter``` for boolean values would incorrectly try to cast in ```bindLong``` to ```Boolean```

#### 1.1.0

Marks a large change in the library:
  1. All base package names "com.grosner.dbflow" are now "com.raizlabs.android.dbflow"
  2. The library will no longer be updated on Maven Central. For versions < 1.1, you can find it there, otherwise you will need to use the new "https://github.com/Raizlabs/maven-releases" repository. Also the group artifact has changed from ```com.github.agrosner``` to ```com.raizlabs.android```
  3. Significant changes to ```ModelAdapter```, inserts now reuse ```SQLiteStatement```for each table. ```save(ContentValues)``` has changed to ```bindToContentValues(ContentValues, ModelClass)``` 
  4. Support for backing up databases and ```pragma quick_check``` for maintaining DB integrity
  5. Multiline migration file query support. commands must be separated by ";"
  6. Bug fixes
  7. Added a postfix operator to ```Condition``` as ```postfix()```

for older changes, from other xx.xx versions, check it out [here](https://github.com/Raizlabs/DBFlow/wiki)


## Including in your project

### Gradle

Using the [apt plugin for gradle](https://bitbucket.org/hvisser/android-apt).

For 1.1+ (recommended), add this repo to your build.gradle:

```groovy

repositories {
  maven { url "https://raw.github.com/Raizlabs/maven-releases/master/releases" }
}

dependencies {
  apt 'com.raizlabs.android:DBFlow-Compiler:1.1.5'
  compile 'com.raizlabs.android:DBFlow-Core:1.1.5' 
  compile 'com.raizlabs.android:DBFlow:1.1.5'
}

```

### Eclipse

Not supported as google is no longer supporting it.

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

in DBFlow migrations are separate, public classes that contain both the ```@Migration``` and ```Migration```interface. If you are using multiple databases, you're required to specify it for the migration.

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

To see more go to the full [tutorial](https://github.com/agrosner/DBFlow/wiki/Basic-Query-Wrapping)

## Model Containers

Model containers are classes that __imitate__ and use the blueprint of ```Model``` classes in order to save data such as JSON, Hashmap, or your own kind of data to the database. To create your own, extend the ```BaseModelContainer``` class or implement the ```ModelContainer``` interface.  More info [here](https://github.com/agrosner/DBFlow/wiki/Model-Containers)

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

```TypeConverter``` allows non-Model objects to save to the database by converting it from its ```Model``` value to its ```Database``` value. These are statically allocated accross all databases. More info [here](https://github.com/agrosner/DBFlow/wiki/Type-Conversion)

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

```ModelView``` are a special kind of ```Model``` that creates a database **VIEW** based on a special SQL statement. They must reference another ```Model``` class currently. 

```java

@ModelView(query = "SELECT * FROM TestModel2 WHERE model_order > 5", databaseName = TestDatabase.NAME)
public class TestModelView extends BaseModelView<TestModel2> {
    @Column
    long model_order;
}

```

