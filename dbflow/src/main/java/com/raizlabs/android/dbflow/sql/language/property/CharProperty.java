package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.NameAlias;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description:
 */
public class CharProperty extends PrimitiveProperty<CharProperty> {

    public CharProperty(Class<?> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public CharProperty(Class<?> table, String columnName) {
        this(table, new NameAlias.Builder(columnName).build());
    }

    public CharProperty(Class<?> table, String columnName, String aliasName) {
        this(table, new NameAlias.Builder(columnName).as(aliasName).build());
    }

    @Override
    protected CharProperty newPropertyInstance(Class<?> table, NameAlias nameAlias) {
        return new CharProperty(table, nameAlias);
    }

    public Condition is(char value) {
        return column(nameAlias).is(value);
    }

    public Condition eq(char value) {
        return column(nameAlias).eq(value);
    }

    public Condition isNot(char value) {
        return column(nameAlias).isNot(value);
    }

    public Condition notEq(char value) {
        return column(nameAlias).notEq(value);
    }

    public Condition like(char value) {
        return column(nameAlias).like(String.valueOf(value));
    }

    public Condition notLike(char value) {
        return column(nameAlias).notLike(String.valueOf(value));
    }

    public Condition glob(char value) {
        return column(nameAlias).glob(String.valueOf(value));
    }

    public Condition greaterThan(char value) {
        return column(nameAlias).greaterThan(value);
    }

    public Condition greaterThanOrEq(char value) {
        return column(nameAlias).greaterThanOrEq(value);
    }

    public Condition lessThan(char value) {
        return column(nameAlias).lessThan(value);
    }

    public Condition lessThanOrEq(char value) {
        return column(nameAlias).lessThanOrEq(value);
    }

    public Condition.Between between(char value) {
        return column(nameAlias).between(value);
    }

    public Condition.In in(char firstValue, char... values) {
        Condition.In in = column(nameAlias).in(firstValue);
        for (char value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition.In notIn(char firstValue, char... values) {
        Condition.In in = column(nameAlias).notIn(firstValue);
        for (char value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition concatenate(char value) {
        return column(nameAlias).concatenate(value);
    }

}