package com.raizlabs.android.dbflow.test.sql;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.sql.migration.IndexMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description:
 */
@Database(version = 2, name = MigrationDatabase.NAME)
public class MigrationDatabase {

    public static final String NAME = "Migrations";

    @Migration(version = 2, database = MigrationDatabase.class)
    public static class Migration2 extends BaseMigration {

        @Override
        public void migrate(DatabaseWrapper database) {

        }
    }

    @Migration(version = 2, priority = 0, database = MigrationDatabase.class)
    public static class IndexMigration2 extends IndexMigration<MigrationModel> {

        public IndexMigration2(@NonNull Class<MigrationModel> onTable) {
            super(onTable);
        }

        @NonNull
        @Override
        public String getName() {
            return "TestIndex";
        }
    }
}
