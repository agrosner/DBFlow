package com.raizlabs.android.dbflow.structure.container;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Provides a simple implementation of most of the value conversion methods. This simplifies other
 * model containers that use {@link #getValue(String)} as a retrieval and will coerce its values into proper types.
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
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            return null;
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
        } else if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else {
            return null;
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
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).byteValue() == 1;
        } else {
            return null;
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
        } else if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else {
            return null;
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
        } else if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else {
            return null;
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
        } else if (value instanceof Short) {
            return (Short) value;
        } else if (value instanceof Number) {
            return ((Number) value).shortValue();
        } else {
            return null;
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
        } else if (value instanceof Byte) {
            return (Byte) value;
        } else if (value instanceof Number) {
            return ((Number) value).byteValue();
        } else {
            return null;
        }
    }

    @Override
    public byte getBytValue(String key) {
        Byte value = getByteValue(key);
        return value == null ? 0 : value;
    }
}
