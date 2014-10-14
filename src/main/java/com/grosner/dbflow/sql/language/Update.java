package com.grosner.dbflow.sql.language;

import android.text.TextUtils;

import com.grosner.dbflow.sql.Query;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class Update implements Query {

    private String mOrQualifier;

    public Update orRollback() {
        mOrQualifier = "ROLLBACK";
        return this;
    }

    public Update orAbort() {
        mOrQualifier = "ABORT";
        return this;
    }

    public Update orReplace() {
        mOrQualifier = "REPLACE";
        return this;
    }

    public Update orFail() {
        mOrQualifier = "FAIL";
        return this;
    }

    public Update orIgnore() {
        mOrQualifier = "IGNORE";
        return this;
    }

    public <ModelClass extends Model> From<ModelClass> table(Class<ModelClass> table) {
        return new From<ModelClass>(this, table);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder("UPDATE ");
        if(!TextUtils.isEmpty(mOrQualifier)) {
            queryBuilder.append("OR").appendSpaceSeparated(mOrQualifier);
        }
        return queryBuilder.getQuery();
    }
}
