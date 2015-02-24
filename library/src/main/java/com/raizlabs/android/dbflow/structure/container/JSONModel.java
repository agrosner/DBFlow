package com.raizlabs.android.dbflow.structure.container;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;

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
    public Object getValue(String columnName) {
        Object value = getData() != null ? getData().opt(columnName) : null;
        if(JSONObject.NULL.equals(value)) {
            value = null;
        }
        return value;
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

    /**
     * Loads a model from the DB into the json stored in this class. It also will recreate the JSON stored in this object
     *
     * @param primaryKeys The keys to reference
     */
    public void load(Object... primaryKeys) {
        setData(new JSONObject());
        ConditionQueryBuilder<ModelClass> primaryQuery = FlowManager.getPrimaryWhereQuery(getTable());
        load(new Select().from(mModelAdapter.getModelClass())
                .where(primaryQuery.replaceEmptyParams(primaryKeys)).query());
    }

}
