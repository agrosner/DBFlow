# Multiple Modules

In apps that want to share DBFlow across multiple modules or when developing a library module that uses DBFlow, we have to provide a little extra configuration to properly ensure that all database classes are accounted for.

It's directly related to the fact that annotation processors are isolated between projects and are not shared.

In order to add support for multiple modules, in each and every library/subproject that uses a DBFlow instance, you must add an annotation processing argument to its `build.gradle`:

Using KAPT:

```java
kapt {
    arguments {
        arg("targetModuleName", "SomeUniqueModuleName")
    }
}
```

or if you use Android/Java:

```java
// inside android -> defaultConfig
javaCompileOptions {
      annotationProcessorOptions {
        arguments = ['library': 'true']
      }
    }
```

By passing the targetModuleName, we append that to the `GeneratedDatabaseHolder` class name to create the `{targetModuleName}GeneratedDatabaseHolder` module. 

**Note**: Specifying this in code means you need to specify the module when initializing DBFlow:

From previous sample code, we recommend initializing the specific module inside your library, to prevent developer error. **Note**: Multiple calls to `FlowManager` will not adversely affect DBFlow. If DBFlow is already initialized, we append the module to DBFlow if and only if it does not already exist.

```kotlin
fun initialize(context: Context) {
  FlowManager.init(FlowConfig.builder(context)
    .addDatabaseHolder(SomeUniqueModuleNameGeneratedDatabaseHolder::class)
    .build())
}
```

