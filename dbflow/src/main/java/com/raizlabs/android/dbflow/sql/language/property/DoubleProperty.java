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
public class DoubleProperty extends BaseProperty<DoubleProperty> {

    public DoubleProperty(Class<? extends Model> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public DoubleProperty(Class<? extends Model> table, String columnName) {
        this(table, new NameAlias(columnName));
    }

    public DoubleProperty(Class<? extends Model> table, String columnName, String aliasName) {
        this(table, new NameAlias(columnName, aliasName));
    }

    @Override
    public DoubleProperty plus(IProperty iProperty) {
        return new DoubleProperty(table, NameAlias.joinNames(Condition.Operation.PLUS,
            nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public DoubleProperty minus(IProperty iProperty) {
        return new DoubleProperty(table, NameAlias.joinNames(Condition.Operation.MINUS,
            nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public DoubleProperty dividedBy(IProperty iProperty) {
        return new DoubleProperty(table, NameAlias.joinNames(Condition.Operation.DIVISION,
            nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public DoubleProperty multipliedBy(IProperty iProperty) {
        return new DoubleProperty(table, NameAlias.joinNames(Condition.Operation.MULTIPLY,
            nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public DoubleProperty mod(IProperty iProperty) {
        return new DoubleProperty(table, NameAlias.joinNames(Condition.Operation.MOD,
            nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public DoubleProperty concatenate(IProperty iProperty) {
        return new DoubleProperty(table, NameAlias.joinNames(Condition.Operation.CONCATENATE,
            nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public DoubleProperty as(String aliasName) {
        return new DoubleProperty(table, nameAlias.getAliasNameRaw(), aliasName);
    }

    @Override
    public DoubleProperty distinct() {
        return new DoubleProperty(table, getDistinctAliasName());
    }

    @Override
    public DoubleProperty withTable(NameAlias tableNameAlias) {
        return new DoubleProperty(table, new NameAlias(nameAlias).withTable(tableNameAlias.getAliasName()));
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

    public Condition is(DoubleProperty property) {
        return column(nameAlias).is(property);
    }

    public Condition isNot(DoubleProperty property) {
        return column(nameAlias).isNot(property);
    }

    public Condition eq(DoubleProperty property) {
        return is(property);
    }

    public Condition notEq(DoubleProperty property) {
        return isNot(property);
    }

    public Condition greaterThan(DoubleProperty property) {
        return column(nameAlias).greaterThan(property);
    }

    public Condition greaterThanOrEq(DoubleProperty property) {
        return column(nameAlias).greaterThanOrEq(property);
    }

    public Condition lessThan(DoubleProperty property) {
        return column(nameAlias).lessThan(property);
    }

    public Condition lessThanOrEq(DoubleProperty property) {
        return column(nameAlias).lessThanOrEq(property);
    }
}
