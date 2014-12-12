[![Android Weekly](http://img.shields.io/badge/Android%20Weekly-%23129-2CB3E5.svg?style=flat)](http://androidweekly.net/issues/issue-129)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-DBFlow-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1134) [![Raizlabs Repository](http://img.shields.io/badge/Raizlabs%20Repository-1.0.3-blue.svg?style=flat)](https://github.com/Raizlabs/maven-releases)
DBFlow
======

A robust, powerful, and very simple ORM android database library with **annotation processing**.

The library eliminates the need for writing most SQL statements, writing ``ContentValues`` for every table, converting cursors into models, and so much more. 

Let DBFlow make SQL code _flow_ like a _steady_ stream so you can focus on your complex problem and not be hindered by repetitive code writing. 

This library is based on [Active Android](https://github.com/pardom/ActiveAndroid), [Schematic](https://github.com/SimonVT/schematic), [Ollie](https://github.com/pardom/ollie/), and [Sprinkles](https://github.com/emilsjolander/sprinkles), but takes the **best** of each while offering much more functionality and extensibility. 

What sets this library apart: **every** feature has been unit tested to ensure functionality, baked in support for **multiple** databases seamlessly, powerful and fluid builder logic in expressing SQL statements, **annotation processing** to enable blistering speed, ```ModelContainer``` classes that enable direct to database parsing for data such as JSON, and rich interface classes that enable powerful flexibility.

## Changelog

#### 1.1.0

Marks a large change in the library:
  1. All base package names "com.grosner.dbflow" are now "com.raizlabs.android.dbflow"
  2. The library will no longer be hosted on Maven Central. It now uses the new "https://github.com/Raizlabs/maven-releases" repository
  3. Significant changes to ```ModelAdapter```, inserts now reuse ```SQLiteStatement```for each table. ```save(ContentValues)``` has changed to ```bindToContentValues(ContentValues, ModelClass)``` 
  4. Support for backing up databases and ```pragma quick_check``` for maintaining DB integrity
  5. Multiline migration file query support. commands must be separated by ";"
  6. Bug fixes
  7. Added a postfix operator to ```Condition``` as ```postfix()```

#### 1.0.3

  1. Added ```JSONArrayModel```, which will operate on all items and backed by a ```JSONArray```
  2. ```@ContainerKey``` enables different key from a model container ```@Key``` field

#### 1.0.2
  1. Fixes issue in ```FlowCursorList``` that caused the cursor to reset everytime ```getItem(int)``` was called
  2. Fixes "DSC" to "DESC" for SQL orderby 


## Including in your project

### Gradle

Using the [apt plugin for gradle](https://bitbucket.org/hvisser/android-apt).

For 1.1+ (recommended), add this repo to your build.gradle:

```groovy

repositories {
  maven { url "https://raw.github.com/Raizlabs/maven-releases/master/releases" }
}

```

Add this to the build.gradle (replace with intended version):

```groovy
dependencies {
  apt 'com.github.agrosner:DBFlow-Compiler:1.1.+'
  compile 'com.github.agrosner:DBFlow-Core:1.1.+' 
  compile 'com.github.agrosner:DBFlow:1.1.+'
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

