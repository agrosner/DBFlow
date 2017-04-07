package com.grosner.dbflow.jack.tests;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;

/**
 * Description:
 */
@Migration(version = 2, database = AppDatabase.class)
public class Migration1 extends AlterTableMigration<AModel> {

    public Migration1(Class<AModel> table) {
        super(table);
    }

    @Override
    public void onPreMigrate() {
        addColumn(SQLiteType.TEXT, "myColumn");
        addColumn(SQLiteType.REAL, "anotherColumn");
    }
}
