# Multi-module

Each compilation that applies the DBFlow plugin generates adapter helpers and `{Name}_Database` for the `@Database` types in that compilation.

Rules:

1. A `@Table` lives in one module and one `@Database`.
2. Consume generated types (`*_Adapter`, `*_Database`) and companion column properties from a compilation that already ran codegen — typically a downstream module or `commonTest`.
3. Open each module’s database with `createDB`. That registers adapters so `select from User::class` works across the process.

`createDB` is additive: opening a second module’s database registers that module’s adapters without replacing the first.

Do not share the same `@Table` class across two databases or two generating modules.
