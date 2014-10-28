package com.grosner.dbflow.structure.container;

import com.grosner.dbflow.annotation.Ignore;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.sql.language.Select;
import com.grosner.dbflow.structure.Model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This eliminates the need for converting json into a {@link com.grosner.dbflow.structure.Model}
 * and then saving to the DB. Let this class handle the saving for you. There are some restrictions to this class:
 * <p/>
 * <br />
 * <li>The names of the keys must match the column names</li>
 */
@Ignore
public class JSONModel<ModelClass extends Model> extends BaseModelContainer<ModelClass, JSONObject> implements Model {

    /**
     * Constructs this object with the {@link org.json.JSONObject} for the specified {@link ModelClass} table.
     *
     * @param jsonObject The json to reference in {@link com.grosner.dbflow.structure.Model} operations
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
    public Object getValue(String columnName) {
        return getData().opt(columnName);
    }

    @Override
    public void put(String columnName, Object value) {
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
        load(new Select().from(mTableStructure.getModelType())
                .where(primaryQuery.replaceEmptyParams(primaryKeys)).query());
    }

}
