package com.grosner.dbflow.sql;

import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class Delete implements Query {

    public Delete() {
    }

    public <ModelClass extends Model> From<ModelClass> from(Class<ModelClass> table) {
        return new From<ModelClass>(this, table);
    }

    @Override
    public String getQuery() {
        return new QueryBuilder()
                .append("DELETE")
                .appendSpace().getQuery();
    }
}
