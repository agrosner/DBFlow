package com.grosner.dbflow.structure.container;

import android.database.Cursor;

import com.grosner.dbflow.structure.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Description: An anonymous model object that operates just like a {@link java.util.Map}. It must correspond to
 * a {@link ModelClass} to get its blueprint.
 */
public class MapModel<ModelClass extends Model> extends BaseModelContainer<ModelClass> implements Model {

    private final Map<String, Object> mDatamap;

    public MapModel(Class<ModelClass> table, Map<String, Object> map) {
        super(table);
        mDatamap = map;
    }

    public MapModel(Class<ModelClass> table) {
        this(table, new HashMap<String, Object>());
    }

    @Override
    protected Object getValue(String columnName) {
        return mDatamap.get(columnName);
    }

    @Override
    protected void put(String columnName, Object value) {
        mDatamap.put(columnName, value);
    }


}
