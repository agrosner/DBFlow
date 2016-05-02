package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: The INDEXED BY part of a SELECT/UPDATE/DELETE
 */
public class IndexedBy<TModel extends Model> implements WhereBase<TModel>, Transformable<TModel> {

    private final IndexProperty<TModel> indexProperty;

    private final WhereBase<TModel> whereBase;

    /**
     * Creates the INDEXED BY part of the clause.
     *
     * @param indexProperty The index property generated.
     * @param whereBase     The base piece of this query
     */
    IndexedBy(IndexProperty<TModel> indexProperty, WhereBase<TModel> whereBase) {
        this.indexProperty = indexProperty;
        this.whereBase = whereBase;
    }

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

    @Override
    public Class<TModel> getTable() {
        return whereBase.getTable();
    }

    @Override
    public Query getQueryBuilderBase() {
        return whereBase.getQueryBuilderBase();
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(whereBase.getQuery())
                .append("INDEXED BY ").append(QueryBuilder.quoteIfNeeded(indexProperty.getIndexName())).appendSpace();
        return queryBuilder.getQuery();
    }

}
