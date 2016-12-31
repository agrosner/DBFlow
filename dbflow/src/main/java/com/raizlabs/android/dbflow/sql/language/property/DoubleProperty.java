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
public class DoubleProperty extends PrimitiveProperty<DoubleProperty> {

    public DoubleProperty(Class<?> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public DoubleProperty(Class<?> table, String columnName) {
        this(table, new NameAlias.Builder(columnName).build());
    }

    public DoubleProperty(Class<?> table, String columnName, String aliasName) {
        this(table, new NameAlias.Builder(columnName).as(aliasName).build());
    }

    @Override
    protected DoubleProperty newPropertyInstance(Class<?> table, NameAlias nameAlias) {
        return new DoubleProperty(table, nameAlias);
    }

    public Condition is(double value) {
        return column(nameAlias).is(value);
    }

    public Condition eq(double value) {
        return column(nameAlias).eq(value);
    }

    public Condition isNot(double value) {
        return column(nameAlias).isNot(value);
    }

    public Condition notEq(double value) {
        return column(nameAlias).notEq(value);
    }

    public Condition like(double value) {
        return column(nameAlias).like(String.valueOf(value));
    }

    public Condition notLike(double value) {
        return column(nameAlias).notLike(String.valueOf(value));
    }

    public Condition glob(double value) {
        return column(nameAlias).glob(String.valueOf(value));
    }

    public Condition greaterThan(double value) {
        return column(nameAlias).greaterThan(value);
    }

    public Condition greaterThanOrEq(double value) {
        return column(nameAlias).greaterThanOrEq(value);
    }

    public Condition lessThan(double value) {
        return column(nameAlias).lessThan(value);
    }

    public Condition lessThanOrEq(double value) {
        return column(nameAlias).lessThanOrEq(value);
    }

    public Condition.Between between(double value) {
        return column(nameAlias).between(value);
    }

    public Condition.In in(double firstValue, double... values) {
        Condition.In in = column(nameAlias).in(firstValue);
        for (double value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition.In notIn(double firstValue, double... values) {
        Condition.In in = column(nameAlias).notIn(firstValue);
        for (double value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition concatenate(double value) {
        return column(nameAlias).concatenate(value);
    }

}
