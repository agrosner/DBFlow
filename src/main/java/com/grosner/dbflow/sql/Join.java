package com.grosner.dbflow.sql;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class Join<ModelClass extends Model> implements Query {
    public static enum JoinType {
        LEFT,
        OUTER,
        INNER,
        CROSS,
    }

    private Class<ModelClass> mTable;

    private JoinType mJoinType;

    private From mFrom;

    private String mAlias;

    private ConditionQueryBuilder<ModelClass> mOn;

    private String[] mUsing;

    private boolean isNatural = false;

    Join(From from, Class<ModelClass> table, JoinType joinType) {
        mFrom = from;
        mTable = table;
        mJoinType = joinType;
    }

    public Join natural() {
        isNatural = true;
        return this;
    }

    public Join as(String alias) {
        mAlias = alias;
        return this;
    }

    public From on(Condition...onConditions) {
        checkType();
        mOn = new ConditionQueryBuilder<ModelClass>(mTable, onConditions);
        return mFrom;
    }

    public From using(String... columns) {
        checkType();
        mUsing = columns;
        return mFrom;
    }

    private void checkType() {
        if(isNatural) {
            throw new IllegalArgumentException("Joins with type Natural cannot have an ON or USING clause");
        }
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();

        if(isNatural) {
            queryBuilder.append("NATURAL ");
        }

        queryBuilder.append(mJoinType.toString()).appendSpace();

        queryBuilder.append("JOIN")
                .appendSpace()
                .append(FlowManager.getTableName(mTable))
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
