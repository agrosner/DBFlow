# Migrations

Bump `@Database(version = …)` and register migration classes. They run on open when the on-disk version is lower.

```kotlin
@Database(
    version = 2,
    tables = [User::class],
    migrations = [AddEmailMigration::class],
)
abstract class AppDatabase : DBFlowDatabase<AppDatabase>()

@Migration(version = 2, priority = 1)
class AddEmailMigration : Migration {
    override suspend fun MigrationScope.migrate() {
        if (!(migrationAdapter("User") hasColumn "email")) {
            (alterTable("User") addColumn ColumnAlter.Plain(
                name = "email",
                type = SQLiteType.TEXT,
            )).execute()
        }
    }
}
```

`priority` is lowest-first when several migrations share a version.

SQLite can `ALTER TABLE … RENAME` and `ADD COLUMN`. For anything else, create a new table, copy, drop, rename.

## Alter helpers

```kotlin
alterTable("User") addColumn ColumnAlter.Plain("email", SQLiteType.TEXT)
alterTable("User") dropColumn "legacy"
alterTable("User") renameTo "Person"
```

`MigrationScope` is the only safe API during upgrade. Do not open the same database recursively from a migration.

## SQL files

Place statements in:

```text
assets/migrations/{DatabaseName}/{version}.sql
```

Example: `assets/migrations/AppDatabase/2.sql`

Each statement ends with `;`. `--` comments must be on their own line.

## Version 0

`@Migration(version = 0)` runs only when the database is created, not on later upgrades.

## Prepackaged files

Ship a starter `.db` in assets and point the open helper at it (see tests under `PrepackagedDB`). Run migrations after copy if `version` is higher than the packaged file.
