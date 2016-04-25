package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.structure.Model;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description:
 */
public class CharProperty extends BaseProperty<CharProperty> {

    public CharProperty(Class<? extends Model> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public CharProperty(Class<? extends Model> table, String columnName) {
        this(table, new NameAlias(columnName));
    }

    public CharProperty(Class<? extends Model> table, String columnName, String aliasName) {
        this(table, new NameAlias(columnName, aliasName));
    }

    @Override
    public CharProperty plus(IProperty iProperty) {
        return new CharProperty(table, NameAlias.joinNames(Condition.Operation.PLUS,
                nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public CharProperty minus(IProperty iProperty) {
        return new CharProperty(table, NameAlias.joinNames(Condition.Operation.MINUS,
                nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public CharProperty dividedBy(IProperty iProperty) {
        return new CharProperty(table, NameAlias.joinNames(Condition.Operation.DIVISION,
                nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public CharProperty multipliedBy(IProperty iProperty) {
        return new CharProperty(table, NameAlias.joinNames(Condition.Operation.MULTIPLY,
                nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public CharProperty mod(IProperty iProperty) {
        return new CharProperty(table, NameAlias.joinNames(Condition.Operation.MOD,
                nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public CharProperty concat(IProperty iProperty) {
        return new CharProperty(table, NameAlias.joinNames(Condition.Operation.CONCATENATE,
            nameAlias.getName(), iProperty.toString()));
    }

    @Override
    public CharProperty as(String aliasName) {
        return new CharProperty(table, nameAlias.getAliasNameRaw(), aliasName);
    }

    @Override
    public CharProperty distinct() {
        return new CharProperty(table, getDistinctAliasName());
    }

    @Override
    public CharProperty withTable(NameAlias tableNameAlias) {
        return new CharProperty(table, new NameAlias(nameAlias).withTable(tableNameAlias.getAliasName()));
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

    public Condition is(CharProperty property) {
        return column(nameAlias).is(property);
    }

    public Condition isNot(CharProperty property) {
        return column(nameAlias).isNot(property);
    }

    public Condition eq(CharProperty property) {
        return is(property);
    }

    public Condition notEq(CharProperty property) {
        return isNot(property);
    }

    public Condition greaterThan(CharProperty property) {
        return column(nameAlias).greaterThan(property);
    }

    public Condition greaterThanOrEq(CharProperty property) {
        return column(nameAlias).greaterThanOrEq(property);
    }

    public Condition lessThan(CharProperty property) {
        return column(nameAlias).lessThan(property);
    }

    public Condition lessThanOrEq(CharProperty property) {
        return column(nameAlias).lessThanOrEq(property);
    }
}
