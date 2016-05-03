package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.PropertyFactory;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description: Specifies a SQLite JOIN statement
 */
public class Join<TModel extends Model, TFromModel extends Model> implements Query {

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
    }

    /**
     * The table to JOIN on
     */
    private Class<TModel> table;

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
    private ConditionGroup on;

    /**
     * What columns to use.
     */
    private List<IProperty> using = new ArrayList<>();

    /**
     * If it is a natural JOIN.
     */
    private boolean isNatural = false;

    Join(From<TFromModel> from, Class<TModel> table, @NonNull JoinType joinType) {
        this.from = from;
        this.table = table;
        type = joinType;
        alias = new NameAlias.Builder(FlowManager.getTableName(table)).build();
    }

    Join(From<TFromModel> from, @NonNull JoinType joinType, ModelQueriable modelQueriable) {
        this.from = from;
        type = joinType;
        alias = PropertyFactory.from(modelQueriable)
    }

    /**
     * Specifies if the JOIN has a name it should be called.
     *
     * @param alias The name to give it
     * @return This instance
     */
    public Join<TModel, TFromModel> as(String alias) {
        this.alias = this.alias
                .newBuilder()
                .as(alias)
                .build();
        return this;
    }

    /**
     * Specifies that this JOIN is a natural JOIN
     *
     * @return The FROM that this JOIN came from.
     */
    public From<TFromModel> natural() {
        isNatural = true;
        return from;
    }

    /**
     * Specify the conditions that the JOIN is on
     *
     * @param onConditions The conditions it is on
     * @return The FROM that this JOIN came from
     */
    public From<TFromModel> on(SQLCondition... onConditions) {
        on = new ConditionGroup();
        on.andAll(onConditions);
        return from;
    }

    /**
     * The USING statement of the JOIN
     *
     * @param columns THe columns to use
     * @return The FROM that this JOIN came from
     */
    public From<TFromModel> using(IProperty... columns) {
        Collections.addAll(using, columns);
        return from;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();

        if (isNatural) {
            queryBuilder.append("NATURAL ");
        }

        queryBuilder.append(type.name().replace("_", " ")).appendSpace();

        queryBuilder.append("JOIN")
                .appendSpace()
                .append(alias.getFullQuery())
                .appendSpace();

        if (on != null) {
            queryBuilder.append("ON")
                    .appendSpace()
                    .append(on.getQuery())
                    .appendSpace();
        } else if (!using.isEmpty()) {
            queryBuilder.append("USING (")
                    .appendArray(using)
                    .append(")").appendSpace();
        }
        return queryBuilder.getQuery();
    }

}
