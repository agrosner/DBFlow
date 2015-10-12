package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.IConditional;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.structure.Model;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Helper class to consolidate logic for properties in one place.
 */
public abstract class BaseProperty<P extends IProperty> implements IProperty<P>, IConditional {

    final Class<? extends Model> table;
    protected final NameAlias nameAlias;

    protected BaseProperty(Class<? extends Model> table, NameAlias nameAlias) {
        this.table = table;
        this.nameAlias = nameAlias;
    }

    @Override
    public P withTable() {
        return withTable(new NameAlias(FlowManager.getTableName(table)));
    }

    @Override
    public Condition is(ITypeConditional conditional) {
        return column(nameAlias).is(conditional);
    }

    @Override
    public Condition eq(ITypeConditional conditional) {
        return column(nameAlias).eq(conditional);
    }

    @Override
    public Condition isNot(ITypeConditional conditional) {
        return column(nameAlias).isNot(conditional);
    }

    @Override
    public Condition notEq(ITypeConditional conditional) {
        return column(nameAlias).notEq(conditional);
    }

    @Override
    public Condition like(ITypeConditional conditional) {
        return column(nameAlias).like(conditional);
    }

    @Override
    public Condition glob(ITypeConditional conditional) {
        return column(nameAlias).glob(conditional);
    }

    @Override
    public Condition greaterThan(ITypeConditional conditional) {
        return column(nameAlias).greaterThan(conditional);
    }

    @Override
    public Condition greaterThanOrEq(ITypeConditional conditional) {
        return column(nameAlias).greaterThanOrEq(conditional);
    }

    @Override
    public Condition lessThan(ITypeConditional conditional) {
        return column(nameAlias).lessThan(conditional);
    }

    @Override
    public Condition lessThanOrEq(ITypeConditional conditional) {
        return column(nameAlias).lessThanOrEq(conditional);
    }

    @Override
    public Condition.Between between(ITypeConditional conditional) {
        return column(nameAlias).between(conditional);
    }

    @Override
    public Condition.In in(ITypeConditional firstConditional, ITypeConditional... conditionals) {
        return column(nameAlias).in(firstConditional, conditionals);
    }

    @Override
    public Condition.In notIn(ITypeConditional firstConditional, ITypeConditional... conditionals) {
        return column(nameAlias).notIn(firstConditional, conditionals);
    }

    @Override
    public Condition is(BaseModelQueriable baseModelQueriable) {
        return column(nameAlias).is(baseModelQueriable);
    }

    @Override
    public Condition isNull() {
        return column(nameAlias).isNull();
    }

    @Override
    public Condition eq(BaseModelQueriable baseModelQueriable) {
        return column(nameAlias).eq(baseModelQueriable);
    }

    @Override
    public Condition isNot(BaseModelQueriable baseModelQueriable) {
        return column(nameAlias).isNot(baseModelQueriable);
    }

    @Override
    public Condition isNotNull() {
        return column(nameAlias).isNotNull();
    }

    @Override
    public Condition notEq(BaseModelQueriable baseModelQueriable) {
        return column(nameAlias).notEq(baseModelQueriable);
    }

    @Override
    public Condition like(BaseModelQueriable baseModelQueriable) {
        return column(nameAlias).like(baseModelQueriable);
    }

    @Override
    public Condition glob(BaseModelQueriable baseModelQueriable) {
        return column(nameAlias).glob(baseModelQueriable);
    }

    @Override
    public Condition greaterThan(BaseModelQueriable baseModelQueriable) {
        return column(nameAlias).greaterThan(baseModelQueriable);
    }

    @Override
    public Condition greaterThanOrEq(BaseModelQueriable baseModelQueriable) {
        return column(nameAlias).greaterThanOrEq(baseModelQueriable);
    }

    @Override
    public Condition lessThan(BaseModelQueriable baseModelQueriable) {
        return column(nameAlias).lessThan(baseModelQueriable);
    }

    @Override
    public Condition lessThanOrEq(BaseModelQueriable baseModelQueriable) {
        return column(nameAlias).lessThanOrEq(baseModelQueriable);
    }

    @Override
    public Condition.Between between(BaseModelQueriable baseModelQueriable) {
        return column(nameAlias).between(baseModelQueriable);
    }

    @Override
    public Condition.In in(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables) {
        return column(nameAlias).in(firstBaseModelQueriable, baseModelQueriables);
    }

    @Override
    public Condition.In notIn(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables) {
        return column(nameAlias).notIn(firstBaseModelQueriable, baseModelQueriables);
    }

    @Override
    public Condition concatenate(ITypeConditional conditional) {
        return column(nameAlias).concatenate(conditional);
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
        return nameAlias.getAliasNameRaw();
    }

    @Override
    public String getQuery() {
        return nameAlias.getQuery();
    }

    public String getDefinition() {
        return nameAlias.getDefinition();
    }

    @Override
    public String toString() {
        return nameAlias.toString();
    }

    /**
     * @return helper method to construct it in a {@link #distinct()} call.
     */
    protected NameAlias getDistinctAliasName() {
        return new NameAlias("DISTINCT " + nameAlias.getName(), nameAlias.getAliasPropertyRaw()).tickName(false);
    }
}
