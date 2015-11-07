package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.structure.Model;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Basic {@link float} property. Accepts only float, {@link BaseModelQueriable}, and
 * {@link ITypeConditional} objects.
 */
public class FloatProperty extends BaseProperty<FloatProperty> {

    public FloatProperty(Class<? extends Model> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public FloatProperty(Class<? extends Model> table, String columnName) {
        this(table, new NameAlias(columnName));
    }

    public FloatProperty(Class<? extends Model> table, String columnName, String aliasName) {
        this(table, new NameAlias(columnName, aliasName));
    }

    @Override
    public FloatProperty as(String aliasName) {
        return new FloatProperty(table, nameAlias.getAliasNameRaw(), aliasName);
    }

    @Override
    public FloatProperty distinct() {
        return new FloatProperty(table, getDistinctAliasName());
    }

    @Override
    public FloatProperty withTable(NameAlias tableNameAlias) {
        return new FloatProperty(table, new NameAlias(nameAlias).withTable(tableNameAlias.getAliasName()));
    }

    public Condition is(float value) {
        return column(nameAlias).is(value);
    }

    public Condition eq(float value) {
        return column(nameAlias).eq(value);
    }

    public Condition isNot(float value) {
        return column(nameAlias).isNot(value);
    }

    public Condition notEq(float value) {
        return column(nameAlias).notEq(value);
    }

    public Condition like(float value) {
        return column(nameAlias).like(value);
    }

    public Condition glob(float value) {
        return column(nameAlias).glob(value);
    }

    public Condition greaterThan(float value) {
        return column(nameAlias).greaterThan(value);
    }

    public Condition greaterThanOrEq(float value) {
        return column(nameAlias).greaterThanOrEq(value);
    }

    public Condition lessThan(float value) {
        return column(nameAlias).lessThan(value);
    }

    public Condition lessThanOrEq(float value) {
        return column(nameAlias).lessThanOrEq(value);
    }

    public Condition.Between between(float value) {
        return column(nameAlias).between(value);
    }

    public Condition.In in(float firstValue, float... values) {
        Condition.In in = column(nameAlias).in(firstValue);
        for (float value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition.In notIn(float firstValue, float... values) {
        Condition.In in = column(nameAlias).notIn(firstValue);
        for (float value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition concatenate(float value) {
        return column(nameAlias).concatenate(value);
    }

    public Condition is(FloatProperty property) {
        return column(nameAlias).is(property);
    }

    public Condition isNot(FloatProperty property) {
        return column(nameAlias).isNot(property);
    }

    public Condition eq(FloatProperty property) {
        return is(property);
    }

    public Condition notEq(FloatProperty property) {
        return isNot(property);
    }

    public Condition greaterThan(FloatProperty property) {
        return column(nameAlias).greaterThan(property);
    }

    public Condition greaterThanOrEq(FloatProperty property) {
        return column(nameAlias).greaterThanOrEq(property);
    }

    public Condition lessThan(FloatProperty property) {
        return column(nameAlias).lessThan(property);
    }

    public Condition lessThanOrEq(FloatProperty property) {
        return column(nameAlias).lessThanOrEq(property);
    }
}
