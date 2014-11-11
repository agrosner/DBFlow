![Image](https://github.com/agrosner/DBFlow/blob/master/clear-river.jpg?raw=true)


DBFlow (BETA)
======

A robust, powerful, and very simple ORM android database library.

The library eliminates the need for writing most SQL statements, writing ``ContentValues`` for every table, converting cursors into models, and so much more. 

Let DBFlow make SQL code _flow_ like a _steady_ stream so you can focus on your complex problem and not be hindered by repetitive code writing. 

This library is based on [Active Android](https://github.com/pardom/ActiveAndroid), [Schematic](https://github.com/SimonVT/schematic), [Ollie](https://github.com/pardom/ollie/), and [Sprinkles](https://github.com/emilsjolander/sprinkles), but takes the **best** of each while offering much more functionality and extensibility. 

What sets this library apart: baked in support for **multiple** databases seamlessly, powerful and fluid builder logic in expressing SQL statements, **annotation processing** to enable blistering speed, ```ModelContainer``` classes that enable direct to database parsing for data such as JSON, and rich interface classes that enable powerful flexibility.

## Including in your project

### Gradle

Local, using the [apt plugin for gradle](https://bitbucket.org/hvisser/android-apt)

```groovy

dependencies {
  apt project(':Libraries:DBFlow:compiler')
  compile project(':Libraries:DBFlow:library')
}

```

Remote, will be available **soon*

```groovy

dependencies {
  apt 'com.github.agrosner:DBFlow-compiler:1.+'
  compile 'com.github.agrosner:DBFlow-library:1.+'
}

```

### Eclipse

No official support as of now, if anyone gets it working in a pull request, send it my way!

## Configuration

First class you need to define is the ```@Database```. It is recommended you store the name and version as static final fields.
The database name is not required for singular databases, however it is good practice to include it here.


```java

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION, foreignKeysSupported = true)
public class AppDatabase {

    public static final String NAME = "App";

    public static final int VERSION = 1;
}

```

