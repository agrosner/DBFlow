package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ITypeConditional;
import com.raizlabs.android.dbflow.sql.language.NameAlias2;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Collection;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

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

    public static final Property ALL_PROPERTY = new Property(null, "*") {
        @Override
        public String toString() {
            // don't tick the *
            return nameAlias.getQuery();
        }
    };

    public Property(Class<? extends Model> table, NameAlias2 nameAlias) {
        super(table, nameAlias);
    }

    public Property(Class<? extends Model> table, String columnName) {
        super(table, null);
        if (columnName != null) {
            nameAlias = new NameAlias2.Builder(columnName).build();
        }
    }

    Property(Class<? extends Model> table, String columnName, String aliasName, boolean shouldTickName, boolean shouldStripTicks) {
        this(table, new NameAlias2.Builder(columnName)
                .shouldStripIdentifier(shouldStripTicks)
                .as(aliasName).tickName(shouldTickName));
    }

    @Override
    public Property<T> plus(IProperty iProperty) {
        return new Property<>(table, NameAlias2.joinNames(Condition.Operation.PLUS,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> minus(IProperty iProperty) {
        return new Property<>(table, NameAlias2.joinNames(Condition.Operation.MINUS,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> dividedBy(IProperty iProperty) {
        return new Property<>(table, NameAlias2.joinNames(Condition.Operation.DIVISION,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> multipliedBy(IProperty iProperty) {
        return new Property<>(table, NameAlias2.joinNames(Condition.Operation.MULTIPLY,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> mod(IProperty iProperty) {
        return new Property<>(table, NameAlias2.joinNames(Condition.Operation.MOD,
                nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> concatenate(IProperty iProperty) {
        return new Property<>(table, NameAlias2.joinNames(Condition.Operation.CONCATENATE,
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
    public Property<T> withTable(NameAlias2 tableNameAlias) {
        return new Property<>(table, getNameAlias()
                .newBuilder()
                .withTable(tableNameAlias.getQuery())
                .build());
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
    public Condition like(String value) {
        return column(getNameAlias()).like(value);
    }

    @Override
    public Condition glob(String value) {
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
    public Condition.In in(Collection<T> values) {
        return column(getNameAlias()).in(values);
    }

    @Override
    public Condition.In notIn(Collection<T> values) {
        return column(getNameAlias()).notIn(values);
    }

    @Override
    public Condition concatenate(T value) {
        return column(getNameAlias()).concatenate(value);
    }
}
