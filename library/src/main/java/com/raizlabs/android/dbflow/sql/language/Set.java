package com.raizlabs.android.dbflow.sql.language;

import android.content.ContentValues;
import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Queriable;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Map;

/**
 * Description: Used to specify the SET part of an {@link com.raizlabs.android.dbflow.sql.language.Update} query.
 */
public class Set<ModelClass extends Model> implements WhereBase<ModelClass>, Queriable {

    private ConditionQueryBuilder<ModelClass> mConditionQueryBuilder;

    private Query mUpdate;

    Set(Query update, Class<ModelClass> table) {
        mUpdate = update;
        mConditionQueryBuilder = new ConditionQueryBuilder<ModelClass>(table).setSeparator(",");
    }

    /**
     * Specifies a condition clause for this SET.
     * @param setClause
     * @param args
     * @return
     */
    public Set<ModelClass> conditionClause(String setClause, Object... args) {
        mConditionQueryBuilder.append(setClause, args);
        return this;
    }

    /**
     * Specifies the condition query for this SET.
     *
     * @param conditionQueryBuilder The condition query to use
     * @return This instance.
     */
    public Set<ModelClass> conditionQuery(ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        mConditionQueryBuilder = conditionQueryBuilder;
        return this;
    }

    /**
     * Specifies a varg of conditions to append to this SET
     *
     * @param conditions The varg of conditions
     * @return This instance.
     */
    public Set<ModelClass> conditions(Condition... conditions) {
        mConditionQueryBuilder.putConditions(conditions);
        return this;
    }

    /**
     * Specifies a set of content values to append to this SET as Conditions
     * @param contentValues The set of values to append.
     * @return This instance.
     */
    public Set<ModelClass> conditionValues(ContentValues contentValues) {
        for(Map.Entry<String, Object> entry : contentValues.valueSet()) {
            String key = entry.getKey();
            mConditionQueryBuilder.putCondition(Condition.column(key).is(contentValues.get(key)));
        }
        return this;
    }

    /**
     * Begins completing the rest of this UPDATE statement.
     *
     * @param whereClause The whereclause to append
     * @param args        The argument bindings for the whereClause.
     * @return
     */
    public Where<ModelClass> where(String whereClause, Object... args) {
        return where().whereClause(whereClause, args);
    }

    /**
     * Begins completing the rest of this UPDATE statement.
     *
     * @param conditions The varg of conditions for the WHERE part
     * @return The where piece of this query.
     */
    public Where<ModelClass> where(Condition... conditions) {
        return where().andThese(conditions);
    }

    /**
     * Begins completing the rest of this UPDATE statement.
     *
     * @return The where piece of this query.
     */
    public Where<ModelClass> where() {
        return new Where<ModelClass>(this);
    }

    /**
     * Begins completing the rest of this UPDATE statement.
     *
     * @param whereConditionBuilder The where condition querybuilder to use
     * @return The where piece of this query.
     */
    public Where<ModelClass> where(ConditionQueryBuilder<ModelClass> whereConditionBuilder) {
        return where().whereQuery(whereConditionBuilder);
    }

    /**
     * Executes a SQL statement that retrieves the count of results in the DB.
     *
     * @return The number of rows this query returns
     */
    public long count() {
        return where().count();
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder =
                new QueryBuilder(mUpdate.getQuery())
                        .append("SET ")
                        .append(mConditionQueryBuilder.getQuery()).appendSpace();
        return queryBuilder.getQuery();
    }

    @Override
    public Class<ModelClass> getTable() {
        return mConditionQueryBuilder.getTableClass();
    }

    @Override
    public Query getQueryBuilderBase() {
        return mUpdate;
    }

    @Override
    public Cursor query() {
        FlowManager.getDatabaseForTable(mConditionQueryBuilder.getTableClass()).getWritableDatabase().execSQL(getQuery());
        return null;
    }

    @Override
    public void queryClose() {
        query();
    }
}
