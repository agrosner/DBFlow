package com.grosner.dbflow.sql.migration;

import android.database.sqlite.SQLiteDatabase;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a very nice way to alter a single table quickly and easily.
 */
public class AlterTableMigration<ModelClass extends Model> extends BaseMigration {

    private final Class<ModelClass> mTable;
    private QueryBuilder mQuery;
    private QueryBuilder mRenameQuery;
    private ArrayList<QueryBuilder> mColumnDefinitions;
    private String mOldTableName;

    public AlterTableMigration(Class<ModelClass> table) {
        mTable = table;
    }

    @Override
    public void onPreMigrate() {
        mQuery = new QueryBuilder().append("ALTER").appendSpaceSeparated("TABLE");
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        // "ALTER TABLE "
        String sql = mQuery.getQuery();
        String tableName = FlowManager.getTableName(mTable);

        // "{oldName}  RENAME TO {newName}"
        // Since the structure has been updated already, the manager knows only the new name.
        if (mRenameQuery != null) {
            database.execSQL(sql + mOldTableName +
                    mRenameQuery.getQuery() + tableName);
        }

        // We have column definitions to add here
        // ADD COLUMN columnName {type}
        if (mColumnDefinitions != null) {
            sql = sql + tableName;
            for (QueryBuilder columnDefinition : mColumnDefinitions) {
                database.execSQL(sql + " ADD COLUMN " + columnDefinition.getQuery());
            }
        }
    }

    @Override
    public void onPostMigrate() {
        // cleanup and make fields eligible for garbage collection
        mQuery = null;
        mRenameQuery = null;
        mColumnDefinitions = null;
    }

    /**
     * Call this to rename a table to a new name, such as changing either the {@link com.grosner.dbflow.structure.Model} class name
     * or by changing the name through a {@link com.grosner.dbflow.structure.Table}
     *
     * @param oldName The new name to call the table.
     * @return
     */
    public AlterTableMigration<ModelClass> renameFrom(String oldName) {
        mOldTableName = oldName;
        mRenameQuery = new QueryBuilder().append(" RENAME").appendSpaceSeparated("TO");
        return this;
    }

    /**
     * Add a column to the DB. This does not necessarily need to be reflected in the {@link ModelClass},
     * but it is recommended.
     *
     * @param columnType
     * @param columnName
     * @return
     */
    public AlterTableMigration<ModelClass> addColumn(Class columnType, String columnName) {
        if (mColumnDefinitions == null) {
            mColumnDefinitions = new ArrayList<QueryBuilder>();
        }

        QueryBuilder queryBuilder = new QueryBuilder()
                .append(columnName).appendSpace().appendType(columnType.getName());
        mColumnDefinitions.add(queryBuilder);

        return this;
    }

    public String getRenameQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(mQuery.getQuery()).append(mOldTableName)
                .append(mRenameQuery).append(FlowManager.getTableName(mTable));
        return queryBuilder.getQuery();
    }

    public List<String> getColumnDefinitions() {
        String sql = mQuery.getQuery() + FlowManager.getTableName(mTable);
        List<String> columnDefinitions = new ArrayList<String>();

        if (mColumnDefinitions != null) {
            for (QueryBuilder columnDefinition : mColumnDefinitions) {
                QueryBuilder queryBuilder = new QueryBuilder(sql).appendSpaceSeparated("ADD COLUMN").append(columnDefinition.getQuery());
                columnDefinitions.add(queryBuilder.getQuery());
            }
        }

        return columnDefinitions;
    }
}
