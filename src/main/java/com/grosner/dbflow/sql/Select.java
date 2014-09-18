package com.grosner.dbflow.sql;

import android.text.TextUtils;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class Select implements Query {

    public static final int NONE = -1;

    public static final int DISTINCT = 0;

    public static final int ALL = 1;

    public static final int COUNT = 2;

    private final String[] mColumns;

    private int mSelectQualifier = NONE;

    private final FlowManager mManager;

    public Select(String...columns) {
        this(FlowManager.getInstance(), columns);
    }

    public Select(FlowManager flowManager, String...columns) {
        mColumns = columns;
        mManager = flowManager;
    }

    public Select distinct() {
        mSelectQualifier = DISTINCT;
        return this;
    }

    public Select all() {
        mSelectQualifier = ALL;
        return this;
    }

    public Select count() {
        mSelectQualifier = COUNT;
        return this;
    }

    public <ModelClass extends Model> From<ModelClass> from(Class<ModelClass> table) {
        return new From<ModelClass>(mManager, this, table);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.append("SELECT").appendSpace();

        if(mSelectQualifier != NONE) {
            if(mSelectQualifier == DISTINCT) {
                queryBuilder.append("DISTINCT");
            } else if(mSelectQualifier == ALL) {
                queryBuilder.append("ALL");
            } else if(mSelectQualifier == COUNT) {
                queryBuilder.append("COUNT(*)");
            }
            queryBuilder.appendSpace();
        }

        if(mColumns != null && mColumns.length > 0) {
            queryBuilder.append(TextUtils.join(", ", mColumns));
        } else if(mSelectQualifier != COUNT) {
            queryBuilder.append("*");
        }
        queryBuilder.appendSpace();
        return queryBuilder.getQuery();
    }
}
