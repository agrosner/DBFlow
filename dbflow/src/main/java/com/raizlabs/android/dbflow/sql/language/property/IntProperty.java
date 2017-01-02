package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Basic {@link int} property. Accepts only int, {@link BaseModelQueriable}, and
 * {@link ITypeConditional} objects.
 */
public class IntProperty extends PrimitiveProperty<IntProperty> {

    public IntProperty(Class<?> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public IntProperty(Class<?> table, String columnName) {
        this(table, new NameAlias.Builder(columnName).build());
    }

    public IntProperty(Class<?> table, String columnName, String aliasName) {
        this(table, new NameAlias.Builder(columnName).as(aliasName).build());
    }

    @Override
    protected IntProperty newPropertyInstance(Class<?> table, NameAlias nameAlias) {
        return new IntProperty(table, nameAlias);
    }

    public Condition is(int value) {
        return column(nameAlias).is(value);
    }

    public Condition eq(int value) {
        return column(nameAlias).eq(value);
    }

    public Condition isNot(int value) {
        return column(nameAlias).isNot(value);
    }

    public Condition notEq(int value) {
        return column(nameAlias).notEq(value);
    }

    public Condition like(int value) {
        return column(nameAlias).like(String.valueOf(value));
    }

    public Condition notLike(int value) {
        return column(nameAlias).notLike(String.valueOf(value));
    }

    public Condition glob(int value) {
        return column(nameAlias).glob(String.valueOf(value));
    }

    public Condition greaterThan(int value) {
        return column(nameAlias).greaterThan(value);
    }

    public Condition greaterThanOrEq(int value) {
        return column(nameAlias).greaterThanOrEq(value);
    }

    public Condition lessThan(int value) {
        return column(nameAlias).lessThan(value);
    }

    public Condition lessThanOrEq(int value) {
        return column(nameAlias).lessThanOrEq(value);
    }

    public Condition.Between between(int value) {
        return column(nameAlias).between(value);
    }

    public Condition.In in(int firstValue, int... values) {
        Condition.In in = column(nameAlias).in(firstValue);
        for (int value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition.In notIn(int firstValue, int... values) {
        Condition.In in = column(nameAlias).notIn(firstValue);
        for (int value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition concatenate(int value) {
        return column(nameAlias).concatenate(value);
    }

}
