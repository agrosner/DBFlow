package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.PropertyFactory;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description: Specifies a SQLite JOIN statement
 */
public class Join<TModel, TFromModel> implements Query {

    /**
     * The specific type of JOIN that is used.
     */
    public enum JoinType {

        /**
         * an extension of the INNER JOIN. Though SQL standard defines three types of OUTER JOINs: LEFT, RIGHT,
         * and FULL but SQLite only supports the LEFT OUTER JOIN.
         * <p/>
         * The OUTER JOINs have a condition that is identical to INNER JOINs, expressed using an ON, USING, or NATURAL keyword.
         * The initial results table is calculated the same way. Once the primary JOIN is calculated,
         * an OUTER join will take any unjoined rows from one or both tables, pad them out with NULLs,
         * and append them to the resulting table.
         */
        LEFT_OUTER,

        /**
         * creates a new result table by combining column values of two tables (table1 and table2) based upon the join-predicate.
         * The query compares each row of table1 with each row of table2 to find all pairs of rows which satisfy the join-predicate.
         * When the join-predicate is satisfied, column values for each matched pair of rows of A and B are combined into a result row
         */
        INNER,

        /**
         * matches every row of the first table with every row of the second table. If the input tables
         * have x and y columns, respectively, the resulting table will have x*y columns.
         * Because CROSS JOINs have the potential to generate extremely large tables,
         * care must be taken to only use them when appropriate.
         */
        CROSS,

        /**
         * a join that performs the same task as an INNER or LEFT JOIN, in which the ON or USING
         * clause refers to all columns that the tables to be joined have in common.
         */
        NATURAL
    }

    private final Class<TModel> table;
    /**
     * The type of JOIN to use
     */
    private JoinType type;

    /**
     * The FROM statement that prefixes this statement.
     */
    private From<TFromModel> from;

    /**
     * The alias to name the JOIN
     */
    private NameAlias alias;

    /**
     * The ON conditions
     */
    private OperatorGroup onGroup;

    /**
     * What columns to use.
     */
    private List<IProperty> using = new ArrayList<>();

    public Join(@NonNull From<TFromModel> from, @NonNull Class<TModel> table, @NonNull JoinType joinType) {
        this.from = from;
        this.table = table;
        type = joinType;
        NameAlias.Builder builder = new NameAlias.Builder(FlowManager.getTableName(table));
        String tableAlias = FlowManager.getModelAdapter(table).getTableAlias().trim();
        if (tableAlias.length() > 0) {
            builder.as(tableAlias);
        }
        alias = builder.build();
    }

    public Join(@NonNull From<TFromModel> from, @NonNull JoinType joinType,
                @NonNull ModelQueriable<TModel> modelQueriable) {
        table = modelQueriable.getTable();
        this.from = from;
        type = joinType;
        alias = PropertyFactory.from(modelQueriable).getNameAlias();
    }

    /**
     * Specifies if the JOIN has a name it should be called.
     *
     * @param alias The name to give it
     * @return This instance
     */
    @NonNull
    public Join<TModel, TFromModel> as(@NonNull String alias) {
        this.alias = this.alias
            .newBuilder()
            .as(alias)
            .build();
        return this;
    }

    /**
     * Specify the conditions that the JOIN is on
     *
     * @param onConditions The conditions it is on
     * @return The FROM that this JOIN came from
     */
    @NonNull
    public From<TFromModel> on(SQLOperator... onConditions) {
        checkNatural();
        onGroup = OperatorGroup.nonGroupingClause();
        onGroup.andAll(onConditions);
        return from;
    }

    /**
     * The USING statement of the JOIN
     *
     * @param columns THe columns to use
     * @return The FROM that this JOIN came from
     */
    @NonNull
    public From<TFromModel> using(IProperty... columns) {
        checkNatural();
        Collections.addAll(using, columns);
        return from;
    }

    /**
     * @return End this {@link Join}. Used for {@link Join.JoinType#NATURAL}
     */
    public From<TFromModel> end() {
        return from;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.append(type.name().replace("_", " ")).appendSpace();

        queryBuilder.append("JOIN")
            .appendSpace()
            .append(alias.getFullQuery())
            .appendSpace();

        // natural joins do no have on or using clauses.
        if (!JoinType.NATURAL.equals(type)) {
            if (onGroup != null) {
                queryBuilder.append("ON")
                    .appendSpace()
                    .append(onGroup.getQuery())
                    .appendSpace();
            } else if (!using.isEmpty()) {
                queryBuilder.append("USING (")
                    .appendList(using)
                    .append(")").appendSpace();
            }
        }
        return queryBuilder.getQuery();
    }

    @NonNull
    public Class<TModel> getTable() {
        return table;
    }

    private void checkNatural() {
        if (JoinType.NATURAL.equals(type)) {
            throw new IllegalArgumentException("Cannot specify a clause for this join if its NATURAL." +
                " Specifying a clause would have no effect. Call end() to continue the query.");
        }
    }
}
