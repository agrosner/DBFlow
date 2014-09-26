package com.grosner.dbflow.sql.migration;

import android.database.sqlite.SQLiteDatabase;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.structure.Model;

import java.util.ArrayList;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a simple way to update a table's field or fields quickly.
 */
public class UpdateTableMigration<ModelClass extends Model> extends BaseMigration {

    private QueryBuilder mQuery;

    private ConditionQueryBuilder<ModelClass> mConditionQueryBuilder;

    private FlowManager mManager;

    private final Class<ModelClass> mTable;

    private ArrayList<QueryBuilder> mSetDefinitions;

    public UpdateTableMigration(FlowManager flowManager, Class<ModelClass> table, int migrationVersion) {
        super(migrationVersion);
        mManager = flowManager;
        mTable = table;
    }

    public UpdateTableMigration(Class<ModelClass> table, int migrationVersion) {
        this(FlowManager.getInstance(), table, migrationVersion);
    }

    /**
     * This will append a SET columnName = value to this migration. It will execute each of these in succession with the order
     * that this is called.
     *
     * @param columnName
     * @param value
     * @return
     */
    public UpdateTableMigration<ModelClass> set(String columnName, String value) {
        if (mSetDefinitions == null) {
            mSetDefinitions = new ArrayList<QueryBuilder>();
        }

        QueryBuilder queryBuilder = new QueryBuilder()
                .append(columnName).appendSpaceSeparated("=").append(value);
        mSetDefinitions.add(queryBuilder);

        return this;
    }

    public UpdateTableMigration<ModelClass> where(Condition condition) {
        if (mConditionQueryBuilder == null) {
            mConditionQueryBuilder = new ConditionQueryBuilder<ModelClass>(mManager, mTable);
        }

        mConditionQueryBuilder.param(condition);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPreMigrate() {
        mQuery = new QueryBuilder().append("UPDATE").appendSpaceSeparated(mManager.getTableName(mTable))
                .append("SET").appendSpace().appendList(mSetDefinitions);

        if (mConditionQueryBuilder != null) {
            mQuery.appendSpaceSeparated("WHERE").append(mConditionQueryBuilder.getQuery());
        }
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        database.execSQL(mQuery.getQuery());
    }

    @Override
    public void onPostMigrate() {
        // make fields eligible for GC
        mQuery = null;
        mSetDefinitions = null;
        mManager = null;
    }
}
