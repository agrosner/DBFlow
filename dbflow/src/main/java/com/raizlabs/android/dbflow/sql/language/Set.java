package com.raizlabs.android.dbflow.sql.language;

import android.content.ContentValues;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Used to specify the SET part of an {@link com.raizlabs.android.dbflow.sql.language.Update} query.
 */
public class Set<ModelClass extends Model> extends BaseQueriable<ModelClass> implements WhereBase<ModelClass>, Queriable, Transformable<ModelClass> {

    private ConditionGroup conditionGroup;

    private Query update;

    Set(Query update, Class<ModelClass> table) {
        super(table);
        this.update = update;
        conditionGroup = new ConditionGroup();
        conditionGroup.setAllCommaSeparated(true);
    }

    /**
     * Specifies a varg of conditions to append to this SET
     *
     * @param conditions The varg of conditions
     * @return This instance.
     */
    public Set<ModelClass> conditions(SQLCondition... conditions) {
        conditionGroup.andAll(conditions);
        return this;
    }

    /**
     * Specifies a set of content values to append to this SET as Conditions
     *
     * @param contentValues The set of values to append.
     * @return This instance.
     */
    public Set<ModelClass> conditionValues(ContentValues contentValues) {
        SqlUtils.addContentValues(contentValues, conditionGroup);
        return this;
    }

    /**
     * Begins completing the rest of this SET statement.
     *
     * @param conditions The conditions to fill the WHERE with.
     * @return The where piece of this query.
     */
    public Where<ModelClass> where(SQLCondition... conditions) {
        return new Where<>(this, conditions);
    }

    @Override
    public Where<ModelClass> groupBy(NameAlias... nameAliases) {
        return where().groupBy(nameAliases);
    }

    @Override
    public Where<ModelClass> groupBy(IProperty... properties) {
        return where().groupBy(properties);
    }

    @Override
    public Where<ModelClass> orderBy(NameAlias nameAlias, boolean ascending) {
        return where().orderBy(nameAlias, ascending);
    }

    @Override
    public Where<ModelClass> orderBy(IProperty property, boolean ascending) {
        return where().orderBy(property, ascending);
    }

    @Override
    public Where<ModelClass> orderBy(OrderBy orderBy) {
        return where().orderBy(orderBy);
    }

    @Override
    public Where<ModelClass> limit(int count) {
        return where().limit(count);
    }

    @Override
    public Where<ModelClass> offset(int offset) {
        return where().offset(offset);
    }

    @Override
    public Where<ModelClass> having(SQLCondition... conditions) {
        return where().having(conditions);
    }

    /**
     * Executes a SQL statement that retrieves the count of results in the DB.
     *
     * @return The number of rows this query returns
     */
    @Override
    public long count() {
        return where().count();
    }

    @Override
    public long count(DatabaseWrapper databaseWrapper) {
        return where().count(databaseWrapper);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder =
                new QueryBuilder(update.getQuery())
                        .append("SET ")
                        .append(conditionGroup.getQuery()).appendSpace();
        return queryBuilder.getQuery();
    }

    @Override
    public Query getQueryBuilderBase() {
        return update;
    }

}
