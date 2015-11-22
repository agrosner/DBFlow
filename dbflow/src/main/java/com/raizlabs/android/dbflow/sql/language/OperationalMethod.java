package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.property.IProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Provides a way to operate on {@link IProperty} such that they have operational separations
 * similar to {@link Condition}
 */
public class OperationalMethod extends Method {

    private List<String> operationsList = new ArrayList<>();

    public OperationalMethod(@NonNull IProperty firstProperty) {
        super(null, firstProperty);
    }

    public OperationalMethod plus(IProperty property) {
        return append(property, Condition.Operation.PLUS);
    }

    public OperationalMethod minus(IProperty property) {
        return append(property, Condition.Operation.MINUS);
    }

    public OperationalMethod append(IProperty property, String operation) {
        addProperty(property);
        operationsList.add(operation);
        return this;
    }

    @Override
    public NameAlias getNameAlias() {
        if (nameAlias == null) {
            String query = "(";
            List<IProperty> propertyList = getPropertyList();
            for (int i = 0; i < propertyList.size(); i++) {
                IProperty property = propertyList.get(i);
                if (i > 0) {
                    query += " " + operationsList.get(i - 1) + " ";
                }
                query += property.toString();

            }
            query += ")";
            nameAlias = new NameAlias(query, false).tickName(false);
        }
        return nameAlias;
    }

    @Override
    public String toString() {
        return getNameAlias().toString();
    }
}
