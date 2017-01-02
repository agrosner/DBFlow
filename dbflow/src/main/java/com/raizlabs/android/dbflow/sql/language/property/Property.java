package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias;

import java.util.Collection;

/**
 * Description: The main, immutable property class that gets generated from a table definition.
 * <p/>
 * This class delegates all of its {@link ITypeConditional} methods to a new {@link Condition} that's used
 * in the SQLite query language.
 * <p/>
 * This ensures that the language is strictly type-safe and only declared
 * columns get used. Also any calls on the methods return a new {@link Property}.
 * <p/>
 * This is type parametrized so that all values passed to this class remain properly typed.
 */
public class Property<T> extends BaseProperty<Property<T>> implements ITypeConditional<T> {

    public static final Property<?> ALL_PROPERTY = new Property<Object>(null, "*") {
        @Override
        public String toString() {
            // don't tick the *
            return nameAlias.nameRaw();
        }
    };

    public Property(Class<?> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public Property(Class<?> table, String columnName) {
        super(table, null);
        if (columnName != null) {
            nameAlias = new NameAlias.Builder(columnName).build();
        }
    }

    @Override
    public Property<T> plus(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Condition.Operation.PLUS,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> minus(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Condition.Operation.MINUS,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> dividedBy(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Condition.Operation.DIVISION,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> multipliedBy(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Condition.Operation.MULTIPLY,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> mod(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Condition.Operation.MOD,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> concatenate(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Condition.Operation.CONCATENATE,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> as(String aliasName) {
        return new Property<>(table, getNameAlias()
                .newBuilder()
                .as(aliasName)
                .build());
    }

    @Override
    public Property<T> distinct() {
        return new Property<>(table, getDistinctAliasName());
    }

    @Override
    public Property<T> withTable(NameAlias tableNameAlias) {
        return new Property<>(table, getNameAlias()
                .newBuilder()
                .withTable(tableNameAlias.getQuery())
                .build());
    }

    @Override
    public Condition is(T value) {
        return getCondition().is(value);
    }

    @Override
    public Condition eq(T value) {
        return getCondition().eq(value);
    }

    @Override
    public Condition isNot(T value) {
        return getCondition().isNot(value);
    }

    @Override
    public Condition notEq(T value) {
        return getCondition().notEq(value);
    }

    @Override
    public Condition greaterThan(T value) {
        return getCondition().greaterThan(value);
    }

    @Override
    public Condition greaterThanOrEq(T value) {
        return getCondition().greaterThanOrEq(value);
    }

    @Override
    public Condition lessThan(T value) {
        return getCondition().lessThan(value);
    }

    @Override
    public Condition lessThanOrEq(T value) {
        return getCondition().lessThanOrEq(value);
    }

    @Override
    public Condition.Between between(T value) {
        return getCondition().between(value);
    }

    @SuppressWarnings({"ConfusingArgumentToVarargsMethod", "unchecked"})
    @Override
    public Condition.In in(T firstValue, T... values) {
        return getCondition().in(firstValue, values);
    }

    @SuppressWarnings({"ConfusingArgumentToVarargsMethod", "unchecked"})
    @Override
    public Condition.In notIn(T firstValue, T... values) {
        return getCondition().notIn(firstValue, values);
    }

    @Override
    public Condition.In in(Collection<T> values) {
        return getCondition().in(values);
    }

    @Override
    public Condition.In notIn(Collection<T> values) {
        return getCondition().notIn(values);
    }

    @Override
    public Condition concatenate(T value) {
        return getCondition().concatenate(value);
    }
}
