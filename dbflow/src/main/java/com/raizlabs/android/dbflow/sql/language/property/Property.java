package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.IConditional;
import com.raizlabs.android.dbflow.sql.language.IOperator;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.Operator;

import java.util.Collection;

import static com.raizlabs.android.dbflow.sql.language.Operator.column;

/**
 * Description: The main, immutable property class that gets generated from a table definition.
 * <p/>
 * This class delegates all of its {@link IOperator} methods to a new {@link Operator} that's used
 * in the SQLite query language.
 * <p/>
 * This ensures that the language is strictly type-safe and only declared
 * columns get used. Also any calls on the methods return a new {@link Property}.
 * <p/>
 * This is type parametrized so that all values passed to this class remain properly typed.
 */
public class Property<T> implements IProperty<Property<T>>, IConditional, IOperator<T> {

    public static final Property<?> ALL_PROPERTY = new Property<Object>(null, "*") {
        @Override
        public String toString() {
            // don't tick the *
            return nameAlias.nameRaw();
        }
    };

    final Class<?> table;
    protected NameAlias nameAlias;

    public Property(Class<?> table, NameAlias nameAlias) {
        this.table = table;
        this.nameAlias = nameAlias;
    }

    public Property(Class<?> table, String columnName) {
        this.table = table;
        if (columnName != null) {
            nameAlias = new NameAlias.Builder(columnName).build();
        }
    }

    public Property(Class<?> table, String columnName, String aliasName) {
        this(table, NameAlias.builder(columnName).as(aliasName).build());
    }

    @Override
    public Property<T> withTable() {
        return withTable(new NameAlias.Builder(FlowManager.getTableName(table)).build());
    }

    @Override
    public NameAlias getNameAlias() {
        return nameAlias;
    }

    @Override
    public String getQuery() {
        return getNameAlias().getQuery();
    }

    @Override
    public String getCursorKey() {
        return getNameAlias().getQuery();
    }

    public String getDefinition() {
        return getNameAlias().getFullQuery();
    }

    @Override
    public String toString() {
        return getNameAlias().toString();
    }

    @Override
    public Operator is(IConditional conditional) {
        return getCondition().is(conditional);
    }

    @Override
    public Operator eq(IConditional conditional) {
        return getCondition().eq(conditional);
    }

    @Override
    public Operator isNot(IConditional conditional) {
        return getCondition().isNot(conditional);
    }

    @Override
    public Operator notEq(IConditional conditional) {
        return getCondition().notEq(conditional);
    }

    @Override
    public Operator like(IConditional conditional) {
        return getCondition().like(conditional);
    }

    @Override
    public Operator glob(IConditional conditional) {
        return getCondition().glob(conditional);
    }

    @Override
    public Operator<T> like(String value) {
        return getCondition().like(value);
    }

    @Override
    public Operator<T> notLike(String value) {
        return getCondition().notLike(value);
    }

    @Override
    public Operator<T> glob(String value) {
        return getCondition().glob(value);
    }

    @Override
    public Operator greaterThan(IConditional conditional) {
        return getCondition().greaterThan(conditional);
    }

    @Override
    public Operator greaterThanOrEq(IConditional conditional) {
        return getCondition().greaterThanOrEq(conditional);
    }

    @Override
    public Operator lessThan(IConditional conditional) {
        return getCondition().lessThan(conditional);
    }

    @Override
    public Operator lessThanOrEq(IConditional conditional) {
        return getCondition().lessThanOrEq(conditional);
    }

    @Override
    public Operator.Between between(IConditional conditional) {
        return getCondition().between(conditional);
    }

    @Override
    public Operator.In in(IConditional firstConditional, IConditional... conditionals) {
        return getCondition().in(firstConditional, conditionals);
    }

    @Override
    public Operator.In notIn(IConditional firstConditional, IConditional... conditionals) {
        return getCondition().notIn(firstConditional, conditionals);
    }

    @Override
    public Operator is(BaseModelQueriable baseModelQueriable) {
        return getCondition().is(baseModelQueriable);
    }

    @Override
    public Operator isNull() {
        return getCondition().isNull();
    }

    @Override
    public Operator eq(BaseModelQueriable baseModelQueriable) {
        return getCondition().eq(baseModelQueriable);
    }

    @Override
    public Operator isNot(BaseModelQueriable baseModelQueriable) {
        return getCondition().isNot(baseModelQueriable);
    }

    @Override
    public Operator isNotNull() {
        return getCondition().isNotNull();
    }

    @Override
    public Operator notEq(BaseModelQueriable baseModelQueriable) {
        return getCondition().notEq(baseModelQueriable);
    }

    @Override
    public Operator like(BaseModelQueriable baseModelQueriable) {
        return getCondition().like(baseModelQueriable);
    }

    @Override
    public Operator glob(BaseModelQueriable baseModelQueriable) {
        return getCondition().glob(baseModelQueriable);
    }

    @Override
    public Operator greaterThan(BaseModelQueriable baseModelQueriable) {
        return getCondition().greaterThan(baseModelQueriable);
    }

    @Override
    public Operator greaterThanOrEq(BaseModelQueriable baseModelQueriable) {
        return getCondition().greaterThanOrEq(baseModelQueriable);
    }

    @Override
    public Operator lessThan(BaseModelQueriable baseModelQueriable) {
        return getCondition().lessThan(baseModelQueriable);
    }

    @Override
    public Operator lessThanOrEq(BaseModelQueriable baseModelQueriable) {
        return getCondition().lessThanOrEq(baseModelQueriable);
    }

    @Override
    public Operator.Between between(BaseModelQueriable baseModelQueriable) {
        return getCondition().between(baseModelQueriable);
    }

    @Override
    public Operator.In in(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables) {
        return getCondition().in(firstBaseModelQueriable, baseModelQueriables);
    }

    @Override
    public Operator.In notIn(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables) {
        return getCondition().notIn(firstBaseModelQueriable, baseModelQueriables);
    }

    @Override
    public Operator concatenate(IConditional conditional) {
        return getCondition().concatenate(conditional);
    }

    @Override
    public Class<?> getTable() {
        return table;
    }

    @Override
    public Property<T> plus(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.PLUS,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> minus(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.MINUS,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> dividedBy(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.DIVISION,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> multipliedBy(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.MULTIPLY,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> mod(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.MOD,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> concatenate(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.CONCATENATE,
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
    public Operator<T> is(T value) {
        return getCondition().is(value);
    }

    @Override
    public Operator<T> eq(T value) {
        return getCondition().eq(value);
    }

    @Override
    public Operator<T> isNot(T value) {
        return getCondition().isNot(value);
    }

    @Override
    public Operator<T> notEq(T value) {
        return getCondition().notEq(value);
    }

    @Override
    public Operator<T> greaterThan(T value) {
        return getCondition().greaterThan(value);
    }

    @Override
    public Operator<T> greaterThanOrEq(T value) {
        return getCondition().greaterThanOrEq(value);
    }

    @Override
    public Operator<T> lessThan(T value) {
        return getCondition().lessThan(value);
    }

    @Override
    public Operator<T> lessThanOrEq(T value) {
        return getCondition().lessThanOrEq(value);
    }

    @Override
    public Operator.Between<T> between(T value) {
        return getCondition().between(value);
    }

    @SuppressWarnings({"ConfusingArgumentToVarargsMethod", "unchecked"})
    @Override
    public Operator.In<T> in(T firstValue, T... values) {
        return getCondition().in(firstValue, values);
    }

    @SuppressWarnings({"ConfusingArgumentToVarargsMethod", "unchecked"})
    @Override
    public Operator.In<T> notIn(T firstValue, T... values) {
        return getCondition().notIn(firstValue, values);
    }

    @Override
    public Operator.In<T> in(Collection<T> values) {
        return getCondition().in(values);
    }

    @Override
    public Operator.In<T> notIn(Collection<T> values) {
        return getCondition().notIn(values);
    }

    @Override
    public Operator<T> concatenate(T value) {
        return getCondition().concatenate(value);
    }


    /**
     * @return helper method to construct it in a {@link #distinct()} call.
     */
    protected NameAlias getDistinctAliasName() {
        return getNameAlias()
            .newBuilder()
            .distinct()
            .build();
    }

    protected Operator<T> getCondition() {
        return column(getNameAlias());
    }
}
