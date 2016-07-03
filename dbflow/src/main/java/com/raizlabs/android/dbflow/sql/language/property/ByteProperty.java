package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.structure.Model;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Basic {@link byte} property. Accepts only byte, {@link BaseModelQueriable}, and
 * {@link ITypeConditional} objects.
 */
public class ByteProperty extends BaseProperty<ByteProperty> {

    public ByteProperty(Class<? extends Model> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public ByteProperty(Class<? extends Model> table, String columnName) {
        this(table, new NameAlias.Builder(columnName).build());
    }

    public ByteProperty(Class<? extends Model> table, String columnName, String aliasName) {
        this(table, new NameAlias.Builder(columnName).as(aliasName).build());
    }

    @Override
    public ByteProperty as(String aliasName) {
        return new ByteProperty(table, nameAlias
                .newBuilder()
                .as(aliasName)
                .build());
    }

    @Override
    public ByteProperty plus(IProperty iProperty) {
        return new ByteProperty(table, NameAlias.joinNames(Condition.Operation.PLUS,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ByteProperty minus(IProperty iProperty) {
        return new ByteProperty(table, NameAlias.joinNames(Condition.Operation.MINUS,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ByteProperty dividedBy(IProperty iProperty) {
        return new ByteProperty(table, NameAlias.joinNames(Condition.Operation.DIVISION,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ByteProperty multipliedBy(IProperty iProperty) {
        return new ByteProperty(table, NameAlias.joinNames(Condition.Operation.MULTIPLY,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ByteProperty mod(IProperty iProperty) {
        return new ByteProperty(table, NameAlias.joinNames(Condition.Operation.MOD,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ByteProperty concatenate(IProperty iProperty) {
        return new ByteProperty(table, NameAlias.joinNames(Condition.Operation.CONCATENATE,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public ByteProperty distinct() {
        return new ByteProperty(table, getDistinctAliasName());
    }

    @Override
    public ByteProperty withTable(NameAlias tableNameAlias) {
        return new ByteProperty(table, nameAlias
                .newBuilder()
                .withTable(tableNameAlias.getQuery())
                .build());
    }

    public Condition is(byte value) {
        return column(nameAlias).is(value);
    }

    public Condition eq(byte value) {
        return column(nameAlias).eq(value);
    }

    public Condition isNot(byte value) {
        return column(nameAlias).isNot(value);
    }

    public Condition notEq(byte value) {
        return column(nameAlias).notEq(value);
    }

    public Condition like(byte value) {
        return column(nameAlias).like(String.valueOf(value));
    }

    public Condition glob(byte value) {
        return column(nameAlias).glob(String.valueOf(value));
    }

    public Condition greaterThan(byte value) {
        return column(nameAlias).greaterThan(value);
    }

    public Condition greaterThanOrEq(byte value) {
        return column(nameAlias).greaterThanOrEq(value);
    }

    public Condition lessThan(byte value) {
        return column(nameAlias).lessThan(value);
    }

    public Condition lessThanOrEq(byte value) {
        return column(nameAlias).lessThanOrEq(value);
    }

    public Condition.Between between(byte value) {
        return column(nameAlias).between(value);
    }

    public Condition.In in(byte firstValue, byte... values) {
        Condition.In in = column(nameAlias).in(firstValue);
        for (byte value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition.In notIn(byte firstValue, byte... values) {
        Condition.In in = column(nameAlias).notIn(firstValue);
        for (byte value : values) {
            in.and(value);
        }
        return in;
    }

    public Condition concatenate(byte value) {
        return column(nameAlias).concatenate(value);
    }

    public Condition is(ByteProperty property) {
        return column(nameAlias).is(property);
    }

    public Condition isNot(ByteProperty property) {
        return column(nameAlias).isNot(property);
    }

    public Condition eq(ByteProperty property) {
        return is(property);
    }

    public Condition notEq(ByteProperty property) {
        return isNot(property);
    }

    public Condition greaterThan(ByteProperty property) {
        return column(nameAlias).greaterThan(property);
    }

    public Condition greaterThanOrEq(ByteProperty property) {
        return column(nameAlias).greaterThanOrEq(property);
    }

    public Condition lessThan(ByteProperty property) {
        return column(nameAlias).lessThan(property);
    }

    public Condition lessThanOrEq(ByteProperty property) {
        return column(nameAlias).lessThanOrEq(property);
    }
}
