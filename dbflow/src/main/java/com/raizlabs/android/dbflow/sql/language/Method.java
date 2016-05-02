package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;

import java.util.ArrayList;
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
    private List<String> operationsList = new ArrayList<>();
    private final IProperty methodProperty;

    public Method(IProperty... properties) {
        this(null, properties);
    }

    public Method(String methodName, IProperty... properties) {
        super(null, (String) null);

        methodProperty = new Property(null, new NameAlias2.Builder(methodName).build());

        if (properties.length == 0) {
            propertyList.add(Property.ALL_PROPERTY);
        } else {
            for (IProperty property : properties) {
                addProperty(property);
            }
        }
    }

    @Override
    public Method plus(IProperty property) {
        return append(property, Condition.Operation.PLUS);
    }

    @Override
    public Method minus(IProperty property) {
        return append(property, Condition.Operation.MINUS);
    }

    /**
     * Allows adding a property to the {@link Method}. Will remove the {@link Property#ALL_PROPERTY}
     * if it exists as first item.
     *
     * @param property The property to add.
     */
    public Method addProperty(@NonNull IProperty property) {
        if (propertyList.size() == 1 && propertyList.get(0) == Property.ALL_PROPERTY) {
            propertyList.remove(0);
        }
        return append(property, ",");
    }

    /**
     * Appends a property with the specified operation that separates it. The operation will appear before
     * the property specified.
     */
    public Method append(IProperty property, String operation) {
        propertyList.add(property);
        operationsList.add(operation);
        return this;
    }

    @NonNull
    protected List<IProperty> getPropertyList() {
        return propertyList;
    }

    @Override
    public NameAlias2 getNameAlias() {
        if (nameAlias == null) {
            String query = methodProperty.getQuery();
            if (query == null) {
                query = "";
            }
            query += "(";
            List<IProperty> propertyList = getPropertyList();
            for (int i = 0; i < propertyList.size(); i++) {
                IProperty property = propertyList.get(i);
                if (i > 0) {
                    query += " " + operationsList.get(i) + " ";
                }
                query += property.toString();

            }
            query += ")";
            nameAlias = NameAlias2.Builder.rawBuilder(query)
                    .build();
        }
        return nameAlias;
    }

    //@Override
    //public String toString() {
    //    return getNameAlias().toString();
    //}
}
