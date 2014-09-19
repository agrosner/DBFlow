package com.grosner.dbflow.sql.migration;

import android.database.sqlite.SQLiteDatabase;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.structure.Model;

import java.util.ArrayList;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a very nice way to alter a single table quickly and easily.
 */
public class AlterTableMigration<ModelClass extends Model> implements Migration {

    private QueryBuilder mQuery;

    private QueryBuilder mRenameQuery;

    private ArrayList<QueryBuilder> mColumnDefinitions;

    private Class<ModelClass> mTable;

    private String mOldTableName;

    private FlowManager mManager;

    private int mMigrationVersion;

    public AlterTableMigration(FlowManager flowManager, Class<ModelClass> table, int migrationVersion) {
        mManager = flowManager;
        mTable = table;
        mMigrationVersion = migrationVersion;
    }

    public AlterTableMigration(Class<ModelClass> table, int migrationVersion) {
        this(FlowManager.getInstance(), table, migrationVersion);
    }

    @Override
    public int getNewVersion() {
        return mMigrationVersion;
    }

    @Override
    public void onPreMigrate() {
        mQuery = new QueryBuilder().append("ALTER").appendSpaceSeparated("TABLE");
    }

    /**
     * Call this to rename a table to a new name, such as changing either the {@link com.grosner.dbflow.structure.Model} class name
     * or by changing the name through a {@link com.grosner.dbflow.structure.Table}
     * @param oldName The new name to call the table.
     * @return
     */
    public AlterTableMigration renameFrom(String oldName) {
        mOldTableName = oldName;
        mRenameQuery = new QueryBuilder().append(" RENAME").appendSpaceSeparated("TO");
        return this;
    }

    /**
     * Add a column to the DB. This does not necessarily need to be reflected in the {@link ModelClass},
     * but it is recommended.
     * @param columnType
     * @param columnName
     * @return
     */
    public AlterTableMigration addColumn(Class columnType, String columnName) {
        if(mColumnDefinitions == null) {
            mColumnDefinitions = new ArrayList<QueryBuilder>();
        }

        QueryBuilder queryBuilder = new QueryBuilder()
                .appendSpaceSeparated(columnName).appendType(columnType);
        mColumnDefinitions.add(queryBuilder);

        return this;
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        // "ALTER TABLE "
        String sql = mQuery.getQuery();
        String tableName = mManager.getTableName(mTable);

        // "{oldName}  RENAME TO {newName}"
        // Since the structure has been updated already, the manager knows only the new name.
        if(mRenameQuery != null) {
            database.execSQL(sql + mOldTableName +
                    mRenameQuery.getQuery() + tableName);
        }

        // We have column definitions to add here
        // ADD COLUMN columnName {type}
        if(mColumnDefinitions != null) {
            sql = sql + tableName;
            for(QueryBuilder columnDefinition: mColumnDefinitions) {
                database.execSQL(sql + " ADD COLUMN " + columnDefinition.getQuery());
            }
        }
    }

    @Override
    public void onPostMigrate() {

    }
}
