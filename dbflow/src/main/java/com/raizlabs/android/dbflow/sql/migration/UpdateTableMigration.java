package com.raizlabs.android.dbflow.sql.migration;

import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Provides a simple way to update a table's field or fields quickly in a migration.
 * It ties an SQLite {@link com.raizlabs.android.dbflow.sql.language.Update}
 * to migrations whenever we want to batch update tables in a structured manner.
 */
public class UpdateTableMigration<ModelClass extends Model> extends BaseMigration implements Query {

    /**
     * The table to update
     */
    private final Class<ModelClass> table;

    /**
     * The query to use
     */
    private QueryBuilder query;

    /**
     * Builds the conditions for the WHERE part of our query
     */
    private ConditionQueryBuilder<ModelClass> whereConditionQueryBuilder;

    /**
     * The conditions to use to set fields in the update query
     */
    private ConditionQueryBuilder<ModelClass> setConditionQueryBuilder;

    /**
     * Creates an update migration.
     *
     * @param table The table to update
     */
    public UpdateTableMigration(Class<ModelClass> table) {
        this.table = table;
    }

    /**
     * This will append a condition to this migration. It will execute each of these in succession with the order
     * that this is called.
     *
     * @param conditions The conditions to append
     */
    public UpdateTableMigration<ModelClass> set(SQLCondition... conditions) {
        if (setConditionQueryBuilder == null) {
            setConditionQueryBuilder = new ConditionQueryBuilder<>(table);
        }

        setConditionQueryBuilder.addConditions(conditions);
        return this;
    }

    public UpdateTableMigration<ModelClass> where(SQLCondition... conditions) {
        if (whereConditionQueryBuilder == null) {
            whereConditionQueryBuilder = new ConditionQueryBuilder<>(table);
        }

        whereConditionQueryBuilder.addConditions(conditions);
        return this;
    }

    private String generateQuery() {
        query = new QueryBuilder(new Update<>(table)
                .set(setConditionQueryBuilder)
                .where(whereConditionQueryBuilder).getQuery());
        return query.getQuery();
    }

    @Override
    public final void migrate(SQLiteDatabase database) {
        database.execSQL(generateQuery());
    }

    @Override
    public void onPostMigrate() {
        // make fields eligible for GC
        query = null;
        setConditionQueryBuilder = null;
        whereConditionQueryBuilder = null;
    }

    @Override
    public String getQuery() {
        return generateQuery();
    }
}
