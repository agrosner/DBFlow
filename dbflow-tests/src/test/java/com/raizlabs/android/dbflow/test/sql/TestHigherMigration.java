package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.migration.Migration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@com.raizlabs.android.dbflow.annotation.Migration(version = 1, database = TestDatabase.class, priority = 2)
public class TestHigherMigration implements Migration {
    @Override
    public void onPreMigrate() {

    }

    @Override
    public void migrate(DatabaseWrapper database) {

    }

    @Override
    public void onPostMigrate() {

    }
}
