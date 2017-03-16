package com.raizlabs.android.dbflow.sql.language;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.List;

/**
 * Description: Used to specify the SET part of an {@link com.raizlabs.android.dbflow.sql.language.Update} query.
 */
public class Set<TModel> extends BaseQueriable<TModel> implements WhereBase<TModel> {

    private OperatorGroup operatorGroup;

    private Query update;

    public Set(Query update, Class<TModel> table) {
        super(table);
        this.update = update;
        operatorGroup = new OperatorGroup();
        operatorGroup.setAllCommaSeparated(true);
    }

    /**
     * Specifies a varg of conditions to append to this SET
     *
     * @param conditions The varg of conditions
     * @return This instance.
     */
    @NonNull
    public Set<TModel> conditions(SQLOperator... conditions) {
        operatorGroup.andAll(conditions);
        return this;
    }

    @NonNull
    public Set<TModel> conditionValues(ContentValues contentValues) {
        SqlUtils.addContentValues(contentValues, operatorGroup);
        return this;
    }

    @NonNull
    public Where<TModel> where(SQLOperator... conditions) {
        return new Where<>(this, conditions);
    }

    @NonNull
    public Where<TModel> groupBy(NameAlias... nameAliases) {
        return where().groupBy(nameAliases);
    }

    @NonNull
    public Where<TModel> groupBy(IProperty... properties) {
        return where().groupBy(properties);
    }

    @NonNull
    public Where<TModel> orderBy(NameAlias nameAlias, boolean ascending) {
        return where().orderBy(nameAlias, ascending);
    }

    @NonNull
    public Where<TModel> orderBy(IProperty property, boolean ascending) {
        return where().orderBy(property, ascending);
    }

    @NonNull
    public Where<TModel> orderBy(OrderBy orderBy) {
        return where().orderBy(orderBy);
    }

    @NonNull
    public Where<TModel> orderByAll(List<OrderBy> orderBies) {
        return where().orderByAll(orderBies);
    }

    @NonNull
    public Where<TModel> limit(int count) {
        return where().limit(count);
    }

    @NonNull
    public Where<TModel> offset(int offset) {
        return where().offset(offset);
    }

    @NonNull
    public Where<TModel> having(SQLOperator... conditions) {
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
                        .append(operatorGroup.getQuery()).appendSpace();
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
