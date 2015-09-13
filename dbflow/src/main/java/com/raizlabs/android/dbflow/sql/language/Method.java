package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents SQLite methods on columns. These act as {@link Property} so we can use them in complex
 * scenarios.
 */
public class Method extends Property {

    /**
     * @param properties
     * @return The average value of all properties within this group. The result is always a float from this statement
     * as long as there is at least one non-NULL input. The result may be NULL if there are no non-NULL columns.
     */
    public static Method avg(Property... properties) {
        return new Method("AVG", properties);
    }

    /**
     * @param properties
     * @return A count of the number of times that specified properties are not NULL in a group. Leaving
     * the properties empty returns COUNT(*), which is the total number of rows in the query.
     */
    public static Method count(Property... properties) {
        return new Method("COUNT", properties);
    }

    /**
     * @param properties
     * @return A string which is the concatenation of all non-NULL values of the properties.
     */
    public static Method group_concat(Property... properties) {
        return new Method("GROUP_CONCAT", properties);
    }

    public static Method max(Property... properties) {
        return new Method("MAX", properties);
    }

    public static Method min(Property... properties) {
        return new Method("MIN", properties);
    }

    public static Method sum(Property... properties) {
        return new Method("SUM", properties);
    }

    public static Method total(Property... properties) {
        return new Method("TOTAL", properties);
    }

    public static Method date(Property... properties) {
        return new Method("DATE", properties);
    }

    private final List<Property> propertyList = new ArrayList<>();

    public Method(String methodName, Property... properties) {
        super(null, methodName);
        Collections.addAll(propertyList, properties);

        if (propertyList.isEmpty()) {
            propertyList.add(Property.ALL_PROPERTY);
        }
    }

    @Override
    public String toString() {
        return nameAlias.getNameNoTicks() + "(" + QueryBuilder.join(",", propertyList) + ")";
    }
}
