package com.raizlabs.android.dbflow.structure.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Common {@link Cursor} class that wraps cursors we use in this library with convenience loading methods.
 * This is used to help cut down on generated code size and potentially decrease method count.
 */
public class FlowCursor extends CursorWrapper {

    public static FlowCursor from(Cursor cursor) {
        if (cursor instanceof FlowCursor) {
            return (FlowCursor) cursor;
        } else {
            return new FlowCursor(cursor);
        }
    }

    private Cursor cursor; // compatibility reasons

    private FlowCursor(@NonNull Cursor cursor) {
        super(cursor);
        this.cursor = cursor;
    }

    // compatibility
    public Cursor getWrappedCursor() {
        return cursor;
    }

    public String getStringOrDefault(int index, String defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getString(index);
        } else {
            return defValue;
        }
    }

    public String getStringOrDefault(String columnName) {
        return getStringOrDefault(cursor.getColumnIndex(columnName));
    }

    @Nullable
    public String getStringOrDefault(int index) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getString(index);
        } else {
            return null;
        }
    }

    public String getStringOrDefault(String columnName, String defValue) {
        return getStringOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public int getIntOrDefault(String columnName) {
        return getIntOrDefault(cursor.getColumnIndex(columnName));
    }

    public int getIntOrDefault(int index) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getInt(index);
        } else {
            return 0;
        }
    }

    public int getIntOrDefault(int index, int defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getInt(index);
        } else {
            return defValue;
        }
    }

    public int getIntOrDefault(String columnName, int defValue) {
        return getIntOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public Integer getIntOrDefault(int index, Integer defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getInt(index);
        } else {
            return defValue;
        }
    }

    public Integer getIntOrDefault(String columnName, Integer defValue) {
        return getIntOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public double getDoubleOrDefault(int index, double defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getDouble(index);
        } else {
            return defValue;
        }
    }

    public double getDoubleOrDefault(String columnName) {
        return getDoubleOrDefault(cursor.getColumnIndex(columnName));
    }

    public double getDoubleOrDefault(int index) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getDouble(index);
        } else {
            return 0;
        }
    }

    public double getDoubleOrDefault(String columnName, double defValue) {
        return getDoubleOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public Double getDoubleOrDefault(int index, Double defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getDouble(index);
        } else {
            return defValue;
        }
    }

    public Double getDoubleOrDefault(String columnName, Double defValue) {
        return getDoubleOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public float getFloatOrDefault(int index, float defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getFloat(index);
        } else {
            return defValue;
        }
    }

    public float getFloatOrDefault(String columnName) {
        return getFloatOrDefault(cursor.getColumnIndex(columnName));
    }

    public float getFloatOrDefault(int index) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getFloat(index);
        } else {
            return 0;
        }
    }

    public float getFloatOrDefault(String columnName, float defValue) {
        return getFloatOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public Float getFloatOrDefault(int index, Float defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getFloat(index);
        } else {
            return defValue;
        }
    }

    public Float getFloatOrDefault(String columnName, Float defValue) {
        return getFloatOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public long getLongOrDefault(int index, long defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getLong(index);
        } else {
            return defValue;
        }
    }

    public long getLongOrDefault(String columnName) {
        return getLongOrDefault(cursor.getColumnIndex(columnName));
    }

    public long getLongOrDefault(int index) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getLong(index);
        } else {
            return 0;
        }
    }

    public long getLongOrDefault(String columnName, long defValue) {
        return getLongOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public Long getLongOrDefault(int index, Long defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getLong(index);
        } else {
            return defValue;
        }
    }

    public Long getLongOrDefault(String columnName, Long defValue) {
        return getLongOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public short getShortOrDefault(int index, short defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getShort(index);
        } else {
            return defValue;
        }
    }

    public short getShortOrDefault(String columnName) {
        return getShortOrDefault(cursor.getColumnIndex(columnName));
    }

    public short getShortOrDefault(int index) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getShort(index);
        } else {
            return 0;
        }
    }

    public short getShortOrDefault(String columnName, short defValue) {
        return getShortOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public Short getShortOrDefault(int index, Short defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getShort(index);
        } else {
            return defValue;
        }
    }

    public Short getShortOrDefault(String columnName, Short defValue) {
        return getShortOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public byte[] getBlobOrDefault(int index, byte[] defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return cursor.getBlob(index);
        } else {
            return defValue;
        }
    }

    public byte[] getBlobOrDefault(String columnName, byte[] defValue) {
        return getBlobOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public boolean getBooleanOrDefault(int index, boolean defValue) {
        if (index != -1 && !cursor.isNull(index)) {
            return getBoolean(index);
        } else {
            return defValue;
        }
    }

    public boolean getBooleanOrDefault(String columnName) {
        return getBooleanOrDefault(cursor.getColumnIndex(columnName));
    }

    public boolean getBooleanOrDefault(int index) {
        if (index != -1 && !cursor.isNull(index)) {
            return getBoolean(index);
        } else {
            return false;
        }
    }

    public boolean getBooleanOrDefault(String columnName, boolean defValue) {
        return getBooleanOrDefault(cursor.getColumnIndex(columnName), defValue);
    }

    public boolean getBoolean(int index) {
        return cursor.getInt(index) == 1;
    }

}

