package com.raizlabs.android.dbflow.test.sql;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.sql.migration.IndexMigration;
import com.raizlabs.android.dbflow.sql.migration.IndexPropertyMigration;
import com.raizlabs.android.dbflow.sql.migration.UpdateTableMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.test.sql.index.IndexModel_Table;

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

    @Migration(version = 2, priority = 1, database = MigrationDatabase.class)
    public static class IndexPropertyMigration2 extends IndexPropertyMigration {

        @NonNull
        @Override
        public IndexProperty getIndexProperty() {
            return IndexModel_Table.index_customIndex;
        }
    }

    @Migration(version = 2, priority = 2, database = MigrationDatabase.class)
    public static class UpdateMigration2 extends UpdateTableMigration<MigrationModel> {

        /**
         * Creates an update migration.
         *
         * @param table The table to update
         */
        public UpdateMigration2(Class<MigrationModel> table) {
            super(table);
            set(MigrationModel_Table.name.eq("New Name"));
        }

    }
}
