package com.raizlabs.android.dbflow.sql.migration;

import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a simple way to update a table's field or fields quickly in a migration. It ties an SQLite {@link com.raizlabs.android.dbflow.sql.language.Update}
 * to migrations whenever we want to batch update tables in a structured manner.
 */
public class UpdateTableMigration<ModelClass extends Model> extends BaseMigration implements Query {

    /**
     * The table to update
     */
    private final Class<ModelClass> mTable;
    /**
     * The query to use
     */
    private QueryBuilder mQuery;
    /**
     * Builds the conditions for the WHERE part of our query
     */
    private ConditionQueryBuilder<ModelClass> mWhereConditionQueryBuilder;
    /**
     * The conditions to use to set fields in the update query
     */
    private ConditionQueryBuilder<ModelClass> mSetConditionQueryBuilder;

    /**
     * Creates an update migration.
     *
     * @param table            The table to update
     * @param migrationVersion The version of the db to update at.
     */
    public UpdateTableMigration(Class<ModelClass> table) {
        mTable = table;
    }

    /**
     * This will append a SET columnName = value to this migration. It will execute each of these in succession with the order
     * that this is called.
     *
     * @param conditions The condition to append
     * @return
     */
    public UpdateTableMigration<ModelClass> set(Condition... conditions) {
        if (mSetConditionQueryBuilder == null) {
            mSetConditionQueryBuilder = new ConditionQueryBuilder<ModelClass>(mTable);
        }

        mSetConditionQueryBuilder.putConditions(conditions);
        return this;
    }

    public UpdateTableMigration<ModelClass> where(Condition... conditions) {
        if (mWhereConditionQueryBuilder == null) {
            mWhereConditionQueryBuilder = new ConditionQueryBuilder<ModelClass>(mTable);
        }

        mWhereConditionQueryBuilder.putConditions(conditions);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPreMigrate() {
        mQuery = new QueryBuilder(new Update().table(mTable).set(mSetConditionQueryBuilder).where(mWhereConditionQueryBuilder).getQuery());
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        database.execSQL(mQuery.getQuery());
    }

    @Override
    public void onPostMigrate() {
        // make fields eligible for GC
        mQuery = null;
        mSetConditionQueryBuilder = null;
    }

    @Override
    public String getQuery() {
        return mQuery.getQuery();
    }
}
