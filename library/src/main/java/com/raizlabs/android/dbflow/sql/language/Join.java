package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Specifies a SQLite JOIN statement
 */
public class Join<ModelClass extends Model, FromClass extends Model> implements Query {

    /**
     * The specific type of JOIN that is used.
     */
    public enum JoinType {

        LEFT,

        OUTER,

        INNER,

        CROSS,
    }

    /**
     * The table to JOIN on
     */
    private Class<ModelClass> mTable;

    /**
     * The type of JOIN to use
     */
    private JoinType mJoinType;

    /**
     * The FROM statement that prefixes this statement.
     */
    private From<FromClass> mFrom;

    /**
     * The alias to name the JOIN
     */
    private String mAlias;

    /**
     * The ON conditions
     */
    private ConditionQueryBuilder<ModelClass> mOn;

    /**
     * What columns to use.
     */
    private String[] mUsing;

    /**
     * If it is a natural JOIN.
     */
    private boolean isNatural = false;

    Join(From<FromClass> from, Class<ModelClass> table, JoinType joinType) {
        mFrom = from;
        mTable = table;
        mJoinType = joinType;
    }

    /**
     * Specifies if the JOIN has a name it should be called.
     *
     * @param alias The name to give it
     * @return This instance
     */
    public Join<ModelClass, FromClass> as(String alias) {
        mAlias = alias;
        return this;
    }

    /**
     * Specifies that this JOIN is a natural JOIN
     *
     * @return The FROM that this JOIN came from.
     */
    public From<FromClass> natural() {
        isNatural = true;
        return mFrom;
    }

    /**
     * Specify the conditions that the JOIN is on
     *
     * @param onConditions The conditions it is on
     * @return The FROM that this JOIN came from
     */
    public From<FromClass> on(Condition... onConditions) {
        mOn = new ConditionQueryBuilder<>(mTable, onConditions);
        return mFrom;
    }

    /**
     * The USING statement of thie JOIN
     *
     * @param columns THe columns to use
     * @return The FROM that this JOIN came from
     */
    public From<FromClass> using(String... columns) {
        mUsing = columns;
        return mFrom;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();

        if (isNatural) {
            queryBuilder.append("NATURAL ");
        }

        queryBuilder.append(mJoinType.toString()).appendSpace();

        queryBuilder.append("JOIN")
                .appendSpace()
                .appendQuoted(FlowManager.getTableName(mTable))
                .appendSpace();

        if (mAlias != null) {
            queryBuilder.append("AS ")
                    .append(mAlias)
                    .appendSpace();
        }

        if (mOn != null) {
            queryBuilder.append("ON")
                    .appendSpace()
                    .append(mOn.getRawQuery())
                    .appendSpace();
        } else if (mUsing != null) {
            queryBuilder.append("USING (")
                    .appendArray(mUsing)
                    .append(")").appendSpace();
        }
        return queryBuilder.getQuery();
    }

}
