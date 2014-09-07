package com.raizlabs.android.dbflow.sql.builder;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class FromQueryBuilder extends QueryBuilder<FromQueryBuilder> {

    public FromQueryBuilder appendQualifier(String name, String value) {
        if(value != null) {
            append(name).appendSpaceSeparated(value);
        }
        return this;
    }
}
