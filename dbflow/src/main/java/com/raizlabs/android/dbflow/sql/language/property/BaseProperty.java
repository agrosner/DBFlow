package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.IConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Helper class to consolidate logic for properties in one place.
 */
public abstract class BaseProperty<P extends IProperty> implements IProperty<P>, IConditional {

    final Class<?> table;
    protected NameAlias nameAlias;

    protected BaseProperty(Class<?> table, NameAlias nameAlias) {
        this.table = table;
        this.nameAlias = nameAlias;
    }

    @Override
    public P withTable() {
        return withTable(new NameAlias.Builder(FlowManager.getTableName(table)).build());
    }

    @Override
    public Condition is(IConditional conditional) {
        return getCondition().is(conditional);
    }

    @Override
    public Condition eq(IConditional conditional) {
        return getCondition().eq(conditional);
    }

    @Override
    public Condition isNot(IConditional conditional) {
        return getCondition().isNot(conditional);
    }

    @Override
    public Condition notEq(IConditional conditional) {
        return getCondition().notEq(conditional);
    }

    @Override
    public Condition like(IConditional conditional) {
        return getCondition().like(conditional);
    }

    @Override
    public Condition glob(IConditional conditional) {
        return getCondition().glob(conditional);
    }

    @Override
    public Condition like(String value) {
        return getCondition().like(value);
    }

    @Override
    public Condition notLike(String value) {
        return getCondition().notLike(value);
    }

    @Override
    public Condition glob(String value) {
        return getCondition().glob(value);
    }

    @Override
    public Condition greaterThan(IConditional conditional) {
        return getCondition().greaterThan(conditional);
    }

    @Override
    public Condition greaterThanOrEq(IConditional conditional) {
        return getCondition().greaterThanOrEq(conditional);
    }

    @Override
    public Condition lessThan(IConditional conditional) {
        return getCondition().lessThan(conditional);
    }

    @Override
    public Condition lessThanOrEq(IConditional conditional) {
        return getCondition().lessThanOrEq(conditional);
    }

    @Override
    public Condition.Between between(IConditional conditional) {
        return getCondition().between(conditional);
    }

    @Override
    public Condition.In in(IConditional firstConditional, IConditional... conditionals) {
        return getCondition().in(firstConditional, conditionals);
    }

    @Override
    public Condition.In notIn(IConditional firstConditional, IConditional... conditionals) {
        return getCondition().notIn(firstConditional, conditionals);
    }

    @Override
    public Condition is(BaseModelQueriable baseModelQueriable) {
        return getCondition().is(baseModelQueriable);
    }

    @Override
    public Condition isNull() {
        return getCondition().isNull();
    }

    @Override
    public Condition eq(BaseModelQueriable baseModelQueriable) {
        return getCondition().eq(baseModelQueriable);
    }

    @Override
    public Condition isNot(BaseModelQueriable baseModelQueriable) {
        return getCondition().isNot(baseModelQueriable);
    }

    @Override
    public Condition isNotNull() {
        return getCondition().isNotNull();
    }

    @Override
    public Condition notEq(BaseModelQueriable baseModelQueriable) {
        return getCondition().notEq(baseModelQueriable);
    }

    @Override
    public Condition like(BaseModelQueriable baseModelQueriable) {
        return getCondition().like(baseModelQueriable);
    }

    @Override
    public Condition glob(BaseModelQueriable baseModelQueriable) {
        return getCondition().glob(baseModelQueriable);
    }

    @Override
    public Condition greaterThan(BaseModelQueriable baseModelQueriable) {
        return getCondition().greaterThan(baseModelQueriable);
    }

    @Override
    public Condition greaterThanOrEq(BaseModelQueriable baseModelQueriable) {
        return getCondition().greaterThanOrEq(baseModelQueriable);
    }

    @Override
    public Condition lessThan(BaseModelQueriable baseModelQueriable) {
        return getCondition().lessThan(baseModelQueriable);
    }

    @Override
    public Condition lessThanOrEq(BaseModelQueriable baseModelQueriable) {
        return getCondition().lessThanOrEq(baseModelQueriable);
    }

    @Override
    public Condition.Between between(BaseModelQueriable baseModelQueriable) {
        return getCondition().between(baseModelQueriable);
    }

    @Override
    public Condition.In in(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables) {
        return getCondition().in(firstBaseModelQueriable, baseModelQueriables);
    }

    @Override
    public Condition.In notIn(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables) {
        return getCondition().notIn(firstBaseModelQueriable, baseModelQueriables);
    }

    @Override
    public Condition concatenate(IConditional conditional) {
        return getCondition().concatenate(conditional);
    }

    @Override
    public Class<?> getTable() {
        return table;
    }

    @Override
    public NameAlias getNameAlias() {
        return nameAlias;
    }

    @Override
    public String getQuery() {
        return getNameAlias().getQuery();
    }

    @Override
    public String getCursorKey() {
        return getNameAlias().getQuery();
    }

    public String getDefinition() {
        return getNameAlias().getFullQuery();
    }

    @Override
    public String toString() {
        return getNameAlias().toString();
    }

    /**
     * @return helper method to construct it in a {@link #distinct()} call.
     */
    protected NameAlias getDistinctAliasName() {
        return getNameAlias()
                .newBuilder()
                .distinct()
                .build();
    }

    protected Condition getCondition() {
        return column(getNameAlias());
    }
}
