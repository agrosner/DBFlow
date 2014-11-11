![Image](https://github.com/agrosner/DBFlow/blob/master/clear-river.jpg?raw=true)


DBFlow (BETA)
======

A robust, powerful, and very simple ORM android database library.

The library eliminates the need for writing most SQL statements, writing ``ContentValues`` for every table, converting cursors into models, and so much more. 

Let DBFlow make SQL code _flow_ like a _steady_ stream so you can focus on your complex problem and not be hindered by repetitive code writing. 

This library is based on [Active Android](https://github.com/pardom/ActiveAndroid), [Schematic](https://github.com/SimonVT/schematic), [Ollie](https://github.com/pardom/ollie/), and [Sprinkles](https://github.com/emilsjolander/sprinkles), but takes the **best** of each while offering much more functionality and extensibility. 

## Getting Started

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


