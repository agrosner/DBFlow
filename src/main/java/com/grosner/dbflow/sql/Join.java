package com.grosner.dbflow.sql;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class Join implements Query {
    static enum JoinType {
        LEFT,
        OUTER,
        INNER,
        CROSS
    }

    private Class<? extends Model> mTable;

    private JoinType mJoinType;

    private From mFrom;

    private String mAlias;

    private String mOn;

    private String[] mUsing;

    Join(From from, Class<? extends Model> table, JoinType joinType) {
        mFrom = from;
        mTable = table;
        mJoinType = joinType;
    }

    public Join as(String alias) {
        mAlias = alias;
        return this;
    }

    public From on(String on) {
        mOn = on;
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
                .append(FlowManager.getManagerForTable(mTable).getTableName(mTable))
                .appendSpace();

        if (mAlias != null) {
            queryBuilder.append("AS ")
                    .append(mAlias)
                    .appendSpace();
        }

        if (mOn != null) {
            queryBuilder.append("ON")
                    .appendSpace()
                    .append(mOn)
                    .appendSpace();
        } else if (mUsing != null) {
            queryBuilder.append("USING (")
                    .appendArray(mUsing)
                    .append(")").appendSpace();
        }
        return queryBuilder.getQuery();
    }

}
