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
        CROSS
    }

    private Class<ModelClass> mTable;

    private JoinType mJoinType;

    private From mFrom;

    private String mAlias;

    private ConditionQueryBuilder<ModelClass> mOn;

    private String[] mUsing;

    Join(From from, Class<ModelClass> table, JoinType joinType) {
        mFrom = from;
        mTable = table;
        mJoinType = joinType;
    }

    public Join as(String alias) {
        mAlias = alias;
        return this;
    }

    public From on(Condition...onConditions) {
        mOn = new ConditionQueryBuilder<ModelClass>(mTable, onConditions);
        return mFrom;
    }

    public From using(String... columns) {
        mUsing = columns;
        return mFrom;
    }


    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();

        if (mJoinType != null) {
            queryBuilder.append(mJoinType.toString()).appendSpace();
        }

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
