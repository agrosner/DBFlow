package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.structure.Model;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: The main, immutable property class that gets generated from a {@link Table} definition.
 * <p/>
 * This class delegates all of its {@link ITypeConditional} methods to a new {@link Condition} that's used
 * in the SQLite query language.
 * <p/>
 * This ensures that the language is strictly type-safe and only declared
 * columns get used. Also any calls on the methods return a new {@link Property}.
 * <p/>
 * This is type parametrized so that all values passed to this class remain proper.
 */
public class Property<T> extends BaseProperty<Property<T>> implements ITypeConditional<T> {

    public static final Property ALL_PROPERTY = new Property(null, "*") {
        @Override
        public String toString() {
            // don't tick the *
            return nameAlias.getAliasNameRaw();
        }
    };

    public Property(Class<? extends Model> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public Property(Class<? extends Model> table, String columnName) {
        super(table, null);
        if (columnName != null) {
            nameAlias = new NameAlias(columnName);
        }
    }

    Property(Class<? extends Model> table, String columnName, String aliasName, boolean shouldTickName, boolean shouldStripTicks) {
        this(table, new NameAlias(columnName, shouldStripTicks).as(aliasName).tickName(shouldTickName));
    }

    @Override
    public Property<T> as(String aliasName) {
        return new Property<>(table, getNameAlias().getAliasNameRaw(), aliasName,
                getNameAlias().shouldTickName(), getNameAlias().shouldStripTicks());
    }

    @Override
    public Property<T> distinct() {
        return new Property<>(table, getDistinctAliasName());
    }

    @Override
    public Property<T> withTable(NameAlias tableNameAlias) {
        return new Property<>(table, new NameAlias(getNameAlias()).withTable(tableNameAlias.getAliasName()));
    }

    @Override
    public Condition is(T value) {
        return column(getNameAlias()).is(value);
    }

    @Override
    public Condition eq(T value) {
        return column(getNameAlias()).eq(value);
    }

    @Override
    public Condition isNot(T value) {
        return column(getNameAlias()).isNot(value);
    }

    @Override
    public Condition notEq(T value) {
        return column(getNameAlias()).notEq(value);
    }

    @Override
    public Condition like(T value) {
        return column(getNameAlias()).like(value);
    }

    @Override
    public Condition glob(T value) {
        return column(getNameAlias()).glob(value);
    }

    @Override
    public Condition greaterThan(T value) {
        return column(getNameAlias()).greaterThan(value);
    }

    @Override
    public Condition greaterThanOrEq(T value) {
        return column(getNameAlias()).greaterThanOrEq(value);
    }

    @Override
    public Condition lessThan(T value) {
        return column(getNameAlias()).lessThan(value);
    }

    @Override
    public Condition lessThanOrEq(T value) {
        return column(getNameAlias()).lessThanOrEq(value);
    }

    @Override
    public Condition.Between between(T value) {
        return column(getNameAlias()).between(value);
    }

    @Override
    public Condition.In in(T firstValue, T... values) {
        return column(getNameAlias()).in(firstValue, values);
    }

    @Override
    public Condition.In notIn(T firstValue, T... values) {
        return column(getNameAlias()).notIn(firstValue, values);
    }

    @Override
    public Condition concatenate(T value) {
        return column(getNameAlias()).concatenate(value);
    }
}
