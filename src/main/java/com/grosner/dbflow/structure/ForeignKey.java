package com.grosner.dbflow.structure;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.Query;
import com.grosner.dbflow.sql.builder.QueryBuilder;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Used to create foreign keys in tables, this enables us to create complex foreign keys within
 * a table without having to mark each individual foreign key.
 */
public abstract class ForeignKey<ModelClass extends Model> implements Query {

    private ModelClass mModel;

    private String mTableName;

    public ForeignKey(FlowManager flowManager, Class<ModelClass> table) {
        mTableName = flowManager.getTableName(table);
    }

    /**
     * Return the corresponding columns
     * @return
     */
    public abstract String[] columns();

    /**
     * Return, in order the corresponding references
     * @return
     */
    public String[] references() {
        return new String[0];
    }

    public ModelClass getModel() {
        return mModel;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder
                .append("FOREIGN KEY(").appendArray(columns()).append(")")
                .appendSpaceSeparated("REFERENCES")
                .append(mTableName);
        String[] references = references();
        if(references.length > 0) {
            queryBuilder.append("(").appendArray(references).append(")");
        }
        return queryBuilder.getQuery();
    }
}
