package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.IConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.structure.Model;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Helper class to consolidate logic for properties in one place.
 */
public abstract class BaseProperty<P extends IProperty> implements IProperty<P>, IConditional {

    final Class<? extends Model> table;
    protected NameAlias nameAlias;

    protected BaseProperty(Class<? extends Model> table, NameAlias nameAlias) {
        this.table = table;
        this.nameAlias = nameAlias;
    }

    @Override
    public P withTable() {
        return withTable(new NameAlias.Builder(FlowManager.getTableName(table)).build());
    }

    @Override
    public Condition is(IConditional conditional) {
        return column(getNameAlias()).is(conditional);
    }

    @Override
    public Condition eq(IConditional conditional) {
        return column(getNameAlias()).eq(conditional);
    }

    @Override
    public Condition isNot(IConditional conditional) {
        return column(getNameAlias()).isNot(conditional);
    }

    @Override
    public Condition notEq(IConditional conditional) {
        return column(getNameAlias()).notEq(conditional);
    }

    @Override
    public Condition like(IConditional conditional) {
        return column(getNameAlias()).like(conditional);
    }

    @Override
    public Condition glob(IConditional conditional) {
        return column(getNameAlias()).glob(conditional);
    }

    @Override
    public Condition like(String value) {
        return column(getNameAlias()).like(value);
    }

    @Override
    public Condition notLike(String value) {
        return column(getNameAlias()).notLike(value);
    }

    @Override
    public Condition glob(String value) {
        return column(getNameAlias()).glob(value);
    }

    @Override
    public Condition greaterThan(IConditional conditional) {
        return column(getNameAlias()).greaterThan(conditional);
    }

    @Override
    public Condition greaterThanOrEq(IConditional conditional) {
        return column(getNameAlias()).greaterThanOrEq(conditional);
    }

    @Override
    public Condition lessThan(IConditional conditional) {
        return column(getNameAlias()).lessThan(conditional);
    }

    @Override
    public Condition lessThanOrEq(IConditional conditional) {
        return column(getNameAlias()).lessThanOrEq(conditional);
    }

    @Override
    public Condition.Between between(IConditional conditional) {
        return column(getNameAlias()).between(conditional);
    }

    @Override
    public Condition.In in(IConditional firstConditional, IConditional... conditionals) {
        return column(getNameAlias()).in(firstConditional, conditionals);
    }

    @Override
    public Condition.In notIn(IConditional firstConditional, IConditional... conditionals) {
        return column(getNameAlias()).notIn(firstConditional, conditionals);
    }

    @Override
    public Condition is(BaseModelQueriable baseModelQueriable) {
        return column(getNameAlias()).is(baseModelQueriable);
    }

    @Override
    public Condition isNull() {
        return column(getNameAlias()).isNull();
    }

    @Override
    public Condition eq(BaseModelQueriable baseModelQueriable) {
        return column(getNameAlias()).eq(baseModelQueriable);
    }

    @Override
    public Condition isNot(BaseModelQueriable baseModelQueriable) {
        return column(getNameAlias()).isNot(baseModelQueriable);
    }

    @Override
    public Condition isNotNull() {
        return column(getNameAlias()).isNotNull();
    }

    @Override
    public Condition notEq(BaseModelQueriable baseModelQueriable) {
        return column(getNameAlias()).notEq(baseModelQueriable);
    }

    @Override
    public Condition like(BaseModelQueriable baseModelQueriable) {
        return column(getNameAlias()).like(baseModelQueriable);
    }

    @Override
    public Condition glob(BaseModelQueriable baseModelQueriable) {
        return column(getNameAlias()).glob(baseModelQueriable);
    }

    @Override
    public Condition greaterThan(BaseModelQueriable baseModelQueriable) {
        return column(getNameAlias()).greaterThan(baseModelQueriable);
    }

    @Override
    public Condition greaterThanOrEq(BaseModelQueriable baseModelQueriable) {
        return column(getNameAlias()).greaterThanOrEq(baseModelQueriable);
    }

    @Override
    public Condition lessThan(BaseModelQueriable baseModelQueriable) {
        return column(getNameAlias()).lessThan(baseModelQueriable);
    }

    @Override
    public Condition lessThanOrEq(BaseModelQueriable baseModelQueriable) {
        return column(getNameAlias()).lessThanOrEq(baseModelQueriable);
    }

    @Override
    public Condition.Between between(BaseModelQueriable baseModelQueriable) {
        return column(getNameAlias()).between(baseModelQueriable);
    }

    @Override
    public Condition.In in(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables) {
        return column(getNameAlias()).in(firstBaseModelQueriable, baseModelQueriables);
    }

    @Override
    public Condition.In notIn(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables) {
        return column(getNameAlias()).notIn(firstBaseModelQueriable, baseModelQueriables);
    }

    @Override
    public Condition concatenate(IConditional conditional) {
        return column(getNameAlias()).concatenate(conditional);
    }

    @Override
    public Class<? extends Model> getTable() {
        return table;
    }

    @Override
    public NameAlias getNameAlias() {
        return nameAlias;
    }

    @Override
    public String getContainerKey() {
        return getNameAlias().getNameAsKey();
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
}
