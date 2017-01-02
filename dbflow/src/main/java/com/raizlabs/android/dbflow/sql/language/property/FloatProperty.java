package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Basic {@link float} property. Accepts only float, {@link BaseModelQueriable}, and
 * {@link ITypeConditional} objects.
 */
public class FloatProperty extends PrimitiveProperty<FloatProperty> {

    public FloatProperty(Class<?> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public FloatProperty(Class<?> table, String columnName) {
        this(table, new NameAlias.Builder(columnName).build());
    }

    public FloatProperty(Class<?> table, String columnName, String aliasName) {
        this(table, new NameAlias.Builder(columnName).as(aliasName).build());
    }

    @Override
    protected FloatProperty newPropertyInstance(Class<?> table, NameAlias nameAlias) {
        return new FloatProperty(table, nameAlias);
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
        return column(nameAlias).like(String.valueOf(value));
    }

    public Condition notLike(float value) {
        return column(nameAlias).notLike(String.valueOf(value));
    }

    public Condition glob(float value) {
        return column(nameAlias).glob(String.valueOf(value));
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

}
