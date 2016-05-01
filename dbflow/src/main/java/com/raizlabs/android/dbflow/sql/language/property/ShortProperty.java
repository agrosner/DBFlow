package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias2;
import com.raizlabs.android.dbflow.structure.Model;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Basic {@link short} property. Accepts only short, {@link BaseModelQueriable}, and
 * {@link ITypeConditional} objects.
 */
public class ShortProperty extends BaseProperty<ShortProperty> {

    public ShortProperty(Class<? extends Model> table, NameAlias2 nameAlias) {
        super(table, nameAlias);
    }

    public ShortProperty(Class<? extends Model> table, String columnName) {
        this(table, new NameAlias2.Builder(columnName).build());
    }

    public ShortProperty(Class<? extends Model> table, String columnName, String aliasName) {
        this(table, new NameAlias2.Builder(columnName).as(aliasName).build());
    }

    @Override
    public ShortProperty plus(IProperty iProperty) {
        return new ShortProperty(table, NameAlias2.joinNames(Condition.Operation.PLUS,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ShortProperty minus(IProperty iProperty) {
        return new ShortProperty(table, NameAlias2.joinNames(Condition.Operation.MINUS,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ShortProperty dividedBy(IProperty iProperty) {
        return new ShortProperty(table, NameAlias2.joinNames(Condition.Operation.DIVISION,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ShortProperty multipliedBy(IProperty iProperty) {
        return new ShortProperty(table, NameAlias2.joinNames(Condition.Operation.MULTIPLY,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ShortProperty mod(IProperty iProperty) {
        return new ShortProperty(table, NameAlias2.joinNames(Condition.Operation.MOD,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ShortProperty concatenate(IProperty iProperty) {
        return new ShortProperty(table, NameAlias2.joinNames(Condition.Operation.CONCATENATE,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ShortProperty as(String aliasName) {
        return new ShortProperty(table, nameAlias
                .newBuilder()
                .as(aliasName).build());
    }

    @Override
    public ShortProperty distinct() {
        return new ShortProperty(table, getDistinctAliasName());
    }

    @Override
    public ShortProperty withTable(NameAlias2 tableNameAlias) {
        return new ShortProperty(table, nameAlias
                .newBuilder()
                .withTable(tableNameAlias.getQuery())
                .build());
    }

    public Condition is(short value) {
        return column(nameAlias).is(value);
    }

    public Condition eq(short value) {
        return column(nameAlias).eq(value);
    }

    public Condition isNot(short value) {
        return column(nameAlias).isNot(value);
    }

    public Condition notEq(short value) {
        return column(nameAlias).notEq(value);
    }

    public Condition like(short value) {
        return column(nameAlias).like(String.valueOf(value));
    }

    public Condition glob(short value) {
        return column(nameAlias).glob(String.valueOf(value));
    }

    public Condition greaterThan(short value) {
        return column(nameAlias).greaterThan(value);
    }

    public Condition greaterThanOrEq(short value) {
        return column(nameAlias).greaterThanOrEq(value);
    }

    public Condition lessThan(short value) {
        return column(nameAlias).lessThan(value);
    }

    public Condition lessThanOrEq(short value) {
        return column(nameAlias).lessThanOrEq(value);
    }

    public Condition.Between between(short value) {
        return column(nameAlias).between(value);
    }

    public Condition.In in(short firstValue, short... values) {
        Condition.In in = column(nameAlias).in(firstValue);
        for (short value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition.In notIn(short firstValue, short... values) {
        Condition.In in = column(nameAlias).notIn(firstValue);
        for (short value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition concatenate(short value) {
        return column(nameAlias).concatenate(value);
    }

    public Condition is(ShortProperty property) {
        return column(nameAlias).is(property);
    }

    public Condition isNot(ShortProperty property) {
        return column(nameAlias).isNot(property);
    }

    public Condition eq(ShortProperty property) {
        return is(property);
    }

    public Condition notEq(ShortProperty property) {
        return isNot(property);
    }

    public Condition greaterThan(ShortProperty property) {
        return column(nameAlias).greaterThan(property);
    }

    public Condition greaterThanOrEq(ShortProperty property) {
        return column(nameAlias).greaterThanOrEq(property);
    }

    public Condition lessThan(ShortProperty property) {
        return column(nameAlias).lessThan(property);
    }

    public Condition lessThanOrEq(ShortProperty property) {
        return column(nameAlias).lessThanOrEq(property);
    }
}
