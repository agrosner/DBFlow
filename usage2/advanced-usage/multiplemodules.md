# MultipleModules

In apps that want to share DBFlow across multiple modules or when developing a library module that uses DBFlow, we have to provide a little extra configuration to properly ensure that all database classes are accounted for.

It's directly related to the fact that annotation processors are isolated between projects and are not shared.

In order to add support for multiple modules, in each and every library/subproject that uses a DBFlow instance, you must add an APT argument \(using the [android-apt plugin](https://bitbucket.org/hvisser/android-apt)\) to its `build.gradle`:

```java
apt {
    arguments {
        targetModuleName 'SomeUniqueModuleName'
    }
}
```

or for if you use Kotlin, KAPT:

```java
kapt {
    generateStubs = true
    arguments {
        arg("targetModuleName", "SomeUniqueModuleName")
    }
}
```

By passing the targetModuleName, we append that to the `GeneratedDatabaseHolder` class name to create the `{targetModuleName}GeneratedDatabaseHolder` module. **Note**: Specifying this in code means you need to specify the module when initializing DBFlow:

From previous sample code, we recommend initializing the specific module inside your library, to prevent developer error. **Note**: Multiple calls to `FlowManager` will not adversely affect DBFlow. If DBFlow is already initialized, we append the module to DBFlow if and only if it does not already exist.

```kotlin
fun initialize(context: Context) {
  FlowManager.init(FlowConfig.builder(context)
    .addDatabaseHolder(SomeUniqueModuleNameGeneratedDatabaseHolder::class)
    .build())
}
```
