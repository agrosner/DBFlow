package com.raizlabs.android.dbflow.structure.container;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.structure.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description: Holds a {@link JSONArray} as an array of {@link JSONModel}.
 * All operations for {@link Model} are iterated to each inner {@link JSONObject} as {@link JSONModel}.
 */
public class JSONArrayModel<TModel extends Model> implements Model {

    private JSONArray jsonArray;

    private Class<TModel> table;

    /**
     * Constructs a new instance with the specified array and table.
     *
     * @param jsonArray The array to back this model.
     * @param table     The table that corresponds to each model.
     */
    public JSONArrayModel(JSONArray jsonArray, Class<TModel> table) {
        this.jsonArray = jsonArray;
        this.table = table;
    }

    /**
     * Constructs a new instance with an empty array and table.
     *
     * @param table The table that corresponds to each model.
     */
    public JSONArrayModel(Class<TModel> table) {
        this(new JSONArray(), table);
    }

    /**
     * Dynamically adds another json object to this array. It should be of type {@link TModel}
     *
     * @param jsonObject should correspond to a {@link TModel}
     */
    public void addJSONObject(JSONObject jsonObject) {
        jsonArray.put(jsonObject);
    }

    /**
     * Returns the {@link org.json.JSONObject} from within the json array.
     *
     * @param index
     * @return
     */
    public JSONObject getJSONObject(int index) {
        JSONObject jsonObject = null;
        try {
            jsonObject = jsonArray.getJSONObject(index);
        } catch (JSONException e) {
            FlowLog.logError(e);
        }
        return jsonObject;
    }

    /**
     * Returns the {@link TModel} representation of the object.
     *
     * @param index The valid index
     * @return The {@link TModel}
     */
    public TModel getModelObject(int index) {
        return getJsonModel(index).toModel();
    }

    public JSONModel<TModel> getJsonModel(int index) {
        return new JSONModel<>(getJSONObject(index), table);
    }

    /**
     * @return The length of the backed {@link JSONArray}
     */
    public int length() {
        return jsonArray != null ? jsonArray.length() : 0;
    }

    @Override
    public void save() {
        if (jsonArray != null && jsonArray.length() > 0) {
            int length = jsonArray.length();
            JSONModel<TModel> jsonModel = new JSONModel<>(table);
            for (int i = 0; i < length; i++) {
                try {
                    jsonModel.data = jsonArray.getJSONObject(i);
                    jsonModel.save();
                } catch (JSONException e) {
                    FlowLog.logError(e);
                }
            }
        }
    }

    @Override
    public void delete() {
        if (jsonArray != null && jsonArray.length() > 0) {
            int length = jsonArray.length();
            JSONModel<TModel> jsonModel = new JSONModel<>(table);
            for (int i = 0; i < length; i++) {
                try {
                    jsonModel.data = jsonArray.getJSONObject(i);
                    jsonModel.delete();
                } catch (JSONException e) {
                    FlowLog.logError(e);
                }
            }
        }
    }

    @Override
    public void update() {
        if (jsonArray != null && jsonArray.length() > 0) {
            int length = jsonArray.length();
            JSONModel<TModel> jsonModel = new JSONModel<>(table);
            for (int i = 0; i < length; i++) {
                try {
                    jsonModel.data = jsonArray.getJSONObject(i);
                    jsonModel.update();
                } catch (JSONException e) {
                    FlowLog.logError(e);
                }
            }
        }
    }

    @Override
    public void insert() {
        if (jsonArray != null && jsonArray.length() > 0) {
            int length = jsonArray.length();
            JSONModel<TModel> jsonModel = new JSONModel<>(table);
            for (int i = 0; i < length; i++) {
                try {
                    jsonModel.data = jsonArray.getJSONObject(i);
                    jsonModel.insert();
                } catch (JSONException e) {
                    FlowLog.logError(e);
                }
            }
        }
    }

    @Override
    public boolean exists() {
        throw new RuntimeException("Cannot call exists() on a JsonArrayModel. Call exists(int) instead");
    }

    /**
     * Retrieves the {@link org.json.JSONObject} at the specified index and checks if it exists
     *
     * @param index The valid index
     * @return true if the json object at the specified index exists, false if it's an invalid index or
     * does not exist.
     */
    public boolean exists(int index) {
        boolean exists = false;
        try {
            exists = new JSONModel<>(jsonArray.getJSONObject(index), table).exists();
        } catch (JSONException e) {
            FlowLog.logError(e);
        }

        return exists;
    }
}

