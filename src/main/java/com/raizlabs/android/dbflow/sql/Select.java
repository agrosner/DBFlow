package com.raizlabs.android.dbflow.sql;

import android.text.TextUtils;

import com.raizlabs.android.dbflow.sql.builder.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

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

    private String[] mColumns;

    private int mSelectQualifier = NONE;

    public Select(String...columns) {
        mColumns = columns;
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
        return new From<ModelClass>(this, table);
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
