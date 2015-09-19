package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: The INDEXED BY part of a SELECT/UPDATE/DELETE
 */
public class IndexedBy<ModelClass extends Model> implements WhereBase<ModelClass>, Transformable<ModelClass> {

    private final IndexProperty<ModelClass> indexProperty;

    private final WhereBase<ModelClass> whereBase;

    /**
     * Creates the INDEXED BY part of the clause.
     *
     * @param indexProperty The index property generated.
     * @param whereBase     The base piece of this query
     */
    IndexedBy(IndexProperty<ModelClass> indexProperty, WhereBase<ModelClass> whereBase) {
        this.indexProperty = indexProperty;
        this.whereBase = whereBase;
    }

    public Where<ModelClass> where(SQLCondition... conditions) {
        return new Where<>(this, conditions);
    }

    @Override
    public Where<ModelClass> groupBy(NameAlias... nameAliases) {
        return where().groupBy(nameAliases);
    }

    @Override
    public Where<ModelClass> groupBy(Property... properties) {
        return where().groupBy(properties);
    }

    @Override
    public Where<ModelClass> orderBy(NameAlias nameAlias, boolean ascending) {
        return where().orderBy(nameAlias, ascending);
    }

    @Override
    public Where<ModelClass> orderBy(Property property, boolean ascending) {
        return where().orderBy(property, ascending);
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

    @Override
    public Class<ModelClass> getTable() {
        return whereBase.getTable();
    }

    @Override
    public Query getQueryBuilderBase() {
        return whereBase.getQueryBuilderBase();
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(whereBase.getQuery())
                .append("INDEXED BY ").appendQuoted(indexProperty.getIndexName()).appendSpace();
        return queryBuilder.getQuery();
    }

}
