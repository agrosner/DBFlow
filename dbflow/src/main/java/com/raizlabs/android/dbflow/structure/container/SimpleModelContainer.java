package com.raizlabs.android.dbflow.structure.container;

import android.support.annotation.NonNull;

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

    public SimpleModelContainer(@NonNull ModelContainer<ModelClass, ?> existingContainer) {
        super(existingContainer);
    }

    @Override
    public Integer getIntegerValue(String key) {
        Object value = getValue(key);
        if (value instanceof String) {
            return Integer.valueOf((String) value);
        } else {
            return (Integer) value;
        }
    }

    @Override
    public int getIntValue(String key) {
        Integer value = getIntegerValue(key);
        return value == null ? 0 : value;
    }

    @Override
    public Long getLongValue(String key) {
        Object value = getValue(key);
        if (value instanceof String) {
            return Long.valueOf((String) value);
        } else {
            return (Long) value;
        }
    }

    @Override
    public long getLngValue(String key) {
        Long value = getLongValue(key);
        return value == null ? 0 : value;
    }

    @Override
    public Boolean getBooleanValue(String key) {
        Object value = getValue(key);
        if (value instanceof String) {
            return Boolean.valueOf((String) value);
        } else {
            return (Boolean) value;
        }
    }

    @Override
    public boolean getBoolValue(String key) {
        Boolean value = getBooleanValue(key);
        return value != null && value;
    }

    @Override
    public String getStringValue(String key) {
        return String.valueOf(getValue(key));
    }

    @Override
    public Float getFloatValue(String key) {
        Object value = getValue(key);
        if (value instanceof String) {
            return Float.valueOf((String) value);
        } else {
            return (Float) value;
        }
    }

    @Override
    public float getFltValue(String key) {
        Float value = getFloatValue(key);
        return value == null ? 0 : value;
    }

    @Override
    public Double getDoubleValue(String key) {
        Object value = getValue(key);
        if (value instanceof String) {
            return Double.valueOf((String) value);
        } else {
            return (Double) value;
        }
    }

    @Override
    public double getDbleValue(String key) {
        Double value = getDoubleValue(key);
        return value == null ? 0 : value;
    }

    @Override
    public Byte[] getBlobValue(String key) {
        return (Byte[]) getValue(key);
    }

    @Override
    public byte[] getBlbValue(String key) {
        return (byte[]) getValue(key);
    }

    @Override
    public Short getShortValue(String key) {
        Object value = getValue(key);
        if (value instanceof String) {
            return Short.valueOf((String) value);
        } else {
            return (Short) value;
        }
    }

    @Override
    public short getShrtValue(String key) {
        Short value = getShortValue(key);
        return value == null ? 0 : value;
    }

    @Override
    public Byte getByteValue(String key) {
        Object value = getValue(key);
        if (value instanceof String) {
            return Byte.valueOf((String) value);
        } else {
            return (Byte) value;
        }
    }

    @Override
    public byte getBytValue(String key) {
        Byte value = getByteValue(key);
        return value == null ? 0 : value;
    }
}
