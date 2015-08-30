package com.raizlabs.android.dbflow.structure.container;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description:
 */
public abstract class SimpleModelContainer<ModelClass extends Model, DataClass> extends BaseModelContainer<ModelClass, DataClass> {

    public SimpleModelContainer(Class<ModelClass> table) {
        super(table);
    }

    public SimpleModelContainer(Class<ModelClass> table, DataClass data) {
        super(table, data);
    }

    @Override
    public Integer getIntValue(String columnName) {
        Object value = getValue(columnName);
        if (value instanceof String) {
            return Integer.valueOf((String) value);
        } else {
            return (Integer) value;
        }
    }

    @Override
    public Long getLongValue(String columnName) {
        Object value = getValue(columnName);
        if (value instanceof String) {
            return Long.valueOf((String) value);
        } else {
            return (Long) value;
        }
    }

    @Override
    public Boolean getBooleanValue(String columnName) {
        Object value = getValue(columnName);
        if (value instanceof String) {
            return Boolean.valueOf((String) value);
        } else {
            return (Boolean) value;
        }
    }

    @Override
    public String getStringValue(String columnName) {
        return String.valueOf(getValue(columnName));
    }

    @Override
    public Float getFloatValue(String columnName) {
        Object value = getValue(columnName);
        if (value instanceof String) {
            return Float.valueOf((String) value);
        } else {
            return (Float) value;
        }
    }

    @Override
    public Short getShortValue(String columnName) {
        Object value = getValue(columnName);
        if (value instanceof String) {
            return Short.valueOf((String) value);
        } else {
            return (Short) value;
        }
    }
}
