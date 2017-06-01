package com.raizlabs.android.dbflow.sql.language.property;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.IConditional;
import com.raizlabs.android.dbflow.sql.language.IOperator;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.Operator;
import com.raizlabs.android.dbflow.sql.language.OrderBy;

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

    public static final Property<?> WILDCARD = new Property<Object>(null, NameAlias.rawBuilder("?").build());

    @Nullable
    final Class<?> table;

    protected NameAlias nameAlias;

    public Property(@Nullable Class<?> table, @NonNull NameAlias nameAlias) {
        this.table = table;
        this.nameAlias = nameAlias;
    }

    public Property(@Nullable Class<?> table, @Nullable String columnName) {
        this.table = table;
        if (columnName != null) {
            nameAlias = new NameAlias.Builder(columnName).build();
        }
    }

    public Property(@Nullable Class<?> table, @NonNull String columnName, @NonNull String aliasName) {
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

    @NonNull
    public String getDefinition() {
        return getNameAlias().getFullQuery();
    }

    @Override
    public String toString() {
        return getNameAlias().toString();
    }

    @NonNull
    @Override
    public Operator is(@NonNull IConditional conditional) {
        return getCondition().is(conditional);
    }

    @NonNull
    @Override
    public Operator eq(@NonNull IConditional conditional) {
        return getCondition().eq(conditional);
    }

    @NonNull
    @Override
    public Operator isNot(@NonNull IConditional conditional) {
        return getCondition().isNot(conditional);
    }

    @NonNull
    @Override
    public Operator notEq(@NonNull IConditional conditional) {
        return getCondition().notEq(conditional);
    }

    @NonNull
    @Override
    public Operator like(@NonNull IConditional conditional) {
        return getCondition().like(conditional);
    }

    @NonNull
    @Override
    public Operator glob(@NonNull IConditional conditional) {
        return getCondition().glob(conditional);
    }

    @NonNull
    @Override
    public Operator<T> like(@NonNull String value) {
        return getCondition().like(value);
    }

    @NonNull
    @Override
    public Operator<T> notLike(@NonNull String value) {
        return getCondition().notLike(value);
    }

    @NonNull
    @Override
    public Operator<T> glob(@NonNull String value) {
        return getCondition().glob(value);
    }

    @NonNull
    @Override
    public Operator greaterThan(@NonNull IConditional conditional) {
        return getCondition().greaterThan(conditional);
    }

    @NonNull
    @Override
    public Operator greaterThanOrEq(@NonNull IConditional conditional) {
        return getCondition().greaterThanOrEq(conditional);
    }

    @NonNull
    @Override
    public Operator lessThan(@NonNull IConditional conditional) {
        return getCondition().lessThan(conditional);
    }

    @NonNull
    @Override
    public Operator lessThanOrEq(@NonNull IConditional conditional) {
        return getCondition().lessThanOrEq(conditional);
    }

    @NonNull
    @Override
    public Operator.Between between(@NonNull IConditional conditional) {
        return getCondition().between(conditional);
    }

    @NonNull
    @Override
    public Operator.In in(@NonNull IConditional firstConditional, @NonNull IConditional... conditionals) {
        return getCondition().in(firstConditional, conditionals);
    }

    @NonNull
    @Override
    public Operator.In notIn(@NonNull IConditional firstConditional, @NonNull IConditional... conditionals) {
        return getCondition().notIn(firstConditional, conditionals);
    }

    @NonNull
    @Override
    public Operator is(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().is(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator isNull() {
        return getCondition().isNull();
    }

    @NonNull
    @Override
    public Operator eq(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().eq(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator isNot(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().isNot(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator isNotNull() {
        return getCondition().isNotNull();
    }

    @NonNull
    @Override
    public Operator notEq(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().notEq(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator like(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().like(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator notLike(@NonNull IConditional conditional) {
        return getCondition().notLike(conditional);
    }

    @NonNull
    @Override
    public Operator notLike(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().notLike(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator glob(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().glob(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator greaterThan(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().greaterThan(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator greaterThanOrEq(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().greaterThanOrEq(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator lessThan(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().lessThan(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator lessThanOrEq(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().lessThanOrEq(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator.Between between(@NonNull BaseModelQueriable baseModelQueriable) {
        return getCondition().between(baseModelQueriable);
    }

    @NonNull
    @Override
    public Operator.In in(@NonNull BaseModelQueriable firstBaseModelQueriable, @NonNull BaseModelQueriable... baseModelQueriables) {
        return getCondition().in(firstBaseModelQueriable, baseModelQueriables);
    }

    @NonNull
    @Override
    public Operator.In notIn(@NonNull BaseModelQueriable firstBaseModelQueriable, @NonNull BaseModelQueriable... baseModelQueriables) {
        return getCondition().notIn(firstBaseModelQueriable, baseModelQueriables);
    }

    @NonNull
    @Override
    public Operator concatenate(@NonNull IConditional conditional) {
        return getCondition().concatenate(conditional);
    }

    @NonNull
    @Override
    public Operator plus(@NonNull BaseModelQueriable value) {
        return getCondition().plus(value);
    }

    @NonNull
    @Override
    public Operator minus(@NonNull BaseModelQueriable value) {
        return getCondition().minus(value);
    }

    @NonNull
    @Override
    public Operator div(@NonNull BaseModelQueriable value) {
        return getCondition().div(value);
    }

    @NonNull
    @Override
    public Operator times(@NonNull BaseModelQueriable value) {
        return getCondition().times(value);
    }

    @NonNull
    @Override
    public Operator rem(@NonNull BaseModelQueriable value) {
        return getCondition().rem(value);
    }

    @NonNull
    @Override
    public Class<?> getTable() {
        return table;
    }

    @NonNull
    @Override
    public Property<T> plus(@NonNull IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.PLUS,
            nameAlias.fullName(), iProperty.toString()));
    }

    @NonNull
    @Override
    public Property<T> minus(@NonNull IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.MINUS,
            nameAlias.fullName(), iProperty.toString()));
    }

    @NonNull
    @Override
    public Property<T> div(@NonNull IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.DIVISION,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public Property<T> times(@NonNull IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.MULTIPLY,
            nameAlias.fullName(), iProperty.toString()));
    }

    @NonNull
    @Override
    public Property<T> rem(@NonNull IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.MOD,
            nameAlias.fullName(), iProperty.toString()));
    }

    @NonNull
    @Override
    public Property<T> concatenate(@NonNull IProperty iProperty) {
        return new Property<>(table, NameAlias.joinNames(Operator.Operation.CONCATENATE,
            nameAlias.fullName(), iProperty.toString()));
    }

    @NonNull
    @Override
    public Property<T> as(@NonNull String aliasName) {
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
    public Property<T> withTable(@NonNull NameAlias tableNameAlias) {
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
    public Operator<T> greaterThan(@NonNull T value) {
        return getCondition().greaterThan(value);
    }

    @NonNull
    @Override
    public Operator<T> greaterThanOrEq(@NonNull T value) {
        return getCondition().greaterThanOrEq(value);
    }

    @NonNull
    @Override
    public Operator<T> lessThan(@NonNull T value) {
        return getCondition().lessThan(value);
    }

    @NonNull
    @Override
    public Operator<T> lessThanOrEq(@NonNull T value) {
        return getCondition().lessThanOrEq(value);
    }

    @NonNull
    @Override
    public Operator.Between<T> between(@NonNull T value) {
        return getCondition().between(value);
    }

    @NonNull
    @SuppressWarnings({"ConfusingArgumentToVarargsMethod", "unchecked"})
    @Override
    public Operator.In<T> in(@NonNull T firstValue, T... values) {
        return getCondition().in(firstValue, values);
    }

    @NonNull
    @SuppressWarnings({"ConfusingArgumentToVarargsMethod", "unchecked"})
    @Override
    public Operator.In<T> notIn(@NonNull T firstValue, T... values) {
        return getCondition().notIn(firstValue, values);
    }

    @NonNull
    @Override
    public Operator.In<T> in(@NonNull Collection<T> values) {
        return getCondition().in(values);
    }

    @NonNull
    @Override
    public Operator.In<T> notIn(@NonNull Collection<T> values) {
        return getCondition().notIn(values);
    }

    @NonNull
    @Override
    public Operator<T> concatenate(T value) {
        return getCondition().concatenate(value);
    }

    @NonNull
    @Override
    public Operator<T> plus(@NonNull T value) {
        return getCondition().plus(value);
    }

    @NonNull
    @Override
    public Operator<T> minus(@NonNull T value) {
        return getCondition().minus(value);
    }

    @NonNull
    @Override
    public Operator<T> div(@NonNull T value) {
        return getCondition().div(value);
    }

    @Override
    public Operator<T> times(@NonNull T value) {
        return getCondition().times(value);
    }

    @NonNull
    @Override
    public Operator<T> rem(@NonNull T value) {
        return getCondition().rem(value);
    }


    @Override
    @NonNull
    public OrderBy asc() {
        return OrderBy.fromProperty(this).ascending();
    }

    @Override
    @NonNull
    public OrderBy desc() {
        return OrderBy.fromProperty(this).descending();
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

    @NonNull
    protected Operator<T> getCondition() {
        return op(getNameAlias());
    }
}
