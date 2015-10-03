package com.raizlabs.android.dbflow.structure.container;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Description: An anonymous model object that operates just like a {@link java.util.Map}. It must correspond to
 * a {@link ModelClass} to get its blueprint.
 */
public class MapModel<ModelClass extends Model> extends SimpleModelContainer<ModelClass, Map<String, Object>> implements Model {

    public MapModel(Class<ModelClass> table) {
        this(new HashMap<String, Object>(), table);
    }

    public MapModel(@NonNull ModelContainer<ModelClass, ?> existingContainer) {
        super(existingContainer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public BaseModelContainer getInstance(Object inValue, Class<? extends Model> columnClass) {
        return new MapModel((Map<String, Object>) inValue, columnClass);
    }

    @Override
    public boolean containsValue(String key) {
        return getData() != null && getData().containsKey(key) && getData().get(key) != null;
    }

    @Override
    public Map newDataInstance() {
        return new HashMap();
    }

    public MapModel(Map<String, Object> map, Class<ModelClass> table) {
        super(table, map);
    }

    @Override
    public Object getValue(String key) {
        return getData().get(key);
    }

    @Override
    public void put(String columnName, Object value) {
        getData().put(columnName, value);
    }

}
