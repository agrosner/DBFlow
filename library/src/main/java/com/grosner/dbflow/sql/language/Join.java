package com.grosner.dbflow.sql.language;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.Query;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class Join<ModelClass extends Model, FromClass extends Model> implements Query {
    private Class<ModelClass> mTable;
    private JoinType mJoinType;
    private From<FromClass> mFrom;
    private String mAlias;
    private ConditionQueryBuilder<ModelClass> mOn;
    private String[] mUsing;
    private boolean isNatural = false;

    Join(From<FromClass> from, Class<ModelClass> table, JoinType joinType) {
        mFrom = from;
        mTable = table;
        mJoinType = joinType;
    }

    public Join as(String alias) {
        mAlias = alias;
        return this;
    }

    public From<FromClass> natural() {
        isNatural = true;
        return mFrom;
    }

    public From<FromClass> on(Condition... onConditions) {
        mOn = new ConditionQueryBuilder<ModelClass>(mTable, onConditions);
        return mFrom;
    }

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

    public static enum JoinType {
        LEFT,
        OUTER,
        INNER,
        CROSS,
    }

}
