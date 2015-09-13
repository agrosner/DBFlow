package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.structure.Model;

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

    public static final Property ALL_PROPERTY = new Property(null, "*") {
        @Override
        public String toString() {
            // don't tick the *
            return nameAlias.getAliasNameNoTicks();
        }
    };

    private final Class<? extends Model> table;
    protected final NameAlias nameAlias;


    public Property(Class<? extends Model> table, NameAlias nameAlias) {
        this.table = table;
        this.nameAlias = nameAlias;
    }

    public Property(Class<? extends Model> table, String columnName) {
        this(table, new NameAlias(columnName));
    }

    public Property(Class<? extends Model> table, String columnName, String aliasName) {
        this(table, new NameAlias(columnName, aliasName));
    }

    /**
     * @param aliasName The fileName of the alias.
     * @return A new {@link Property} that expresses the current column fileName with the specified Alias fileName.
     */
    public Property<T> as(String aliasName) {
        return new Property<>(table, nameAlias.getAliasNameNoTicks(), aliasName);
    }

    /**
     * @return A property appends DISTINCT to the property name. This is handy in {@link Method} queries.
     * This distinct {@link Property} can only be used with one column within a {@link Method}.
     */
    public Property<T> distinct() {
        return new Property<>(table, new NameAlias("DISTINCT " + nameAlias.getName(), nameAlias.getAliasNamePropertyNoTicks()).tickName(false));
    }

    /**
     * @return A property that represents the {@link Model} from which it belongs to. This is useful
     * in {@link Join} queries to represent this property. The resulting column name becomes a
     * tableName.columnName.
     */
    public Property<T> withTable() {
        return withTable(new NameAlias(FlowManager.getTableName(table)));
    }

    /**
     * @param tableNameAlias The name of the table to append. This may be different because of complex queries
     *                       that use a {@link NameAlias} for the table Name.
     * @return A property that represents the {@link Model} from which it belongs to. This is useful
     * in {@link Join} queries to represent this property. The resulting column name becomes a
     * tableName.columnName.
     */
    public Property<T> withTable(NameAlias tableNameAlias) {
        NameAlias alias = new NameAlias(tableNameAlias.getAliasName() + "." + nameAlias.getName(), nameAlias.getAliasName());
        alias.tickName(false);
        return new Property<>(table, alias);
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

    @Override
    public Condition is(IConditional<T> conditional) {
        return column(nameAlias).is(conditional);
    }

    @Override
    public Condition eq(IConditional<T> conditional) {
        return column(nameAlias).eq(conditional);
    }

    @Override
    public Condition isNot(IConditional<T> conditional) {
        return column(nameAlias).isNot(conditional);
    }

    @Override
    public Condition notEq(IConditional<T> conditional) {
        return column(nameAlias).notEq(conditional);
    }

    @Override
    public Condition like(IConditional<T> conditional) {
        return column(nameAlias).like(conditional);
    }

    @Override
    public Condition glob(IConditional<T> conditional) {
        return column(nameAlias).glob(conditional);
    }

    @Override
    public Condition greaterThan(IConditional<T> conditional) {
        return column(nameAlias).greaterThan(conditional);
    }

    @Override
    public Condition greaterThanOrEq(IConditional<T> conditional) {
        return column(nameAlias).greaterThanOrEq(conditional);
    }

    @Override
    public Condition lessThan(IConditional<T> conditional) {
        return column(nameAlias).lessThan(conditional);
    }

    @Override
    public Condition lessThanOrEq(IConditional<T> conditional) {
        return column(nameAlias).lessThanOrEq(conditional);
    }

    @Override
    public Condition.Between between(IConditional<T> conditional) {
        return column(nameAlias).between(conditional);
    }

    @Override
    public Condition.In in(IConditional<T> firstConditional, IConditional<T>... conditionals) {
        return column(nameAlias).in(firstConditional, conditionals);
    }

    @Override
    public Condition.In notIn(IConditional<T> firstConditional, IConditional<T>... conditionals) {
        return column(nameAlias).notIn(firstConditional, conditionals);
    }

    public Class<? extends Model> getTable() {
        return table;
    }
}
