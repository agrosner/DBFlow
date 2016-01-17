package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents SQLite methods on columns. These act as {@link Property} so we can use them in complex
 * scenarios.
 */
public class Method extends Property {

    /**
     * @param properties Set of properties that the method acts on.
     * @return The average value of all properties within this group. The result is always a float from this statement
     * as long as there is at least one non-NULL input. The result may be NULL if there are no non-NULL columns.
     */
    public static Method avg(IProperty... properties) {
        return new Method("AVG", properties);
    }

    /**
     * @param properties Set of properties that the method acts on.
     * @return A count of the number of times that specified properties are not NULL in a group. Leaving
     * the properties empty returns COUNT(*), which is the total number of rows in the query.
     */
    public static Method count(IProperty... properties) {
        return new Method("COUNT", properties);
    }

    /**
     * @param properties Set of properties that the method acts on.
     * @return A string which is the concatenation of all non-NULL values of the properties.
     */
    public static Method group_concat(IProperty... properties) {
        return new Method("GROUP_CONCAT", properties);
    }

    /**
     * @param properties Set of properties that the method acts on.
     * @return The method that represents the max of the specified columns/properties.
     */
    public static Method max(IProperty... properties) {
        return new Method("MAX", properties);
    }

    /**
     * @param properties Set of properties that the method acts on.
     * @return The method that represents the min of the specified columns/properties.
     */
    public static Method min(IProperty... properties) {
        return new Method("MIN", properties);
    }

    /**
     * @param properties Set of properties that the method acts on.
     * @return The method that represents the sum of the specified columns/properties.
     */
    public static Method sum(IProperty... properties) {
        return new Method("SUM", properties);
    }

    /**
     * @param properties Set of properties that the method acts on.
     * @return The method that represents the total of the specified columns/properties.
     */
    public static Method total(IProperty... properties) {
        return new Method("TOTAL", properties);
    }

    private final List<IProperty> propertyList = new ArrayList<>();

    public Method(String methodName, IProperty... properties) {
        super(null, methodName);
        Collections.addAll(propertyList, properties);

        if (propertyList.isEmpty()) {
            propertyList.add(Property.ALL_PROPERTY);
        }
    }

    /**
     * Allows adding a property to the {@link Method}. Will remove the {@link Property#ALL_PROPERTY}
     * if it exists as first item.
     *
     * @param property The property to add.
     */
    protected void addProperty(@NonNull IProperty property) {
        propertyList.add(property);
    }

    @NonNull
    protected List<IProperty> getPropertyList() {
        return propertyList;
    }

    @Override
    public String toString() {
        return nameAlias.getNamePropertyRaw() + "(" + QueryBuilder.join(",", propertyList) + ")";
    }
}
