package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias2;
import com.raizlabs.android.dbflow.structure.Model;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Basic {@link int} property. Accepts only int, {@link BaseModelQueriable}, and
 * {@link ITypeConditional} objects.
 */
public class IntProperty extends BaseProperty<IntProperty> {

    public IntProperty(Class<? extends Model> table, NameAlias2 nameAlias) {
        super(table, nameAlias);
    }

    public IntProperty(Class<? extends Model> table, String columnName) {
        this(table, new NameAlias2.Builder(columnName).build());
    }

    public IntProperty(Class<? extends Model> table, String columnName, String aliasName) {
        this(table, new NameAlias2.Builder(columnName).as(aliasName).build());
    }

    @Override
    public IntProperty plus(IProperty iProperty) {
        return new IntProperty(table, NameAlias2.joinNames(Condition.Operation.PLUS,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public IntProperty minus(IProperty iProperty) {
        return new IntProperty(table, NameAlias2.joinNames(Condition.Operation.MINUS,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public IntProperty dividedBy(IProperty iProperty) {
        return new IntProperty(table, NameAlias2.joinNames(Condition.Operation.DIVISION,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public IntProperty multipliedBy(IProperty iProperty) {
        return new IntProperty(table, NameAlias2.joinNames(Condition.Operation.MULTIPLY,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public IntProperty mod(IProperty iProperty) {
        return new IntProperty(table, NameAlias2.joinNames(Condition.Operation.MOD,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public IntProperty concatenate(IProperty iProperty) {
        return new IntProperty(table, NameAlias2.joinNames(Condition.Operation.CONCATENATE,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public IntProperty as(String aliasName) {
        return new IntProperty(table, nameAlias
                .newBuilder()
                .as(aliasName)
                .build());
    }

    @Override
    public IntProperty distinct() {
        return new IntProperty(table, getDistinctAliasName());
    }

    @Override
    public IntProperty withTable(NameAlias2 tableNameAlias) {
        return new IntProperty(table, nameAlias
                .newBuilder()
                .withTable(tableNameAlias.getQuery())
                .build());
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

    public Condition is(IntProperty property) {
        return column(nameAlias).is(property);
    }

    public Condition isNot(IntProperty property) {
        return column(nameAlias).isNot(property);
    }

    public Condition eq(IntProperty property) {
        return is(property);
    }

    public Condition notEq(IntProperty property) {
        return isNot(property);
    }

    public Condition greaterThan(IntProperty property) {
        return column(nameAlias).greaterThan(property);
    }

    public Condition greaterThanOrEq(IntProperty property) {
        return column(nameAlias).greaterThanOrEq(property);
    }

    public Condition lessThan(IntProperty property) {
        return column(nameAlias).lessThan(property);
    }

    public Condition lessThanOrEq(IntProperty property) {
        return column(nameAlias).lessThanOrEq(property);
    }

}
