package com.raizlabs.android.dbflow.test;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description:
 */
@Migration(version = 2, database = AppDatabase.class)
public class Migration1 extends AlterTableMigration<AModel> {

    public Migration1(Class<AModel> table) {
        super(table);
    }

}
