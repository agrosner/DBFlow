package com.grosner.dbflow.sql.migration;

import android.database.sqlite.SQLiteDatabase;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.TableStructure;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a simple way to create a table when migration occurs.
 */
public abstract class CreateTableMigration<ModelClass extends Model> implements Migration {

    private Class<ModelClass> mNewTable;

    private TableStructure<ModelClass> mTableStructure;

    private FlowManager mManager;

    public CreateTableMigration(FlowManager flowManager, Class<ModelClass> newTable) {
        mNewTable = newTable;
        mManager = flowManager;
    }

    @Override
    public void onPreMigrate() {
        // create the structure here
        mTableStructure = new TableStructure<ModelClass>(mManager, mNewTable);
    }

    @Override
    public void migrate(SQLiteDatabase database) {

        // create the table
        database.execSQL(mTableStructure.getCreationQuery().getQuery());
    }

    @Override
    public void onPostMigrate() {
        // Apply it to the table structure
        mManager.getStructure().getTableStructure().put(mNewTable, mTableStructure);
    }
}
