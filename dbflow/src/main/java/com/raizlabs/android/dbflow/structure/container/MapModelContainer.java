package com.raizlabs.android.dbflow.structure.container;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.structure.Model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Description: An anonymous model object that operates just like a {@link java.util.Map}. It must correspond to
 * a {@link ModelClass} to get its blueprint.
 */
public class MapModelContainer<ModelClass extends Model> extends SimpleModelContainer<ModelClass, Map<String, Object>> implements Model {

    public MapModelContainer(Class<ModelClass> table) {
        this(new HashMap<String, Object>(), table);
    }

    public MapModelContainer(@NonNull ModelContainer<ModelClass, ?> existingContainer) {
        super(existingContainer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public BaseModelContainer getInstance(Object inValue, Class<? extends Model> columnClass) {
        if (inValue instanceof ModelContainer) {
            return new MapModelContainer((ModelContainer) inValue);
        } else {
            return new MapModelContainer((Map<String, Object>) inValue, columnClass);
        }
    }

    @Override
    public boolean containsValue(String key) {
        return getData() != null && getData().containsKey(key) && getData().get(key) != null;
    }

    @NonNull
    @Override
    public Map<String, Object> newDataInstance() {
        return new HashMap<>();
    }

    public MapModelContainer(Map<String, Object> map, Class<ModelClass> table) {
        super(table, map);
    }

    @Override
    public Object getValue(String key) {
        return getData() != null ? getData().get(key) : null;
    }

    @Override
    public void put(String columnName, Object value) {
        if (getData() == null) {
            setData(newDataInstance());
        }
        //noinspection ConstantConditions
        getData().put(columnName, value);
    }

    @Nullable
    @Override
    public Iterator<String> iterator() {
        return data != null ? data.keySet().iterator() : null;
    }
}
