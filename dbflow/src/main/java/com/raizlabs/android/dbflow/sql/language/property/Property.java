package com.raizlabs.android.dbflow.sql.language.property;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.IConditional;
import com.raizlabs.android.dbflow.sql.language.IOperator;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.Operator;

import java.util.Collection;

import static com.raizlabs.android.dbflow.sql.language.Operator.op;

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

    @NonNull
    @Override
    public Property<T> withTable() {
        return withTable(new NameAlias.Builder(FlowManager.getTableName(table)).build());
    }

    @NonNull
    @Override
    public NameAlias getNameAlias() {
        return nameAlias;
    }

    @Override
    public String getQuery() {
        return getNameAlias().getQuery();
    }

    @NonNull
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

    @NonNull
    @Override
    public Operator is(IConditional conditional) {
        return getCondition().is(conditional);
    }

    @NonNull
    @Override
    public Operator eq(IConditional conditional) {
        return getCondition().eq(conditional);
    }

    @NonNull
    @Override
    public Operator isNot(IConditional conditional) {
        return getCondition().isNot(conditional);
    }

    @NonNull
    @Override
    public Operator notEq(IConditional conditional) {
        return getCondition().notEq(conditional);
    }

    @NonNull
    @Override
    public Operator like(IConditional conditional) {
        return getCondition().like(conditional);
    }

    @NonNull
    @Override
    public Operator glob(IConditional conditional) {
        return getCondition().glob(conditional);
    }

    @NonNull
    @Override
    public Operator<T> like(String value) {
        return getCondition().like(value);
    }

    @NonNull
    @Override
    public Operator<T> notLike(String value) {
        return getCondition().notLike(value);
    }

    @NonNull
    @Override
    public Operator<T> glob(String value) {
        return getCondition().glob(value);
    }

    @NonNull
    @Override
    public Operator greaterThan(IConditional conditional) {
        return getCondition().greaterThan(conditional);
    }

    @NonNull
    @Override
    public Operator greaterThanOrEq(IConditional conditional) {
        return getCondition().greaterThanOrEq(conditional);
    }

    @NonNull
    @Override
    public Operator lessThan(IConditional conditional) {
        return getCondition().lessThan(conditional);
    }

    @NonNull
    @Override
    public Operator lessThanOrEq(IConditional conditional) {
        return getCondition().lessThanOrEq(conditional);
    }

    @NonNull
    @Override
    public Operator.Between between(IConditional conditional) {
        return getCondition().between(conditional);
    }

    @NonNull
    @Override
    public Operator.In in(IConditional firstConditional, IConditional... conditionals) {
        return getCondition().in(firstConditional, conditionals);
    }

    @NonNull
    @Override
    public Operator.In notIn(IConditional firstConditional, IConditional... conditionals) {
        return getCondition().notIn(firstConditional, conditionals);
    }

    @NonNull
    @Override
    public Operator is(BaseModelQueriable baseModelQueriable) {
        return getCondition().is(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator isNull() {
        return getCondition().isNull();
    }

    @NonNull
    @Override
    public Operator eq(BaseModelQueriable baseModelQueriable) {
        return getCondition().eq(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator isNot(BaseModelQueriable baseModelQueriable) {
        return getCondition().isNot(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator isNotNull() {
        return getCondition().isNotNull();
    }

    @NonNull
    @Override
    public Operator notEq(BaseModelQueriable baseModelQueriable) {
        return getCondition().notEq(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator like(BaseModelQueriable baseModelQueriable) {
        return getCondition().like(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator glob(BaseModelQueriable baseModelQueriable) {
        return getCondition().glob(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator greaterThan(BaseModelQueriable baseModelQueriable) {
        return getCondition().greaterThan(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator greaterThanOrEq(BaseModelQueriable baseModelQueriable) {
        return getCondition().greaterThanOrEq(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator lessThan(BaseModelQueriable baseModelQueriable) {
        return getCondition().lessThan(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator lessThanOrEq(BaseModelQueriable baseModelQueriable) {
        return getCondition().lessThanOrEq(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator.Between between(BaseModelQueriable baseModelQueriable) {
        return getCondition().between(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator.In in(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables) {
        return getCondition().in(firstBaseModelQueriable, baseModelQueriables);
    }

    @NonNull
    @Override
    public Operator.In notIn(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables) {
        return getCondition().notIn(firstBaseModelQueriable, baseModelQueriables);
    }

    @NonNull
    @Override
    public Operator concatenate(IConditional conditional) {
        return getCondition().concatenate(conditional);
    }

    @NonNull
    @Override
    public Operator plus(IConditional value) {
        return getCondition().plus(value);
    }

    @NonNull
    @Override
    public Operator minus(IConditional value) {
        return getCondition().minus(value);
    }

    @NonNull
    @Override
    public Operator div(IConditional value) {
        return getCondition().div(value);
    }

    @NonNull
    @Override
    public Operator times(IConditional value) {
        return getCondition().times(value);
    }

    @NonNull
    @Override
    public Operator rem(IConditional value) {
        return getCondition().rem(value);
    }

    @NonNull
    @Override
    public Operator plus(BaseModelQueriable value) {
        return getCondition().plus(value);
    }

    @NonNull
    @Override
    public Operator minus(BaseModelQueriable value) {
        return getCondition().minus(value);
    }

    @NonNull
    @Override
    public Operator div(BaseModelQueriable value) {
        return getCondition().div(value);
    }

    @NonNull
    @Override
    public Operator times(BaseModelQueriable value) {
        return getCondition().times(value);
    }

    @NonNull
    @Override
    public Operator rem(BaseModelQueriable value) {
        return getCondition().rem(value);
    }

    @NonNull
    @Override
    public Class<?> getTable() {
        return table;
    }

    @NonNull
    @Override
    public Property<T> plus(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.PLUS,
            nameAlias.fullName(), iProperty.toString()));
    }

    @NonNull
    @Override
    public Property<T> minus(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.MINUS,
            nameAlias.fullName(), iProperty.toString()));
    }

    @NonNull
    @Override
    public Property<T> div(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.DIVISION,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> times(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.MULTIPLY,
            nameAlias.fullName(), iProperty.toString()));
    }

    @NonNull
    @Override
    public Property<T> rem(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.MOD,
            nameAlias.fullName(), iProperty.toString()));
    }

    @NonNull
    @Override
    public Property<T> concatenate(IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.CONCATENATE,
            nameAlias.fullName(), iProperty.toString()));
    }

    @NonNull
    @Override
    public Property<T> as(String aliasName) {
        return new Property<>(table, getNameAlias()
            .newBuilder()
            .as(aliasName)
            .build());
    }

    @NonNull
    @Override
    public Property<T> distinct() {
        return new Property<>(table, getDistinctAliasName());
    }

    @NonNull
    @Override
    public Property<T> withTable(NameAlias tableNameAlias) {
        return new Property<>(table, getNameAlias()
            .newBuilder()
            .withTable(tableNameAlias.getQuery())
            .build());
    }

    @NonNull
    @Override
    public Operator<T> is(T value) {
        return getCondition().is(value);
    }

    @NonNull
    @Override
    public Operator<T> eq(T value) {
        return getCondition().eq(value);
    }

    @NonNull
    @Override
    public Operator<T> isNot(T value) {
        return getCondition().isNot(value);
    }

    @NonNull
    @Override
    public Operator<T> notEq(T value) {
        return getCondition().notEq(value);
    }

    @NonNull
    @Override
    public Operator<T> greaterThan(T value) {
        return getCondition().greaterThan(value);
    }

    @NonNull
    @Override
    public Operator<T> greaterThanOrEq(T value) {
        return getCondition().greaterThanOrEq(value);
    }

    @NonNull
    @Override
    public Operator<T> lessThan(T value) {
        return getCondition().lessThan(value);
    }

    @NonNull
    @Override
    public Operator<T> lessThanOrEq(T value) {
        return getCondition().lessThanOrEq(value);
    }

    @NonNull
    @Override
    public Operator.Between<T> between(T value) {
        return getCondition().between(value);
    }

    @NonNull
    @SuppressWarnings({"ConfusingArgumentToVarargsMethod", "unchecked"})
    @Override
    public Operator.In<T> in(T firstValue, T... values) {
        return getCondition().in(firstValue, values);
    }

    @NonNull
    @SuppressWarnings({"ConfusingArgumentToVarargsMethod", "unchecked"})
    @Override
    public Operator.In<T> notIn(T firstValue, T... values) {
        return getCondition().notIn(firstValue, values);
    }

    @NonNull
    @Override
    public Operator.In<T> in(Collection<T> values) {
        return getCondition().in(values);
    }

    @NonNull
    @Override
    public Operator.In<T> notIn(Collection<T> values) {
        return getCondition().notIn(values);
    }

    @NonNull
    @Override
    public Operator<T> concatenate(T value) {
        return getCondition().concatenate(value);
    }

    @NonNull
    @Override
    public Operator<T> plus(T value) {
        return getCondition().plus(value);
    }

    @NonNull
    @Override
    public Operator<T> minus(T value) {
        return getCondition().minus(value);
    }

    @NonNull
    @Override
    public Operator<T> div(T value) {
        return getCondition().div(value);
    }

    @Override
    public Operator<T> times(T value) {
        return getCondition().times(value);
    }

    @NonNull
    @Override
    public Operator<T> rem(T value) {
        return getCondition().rem(value);
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
        return op(getNameAlias());
    }
}
