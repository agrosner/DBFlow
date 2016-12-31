package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Basic {@link long} property. Accepts only long, {@link BaseModelQueriable}, and
 * {@link ITypeConditional} objects.
 */
public class LongProperty extends PrimitiveProperty<LongProperty> {

    public LongProperty(Class<?> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public LongProperty(Class<?> table, String columnName) {
        this(table, new NameAlias.Builder(columnName).build());
    }

    public LongProperty(Class<?> table, String columnName, String aliasName) {
        this(table, new NameAlias.Builder(columnName).as(aliasName).build());
    }

    @Override
    protected LongProperty newPropertyInstance(Class<?> table, NameAlias nameAlias) {
        return new LongProperty(table, nameAlias);
    }

    public Condition is(long value) {
        return column(nameAlias).is(value);
    }

    public Condition eq(long value) {
        return column(nameAlias).eq(value);
    }

    public Condition isNot(long value) {
        return column(nameAlias).isNot(value);
    }

    public Condition notEq(long value) {
        return column(nameAlias).notEq(value);
    }

    public Condition like(long value) {
        return column(nameAlias).like(String.valueOf(value));
    }

    public Condition notLike(long value) {
        return column(nameAlias).notLike(String.valueOf(value));
    }

    public Condition glob(long value) {
        return column(nameAlias).glob(String.valueOf(value));
    }

    public Condition greaterThan(long value) {
        return column(nameAlias).greaterThan(value);
    }

    public Condition greaterThanOrEq(long value) {
        return column(nameAlias).greaterThanOrEq(value);
    }

    public Condition lessThan(long value) {
        return column(nameAlias).lessThan(value);
    }

    public Condition lessThanOrEq(long value) {
        return column(nameAlias).lessThanOrEq(value);
    }

    public Condition.Between between(long value) {
        return column(nameAlias).between(value);
    }

    public Condition.In in(long firstValue, long... values) {
        Condition.In in = column(nameAlias).in(firstValue);
        for (long value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition.In notIn(long firstValue, long... values) {
        Condition.In in = column(nameAlias).notIn(firstValue);
        for (long value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition concatenate(long value) {
        return column(nameAlias).concatenate(value);
    }

}
