# Multi-module

Each module that applies the DBFlow plugin generates adapter helpers and `{Name}_Database` for the `@Database` types in that module. Generation runs in that module's `dbflowGenerate` task, and the generated code compiles into the module's own main artifacts.

Rules:

1. A `@Table` lives in one module and one `@Database`.
2. The defining module's main code uses `createDB` and companion column properties; reference generated types (`*_Adapter`, `*_Database`) directly from its tests or from downstream modules.
3. Open each module’s database with `createDB`. That registers adapters so `select from User::class` works across the process.

`createDB` is additive: opening a second module’s database registers that module’s adapters without replacing the first.

Do not share the same `@Table` class across two databases or two generating modules.
