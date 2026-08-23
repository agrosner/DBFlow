# Multi-module

Each compilation that applies the DBFlow plugin generates its own `GeneratedDatabaseHolderFactory` and adapters.

Rules:

1. A `@Table` lives in one module and one `@Database`.
2. Consume generated types (`*_Table`, `*_Database`) from a compilation that already ran codegen — typically a downstream module or `commonTest`.
3. If you look up adapters by `KClass`, load that module’s holder:

```kotlin
DatabaseObjectLookup.loadHolder(GeneratedDatabaseHolderFactory)
```

`loadHolder` is additive. Call it once per generated factory. Creating a database with `{Name}_Database.create` uses that module’s factory and does not replace another module’s holder.

Do not share the same `@Table` class across two databases or two generating modules.
