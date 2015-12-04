# Databases Modules

When you use DBFlow as is, DBFlow assumes the application defines all the databases
it needs. There, however, are scenarios where an application needs to load a module,
or library, that uses DBFlow to manage its databases. This is an important scenario
because it allows you to reuse a database across multiple applications.

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
can easily be done by providing an initialization method for the module that the application
must invoke before it can be used. Otherwise, the application must be aware that is needs
to manually instruct DBFlow to load a database module.
