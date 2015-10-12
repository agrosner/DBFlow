package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.structure.Model;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Basic {@link int} property. Accepts only int, {@link BaseModelQueriable}, and
 * {@link ITypeConditional} objects.
 */
public class IntProperty extends BaseProperty<IntProperty> {

    public IntProperty(Class<? extends Model> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public IntProperty(Class<? extends Model> table, String columnName) {
        this(table, new NameAlias(columnName));
    }

    public IntProperty(Class<? extends Model> table, String columnName, String aliasName) {
        this(table, new NameAlias(columnName, aliasName));
    }

    @Override
    public IntProperty as(String aliasName) {
        return new IntProperty(table, nameAlias.getAliasNameRaw(), aliasName);
    }

    @Override
    public IntProperty distinct() {
        return new IntProperty(table, getDistinctAliasName());
    }

    @Override
    public IntProperty withTable(NameAlias tableNameAlias) {
        return new IntProperty(table, new NameAlias(tableNameAlias).withTable(tableNameAlias.getAliasName()));
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
        return column(nameAlias).like(value);
    }

    public Condition glob(int value) {
        return column(nameAlias).glob(value);
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
