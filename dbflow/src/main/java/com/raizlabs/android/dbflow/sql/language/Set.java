package com.raizlabs.android.dbflow.sql.language;

import android.content.ContentValues;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

/**
 * Description: Used to specify the SET part of an {@link com.raizlabs.android.dbflow.sql.language.Update} query.
 */
public class Set<TModel> extends BaseQueriable<TModel> implements WhereBase<TModel>,
    Queriable, Transformable<TModel> {

    private ConditionGroup conditionGroup;

    private Query update;

    Set(Query update, Class<TModel> table) {
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
    public Set<TModel> conditions(SQLCondition... conditions) {
        conditionGroup.andAll(conditions);
        return this;
    }

    /**
     * Specifies a set of content values to append to this SET as Conditions
     *
     * @param contentValues The set of values to append.
     * @return This instance.
     */
    public Set<TModel> conditionValues(ContentValues contentValues) {
        SqlUtils.addContentValues(contentValues, conditionGroup);
        return this;
    }

    /**
     * Begins completing the rest of this SET statement.
     *
     * @param conditions The conditions to fill the WHERE with.
     * @return The where piece of this query.
     */
    public Where<TModel> where(SQLCondition... conditions) {
        return new Where<>(this, conditions);
    }

    @Override
    public Where<TModel> groupBy(NameAlias... nameAliases) {
        return where().groupBy(nameAliases);
    }

    @Override
    public Where<TModel> groupBy(IProperty... properties) {
        return where().groupBy(properties);
    }

    @Override
    public Where<TModel> orderBy(NameAlias nameAlias, boolean ascending) {
        return where().orderBy(nameAlias, ascending);
    }

    @Override
    public Where<TModel> orderBy(IProperty property, boolean ascending) {
        return where().orderBy(property, ascending);
    }

    @Override
    public Where<TModel> orderBy(OrderBy orderBy) {
        return where().orderBy(orderBy);
    }

    @Override
    public Where<TModel> limit(int count) {
        return where().limit(count);
    }

    @Override
    public Where<TModel> offset(int offset) {
        return where().offset(offset);
    }

    @Override
    public Where<TModel> having(SQLCondition... conditions) {
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
    public long executeUpdateDelete(DatabaseWrapper databaseWrapper) {
        return where().executeUpdateDelete(databaseWrapper);
    }

    @Override
    public long executeUpdateDelete() {
        return where().executeUpdateDelete();
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder =
            new QueryBuilder(update.getQuery())
                .append("SET ")
                .append(conditionGroup.getQuery()).appendSpace();
        return queryBuilder.getQuery();
    }

    /**
     * @return A {@link Transaction.Builder} handle to begin an async transaction.
     * A simple helper method.
     */
    public Transaction.Builder async() {
        return FlowManager.getDatabaseForTable(getTable())
            .beginTransactionAsync(new ITransaction() {
                @Override
                public void execute(DatabaseWrapper databaseWrapper) {
                    Set.this.executeUpdateDelete(databaseWrapper);
                }
            });
    }

    @Override
    public Query getQueryBuilderBase() {
        return update;
    }

    @Override
    public BaseModel.Action getPrimaryAction() {
        return BaseModel.Action.UPDATE;
    }
}
