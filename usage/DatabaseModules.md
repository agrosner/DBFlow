# Database Modules
DBFlow at it's base will generate a `GeneratedDatabaseHolder` class, which contains all of the databases, tables, more defined for everything your application will need to reference when interacting with the library.

However, there are scenarios where an application has a library or subproject that also uses DBFlow to manage its databases. This is an important scenario because it allows you to reuse a database across multiple applications. Previously, DBFlow did not support this use-case and would fail when attempting to do so.

To get around this problem, you must enable database module support for the module intended to be loaded by an application. Fortunately, this is a very easy process.

To add databases to a module, first update your `build.gradle` of the library to define a custom `apt` argument that will place the `GeneratedDatabaseHolder` class-like definition in a different class file (in same package) so that the classes will not get duplicated.  After you have called "apply plugin: 'com.neenbedankt.android-apt'" but before your depencies, add this:

```groovy
apt {
    arguments {
        targetModuleName 'Test'
    }
}
```

By passing the `targetModuleName`, we append that to the `GeneratedDatabaseHolder` to create the `GeneratedDatabaseHolderTest` module.

In your library (and application) you should initialize DBFlow using the standard approach.

```java
public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }
}
```

Lastly, instruct DBFlow to load the module that contains the database (you may need to build the app to generate the class file to be able to reference it).

```java
FlowManager.initModule(GeneratedDatabaseHolderTest.class);
```

This method can be invoked multiple times without any effect on the state of the application because it keeps a mapping of the ones already loaded.
