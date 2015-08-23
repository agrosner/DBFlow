package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: The main, immutable property class that gets generated from a {@link Table} definition.
 * <p>
 * This class delegates all of its {@link IConditional} methods to a new {@link Condition} that's used
 * in the SQLite query language.
 * <p>
 * This ensures that the language is strictly type-safe and only declared
 * columns get used. Also any calls on the methods return a new {@link Property}.
 * <p>
 * This is type parametrized so that all values passed to this class remain proper.
 */
public class Property<T> implements IConditional<T>, Query {

    public static final Property ALL_PROPERTY = new Property("*") {
        @Override
        public String toString() {
            // don't tick the *
            return nameAlias.getAliasNameNoTicks();
        }
    };

    protected final NameAlias nameAlias;

    public Property(NameAlias nameAlias) {
        this.nameAlias = nameAlias;
    }

    public Property(String columnName) {
        nameAlias = new NameAlias(columnName);
    }

    public Property(String columnName, String aliasName) {
        nameAlias = new NameAlias(columnName, aliasName);
    }

    /**
     * @param aliasName The fileName of the alias.
     * @return A new {@link Property} that expresses the current column fileName with the specified Alias fileName.
     */
    public Property as(String aliasName) {
        return new Property(nameAlias.getAliasNameNoTicks(), aliasName);
    }

    /**
     * @return A property appends DISTINCT to the property name. This is handy in {@link Method} queries.
     * This distinct {@link Property} can only be used with one column within a {@link Method}.
     */
    public Property distinct() {
        return new Property(new NameAlias("DISTINCT " + nameAlias.getName(), nameAlias.getAliasNamePropertyNoTicks()).tickName(false));
    }

    public String getDefinition() {
        return nameAlias.getDefinition();
    }

    @Override
    public String toString() {
        return nameAlias.toString();
    }

    @Override
    public String getQuery() {
        return nameAlias.getQuery();
    }

    @Override
    public Condition is(T value) {
        return column(nameAlias).is(value);
    }

    @Override
    public Condition eq(T value) {
        return column(nameAlias).eq(value);
    }

    @Override
    public Condition isNot(T value) {
        return column(nameAlias).isNot(value);
    }

    @Override
    public Condition notEq(T value) {
        return column(nameAlias).notEq(value);
    }

    @Override
    public Condition like(T value) {
        return column(nameAlias).like(value);
    }

    @Override
    public Condition glob(T value) {
        return column(nameAlias).glob(value);
    }

    @Override
    public Condition greaterThan(T value) {
        return column(nameAlias).greaterThan(value);
    }

    @Override
    public Condition greaterThanOrEq(T value) {
        return column(nameAlias).greaterThanOrEq(value);
    }

    @Override
    public Condition lessThan(T value) {
        return column(nameAlias).lessThan(value);
    }

    @Override
    public Condition lessThanOrEq(T value) {
        return column(nameAlias).lessThanOrEq(value);
    }

    @Override
    public Condition.Between between(T value) {
        return column(nameAlias).between(value);
    }

    @Override
    public Condition.In in(T firstValue, T... values) {
        return column(nameAlias).in(firstValue, values);
    }

    @Override
    public Condition.In notIn(T firstValue, T... values) {
        return column(nameAlias).notIn(firstValue, values);
    }
}
