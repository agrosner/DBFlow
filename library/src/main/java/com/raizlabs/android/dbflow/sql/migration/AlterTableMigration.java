package com.raizlabs.android.dbflow.sql.migration;

import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Provides a very nice way to alter a single table quickly and easily.
 */
public class AlterTableMigration<ModelClass extends Model> extends BaseMigration {

    /**
     * The table to ALTER
     */
    private final Class<ModelClass> mTable;

    /**
     * The query we use
     */
    private QueryBuilder mQuery;

    /**
     * The query to rename the table with
     */
    private QueryBuilder mRenameQuery;

    /**
     * The columns to ALTER within a table
     */
    private ArrayList<QueryBuilder> mColumnDefinitions;

    /**
     * The old name of the table before renaming it
     */
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
            String renameQuery = new QueryBuilder(sql).appendQuoted(mOldTableName)
                    .append(mRenameQuery.getQuery())
                    .appendQuoted(tableName)
                    .toString();
            database.execSQL(renameQuery);
        }

        // We have column definitions to add here
        // ADD COLUMN columnName {type}
        if (mColumnDefinitions != null) {
            sql = new QueryBuilder(sql).appendQuoted(tableName).toString();
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
     * Call this to rename a table to a new name, such as changing either the {@link com.raizlabs.android.dbflow.structure.Model} class name
     * or by changing the name through a {@link com.raizlabs.android.dbflow.structure.Table}
     *
     * @param oldName The new name to call the table.
     * @return This instance
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
     * @param columnType The type of column that pertains to an {@link com.raizlabs.android.dbflow.sql.SQLiteType}
     * @param columnName The name of the column to add. Use the "$Table" class for the specified table.
     * @return This instance
     */
    public AlterTableMigration<ModelClass> addColumn(Class columnType, String columnName) {
        if (mColumnDefinitions == null) {
            mColumnDefinitions = new ArrayList<>();
        }

        QueryBuilder queryBuilder = new QueryBuilder()
                .appendQuoted(columnName).appendSpace().appendType(columnType.getName());
        mColumnDefinitions.add(queryBuilder);

        return this;
    }

    /**
     * @return The query that renames the table.
     */
    public String getRenameQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(mQuery.getQuery()).appendQuoted(mOldTableName)
                .append(mRenameQuery).appendQuoted(FlowManager.getTableName(mTable));
        return queryBuilder.getQuery();
    }

    /**
     * @return A List of column definitions that add column to a table in the DB.
     */
    public List<String> getColumnDefinitions() {
        String sql = new QueryBuilder(mQuery.getQuery()).appendQuoted(FlowManager.getTableName(mTable)).toString();
        List<String> columnDefinitions = new ArrayList<>();

        if (mColumnDefinitions != null) {
            for (QueryBuilder columnDefinition : mColumnDefinitions) {
                QueryBuilder queryBuilder = new QueryBuilder(sql).appendSpaceSeparated("ADD COLUMN").append(columnDefinition.getQuery());
                columnDefinitions.add(queryBuilder.getQuery());
            }
        }

        return columnDefinitions;
    }
}
