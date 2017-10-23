package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description: A SQL SELECT statement generator. It generates the SELECT part of the statement.
 */
public class Select implements Query {

    /**
     * Default does not include the qualifier
     */
    public static final int NONE = -1;
    /**
     * The select qualifier to append to the SELECT statement
     */
    private int mSelectQualifier = NONE;
    /**
     * SELECT DISTINCT call
     */
    public static final int DISTINCT = 0;
    /**
     * SELECT ALL call
     */
    public static final int ALL = 1;

    private final List<IProperty> propertyList = new ArrayList<>();

    /**
     * Creates this instance with the specified columns from the specified {@link com.raizlabs.android.dbflow.config.FlowManager}
     *
     * @param properties The properties to select from.
     */
    public Select(IProperty... properties) {
        Collections.addAll(propertyList, properties);

        if (propertyList.isEmpty()) {
            propertyList.add(Property.Companion.getALL_PROPERTY());
        }
    }

    /**
     * Passes this statement to the {@link From}
     *
     * @param table    The model table to run this query on
     * @param <TModel> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return the From part of this query
     */
    @NonNull
    public <TModel> From<TModel> from(@NonNull Class<TModel> table) {
        return new From<>(this, table);
    }

    /**
     * appends {@link #DISTINCT} to the query
     *
     * @return
     */
    @NonNull
    public Select distinct() {
        return selectQualifier(DISTINCT);
    }

    @NonNull
    public String toString() {
        return getQuery();
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder("SELECT ");

        if (mSelectQualifier != NONE) {
            if (mSelectQualifier == DISTINCT) {
                queryBuilder.append("DISTINCT");
            } else if (mSelectQualifier == ALL) {
                queryBuilder.append("ALL");
            }
            queryBuilder.appendSpace();
        }

        queryBuilder.append(QueryBuilder.join(",", propertyList));
        queryBuilder.appendSpace();
        return queryBuilder.getQuery();
    }


    /**
     * Helper method to pick the correct qualifier for a SELECT query
     *
     * @param qualifierInt Can be {@link #ALL}, {@link #NONE}, or {@link #DISTINCT}
     * @return
     */
    private Select selectQualifier(int qualifierInt) {
        mSelectQualifier = qualifierInt;
        return this;
    }
}
