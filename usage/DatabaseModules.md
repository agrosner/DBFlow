# Database Modules

When you use DBFlow as is, DBFlow assumes the application defines all the databases
it needs. There, however, are scenarios where an application needs to load a module,
or library, that uses DBFlow to manage its databases. This is an important scenario
because it allows you to reuse a database across multiple applications. Unfortunately,
if you try this with DBFlow, then there will be duplicate symbols in the application and
the module and the application will not build.

To get around this problem, you must enable database module support for the module
intended to be loaded by an application. Fortunately, this is a very easy process.

To add databases to a module, first update the ```build.config``` with the ```apt```
section. In the example below, all databases will be in the ```Test``` module.

```groovy
apt {
    arguments {
        targetModuleName 'Test'
    }
}
```

Initialize DBFlow using the standard approach. For example, you can initialize
DBFlow in the ```Application``` class:

```java
public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }
}
```

Lastly, instruct DBFlow to load the module that contains the database.

```java
FlowManager.initModule("Test");
```

Ideally, the module containing the databases should execute the line of code above. This
can easily be done by exporting an initialization method from the module that the application
must invoke before it can be used, similar to DBFlow. For example:

```java
public class Test {
    public static void initialize(Context context) {
        FlowManager.initModule("Test");

        // Perform other initialization steps
    }
}
```

Otherwise, the application must be aware that is needs to manually instruct DBFlow to load
a database module. Lastly, ```FlowManager.initModule(moduleName)``` can be invoked multiple
times without causing any additional side-effects after the first invocation.
