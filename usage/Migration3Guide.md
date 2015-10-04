# DBFlow 3.0 Migration Guide
DBFlow has undergone the most _significant_ changes in its lifetime in 3.0. This guide is meant to assist you in migrating from 2.1.x and above and may not be fully inclusive of all changes. This doc will mention the most glaring and significant changes.

## Table Of Contents
1. Database + Table Structure
2. Properties, Conditions, Queries and more
3. ModelContainers
4. ModelViews
5. Indexes

## Database + Table Structure
### Database changes
The default `generatedClassSeparator` is now `_` instead of `$` to play nice with Kotlin by default. A simple addition of:

```java

@Database(generatedClassSeparator = "$")
```

will restore your generated "Table" and more classes to normalcy.

Globally, we no longer reference what `@Database` any database-specific element (Table, Migration, etc) by `String` name, but by `Class` now.

Before:

```java

@Table(databaseName = AppDatabase.NAME)
@Migration(databaseName = AppDatabase.NAME)
```

After:

```java

@Table(database = AppDatabase.class)
@Migration(database = AppDatabase.class)
```

Why: We decided that referencing it directly by class name enforces type-safety and direct enforcement of the database placeholder class. Previously,

```java

@Table(databaseName = "AppDatabase")
```

was a valid specifier.
