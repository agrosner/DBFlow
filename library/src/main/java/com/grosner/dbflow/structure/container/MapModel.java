package com.grosner.dbflow.structure.container;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.structure.Model;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Author: andrewgrosner
 * Description: An anonymous model object that operates just like a {@link java.util.Map}. It must correspond to
 * a {@link ModelClass} to get its blueprint.
 */
public class MapModel<ModelClass extends Model> extends BaseModelContainer<ModelClass, Map> implements Model {

    public MapModel(Class<ModelClass> table) {
        this(new HashMap<String, Object>(), table);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected BaseModelContainer getInstance(Object inValue, Class<? extends Model> columnClass) {
        return new MapModel((Map<String, Object>) inValue, columnClass);
    }

    public MapModel(Map<String, Object> map, Class<ModelClass> table) {
        super(table, map);
    }

    @Override
    public Object getValue(String columnName) {
        Object data = getData().get(columnName);
        if(data instanceof Map) {
            data = getModelValue(data, columnName);
        }
        return data;
    }

    @Override
    public void put(String columnName, Object value) {
        getData().put(columnName, value);
    }

}
