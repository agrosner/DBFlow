package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
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
}
