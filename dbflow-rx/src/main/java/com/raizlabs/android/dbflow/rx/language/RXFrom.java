package com.raizlabs.android.dbflow.rx.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.IFrom;
import com.raizlabs.android.dbflow.sql.language.IWhere;
import com.raizlabs.android.dbflow.sql.language.IndexedBy;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

public class RXFrom<T> extends BaseRXModelQueriable<T> implements IFrom<T> {

    private final From<T> innerFrom;

    public RXFrom(Query q, Class<T> table) {
        super(table);
        innerFrom = new From<>(q, table);
    }

    @Override
    protected BaseModelQueriable<T> getInnerModelQueriable() {
        return innerFrom;
    }

    @Override
    public String getQuery() {
        return innerFrom.getQuery();
    }

    @NonNull
    @Override
    public Class<T> getTable() {
        return innerFrom.getTable();
    }

    @Override
    public Query getQueryBuilderBase() {
        return innerFrom.getQueryBuilderBase();
    }

    @Override
    public IWhere<T> groupBy(NameAlias... nameAliases) {
        return where().groupBy(nameAliases);
    }

    @Override
    public IWhere<T> groupBy(IProperty... properties) {
        return where().groupBy(properties);
    }

    @Override
    public IWhere<T> orderBy(NameAlias nameAlias, boolean ascending) {
        return where().orderBy(nameAlias, ascending);
    }

    @Override
    public IWhere<T> orderBy(IProperty property, boolean ascending) {
        return where().orderBy(property, ascending);
    }

    @Override
    public IWhere<T> orderBy(OrderBy orderBy) {
        return where().orderBy(orderBy);
    }

    @Override
    public IWhere<T> limit(int count) {
        return where().limit(count);
    }

    @Override
    public IWhere<T> offset(int offset) {
        return where().offset(offset);
    }

    @Override
    public IWhere<T> having(SQLCondition... conditions) {
        return where().having(conditions);
    }

    @Override
    public IWhere<T> orderByAll(List<OrderBy> orderBies) {
        return where().orderByAll(orderBies);
    }

    @NonNull
    @Override
    public <TJoin> RXJoin<TJoin, T> join(Class<TJoin> table, @NonNull Join.JoinType joinType) {
        return new RXJoin<>(this, table, joinType);
    }

    @NonNull
    @Override
    public <TJoin> RXJoin<TJoin, T> join(ModelQueriable<TJoin> modelQueriable, @NonNull Join.JoinType joinType) {
        return new RXJoin<>(this, joinType, modelQueriable);
    }

    @NonNull
    @Override
    public <TJoin> RXJoin<TJoin, T> crossJoin(Class<TJoin> table) {
        return new RXJoin<>(this, table, Join.JoinType.CROSS);
    }

    @NonNull
    @Override
    public <TJoin> RXJoin<TJoin, T> crossJoin(ModelQueriable<TJoin> modelQueriable) {
        return new RXJoin<>(this, Join.JoinType.CROSS, modelQueriable);
    }

    @NonNull
    @Override
    public <TJoin> RXJoin<TJoin, T> innerJoin(Class<TJoin> table) {
        return new RXJoin<>(this, table, Join.JoinType.INNER);
    }

    @NonNull
    @Override
    public <TJoin> RXJoin<TJoin, T> innerJoin(ModelQueriable<TJoin> modelQueriable) {
        return new RXJoin<>(this, Join.JoinType.INNER, modelQueriable);
    }

    @NonNull
    @Override
    public RXFrom<T> as(String alias) {
        innerFrom.as(alias);
        return this;
    }

    @NonNull
    @Override
    public <TJoin> RXJoin<TJoin, T> leftOuterJoin(Class<TJoin> table) {
        return new RXJoin<>(this, table, Join.JoinType.LEFT_OUTER);
    }

    @NonNull
    @Override
    public <TJoin> RXJoin<TJoin, T> leftOuterJoin(ModelQueriable<TJoin> modelQueriable) {
        return new RXJoin<>(this, Join.JoinType.LEFT_OUTER, modelQueriable);
    }

    @NonNull
    @Override
    public RXWhere<T> where() {
        return new RXWhere<>(this);
    }

    @NonNull
    @Override
    public RXWhere<T> where(SQLCondition... conditions) {
        return where().andAll(conditions);
    }

    @NonNull
    @Override
    public IndexedBy<T> indexedBy(IndexProperty<T> indexProperty) {
        // TODO: RX
        return new IndexedBy<>(indexProperty, this);
    }

    @Override
    public BaseModel.Action getPrimaryAction() {
        return innerFrom.getPrimaryAction();
    }

    From<T> getInnerFrom() {
        return innerFrom;
    }
}
