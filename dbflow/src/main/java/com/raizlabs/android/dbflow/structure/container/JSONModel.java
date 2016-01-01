package com.raizlabs.android.dbflow.structure.container;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description: This eliminates the need for converting json into a {@link com.raizlabs.android.dbflow.structure.Model}
 * and then saving to the DB. Let this class handle the saving for you.
 */
public class JSONModel<ModelClass extends Model> extends BaseModelContainer<ModelClass, JSONObject> implements Model {

    /**
     * Constructs this object with the {@link org.json.JSONObject} for the specified {@link ModelClass} table.
     *
     * @param jsonObject The json to reference in {@link com.raizlabs.android.dbflow.structure.Model} operations
     * @param table      The table of the referenced model
     */
    public JSONModel(JSONObject jsonObject, Class<ModelClass> table) {
        super(table, jsonObject);
    }

    /**
     * Constructs this object with an empty {@link org.json.JSONObject} referencing the {@link ModelClass} table.
     *
     * @param table The table of the referenced model
     */
    public JSONModel(Class<ModelClass> table) {
        super(table, new JSONObject());
    }

    public JSONModel(@NonNull ModelContainer<ModelClass, ?> existingContainer) {
        super(existingContainer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public BaseModelContainer getInstance(Object inValue, Class<? extends Model> columnClass) {
        return new JSONModel((JSONObject) inValue, columnClass);
    }

    @Override
    public JSONObject newDataInstance() {
        return new JSONObject();
    }

    @Override
    public boolean containsValue(String key) {
        return getData() != null && getData().has(key) && getData().opt(key) != null;
    }

    @Override
    public Object getValue(String key) {
        Object value = getData() != null ? getData().opt(key) : null;
        if (JSONObject.NULL.equals(value)) {
            value = null;
        }
        return value;
    }

    @Override
    public Integer getIntegerValue(String key) {
        try {
            return getData() != null ? getData().getInt(key) : null;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return null;
        }
    }

    @Override
    public int getIntValue(String key) {
        try {
            return getData() != null ? getData().getInt(key) : 0;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return 0;
        }
    }

    @Override
    public Long getLongValue(String key) {
        try {
            return getData() != null ? getData().getLong(key) : null;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return null;
        }
    }

    @Override
    public long getLngValue(String key) {
        try {
            return getData() != null ? getData().getLong(key) : 0;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return 0;
        }
    }

    @Override
    public Boolean getBooleanValue(String key) {
        try {
            return getData() != null ? getData().getBoolean(key) : null;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return null;
        }
    }

    @Override
    public boolean getBoolValue(String key) {
        try {
            return getData() != null && getData().getBoolean(key);
        } catch (JSONException e) {
            FlowLog.logError(e);
            return false;
        }
    }

    @Override
    public String getStringValue(String key) {
        try {
            return getData() != null ? getData().getString(key) : null;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return null;
        }
    }

    @Override
    public Float getFloatValue(String key) {
        Double d = getDoubleValue(key);
        return (d == null) ? null : d.floatValue();
    }

    @Override
    public float getFltValue(String key) {
        Float f = getFloatValue(key);
        return f == null ? 0 : f;
    }

    @Override
    public Double getDoubleValue(String key) {
        try {
            return getData() != null ? getData().getDouble(key) : null;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return null;
        }
    }

    @Override
    public double getDbleValue(String key) {
        try {
            return getData() != null ? getData().getDouble(key) : 0;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return 0;
        }
    }

    @Override
    public Short getShortValue(String key) {
        try {
            return getData() != null ? (short) getData().getInt(key) : null;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return null;
        }
    }

    @Override
    public short getShrtValue(String key) {
        try {
            return getData() != null ? (short) getData().getInt(key) : 0;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return 0;
        }
    }

    @Override
    public Byte[] getBlobValue(String key) {
        try {
            return getData() != null ? (Byte[]) getData().get(key) : null;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return null;
        }
    }

    @Override
    public byte[] getBlbValue(String key) {
        try {
            if (getData() != null) {
                Object value = getData().get(key);
                if (value instanceof Blob) {
                    return ((Blob) value).getBlob();
                } else {
                    return (byte[]) value;
                }
            }
        } catch (JSONException e) {
            FlowLog.logError(e);
        }
        return null;
    }

    @Override
    public Byte getByteValue(String key) {
        try {
            return getData() != null ? (byte) getData().getInt(key) : null;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return 0;
        }
    }

    @Override
    public byte getBytValue(String key) {
        try {
            return getData() != null ? (byte) getData().getInt(key) : 0;
        } catch (JSONException e) {
            FlowLog.logError(e);
            return 0;
        }
    }

    @Override
    public void put(String columnName, Object value) {
        if (getData() == null) {
            setData(newDataInstance());
        }
        try {
            getData().put(columnName, value);
        } catch (JSONException e) {
            FlowLog.logError(e);
        }
    }

}
