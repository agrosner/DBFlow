package com.raizlabs.android.dbflow.structure.container;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.structure.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: andrewgrosner
 * Description: Holds a {@link org.json.JSONArray} as an array of {@link com.raizlabs.android.dbflow.structure.container.JSONModel}.
 * All operations for {@link com.raizlabs.android.dbflow.structure.Model} are iterated to each inner
 * Model.
 */
public class JSONArrayModel<ModelClass extends Model> implements Model {

    private JSONArray mJsonArray;

    private Class<ModelClass> mTable;

    /**
     * Constructs a new instance with the specified array and table.
     *
     * @param jsonArray The array to back this model.
     * @param table     The table that corresponds to each model.
     */
    public JSONArrayModel(JSONArray jsonArray, Class<ModelClass> table) {
        mJsonArray = jsonArray;
        mTable = table;
    }

    /**
     * Constructs a new instance with an empty array and table.
     * @param table The table that corresponds to each model.
     */
    public JSONArrayModel(Class<ModelClass> table) {
        this(new JSONArray(), table);
    }

    /**
     * Dynamically adds another json object to this array. It should be of type {@link ModelClass}
     * @param jsonObject should correspond to a {@link ModelClass}
     */
    public void addJSONObject(JSONObject jsonObject) {
        mJsonArray.put(jsonObject);
    }

    /**
     * Returns the {@link org.json.JSONObject} from within the json array.
     * @param index
     * @return
     */
    public JSONObject getJSONObject(int index) {
        JSONObject jsonObject = null;
        try {
            jsonObject = mJsonArray.getJSONObject(index);
        } catch (JSONException e) {
            FlowLog.logError(e);
        }
        return jsonObject;
    }

    /**
     * Returns the {@link ModelClass} representation of the object.
     * @param index The valid index
     * @return The {@link ModelClass}
     */
    public ModelClass getModelObject(int index) {
        return getJsonModel(index).toModel();
    }

    public JSONModel<ModelClass> getJsonModel(int index) {
        return new JSONModel<>(getJSONObject(index), mTable);
    }

    /**
     * @return The length of the backed {@link org.json.JSONArray}
     */
    public int length() {
        return mJsonArray != null ? mJsonArray.length() : 0;
    }

    @Override
    public void save() {
        if (mJsonArray != null && mJsonArray.length() > 0) {
            int length = mJsonArray.length();
            JSONModel<ModelClass> jsonModel = new JSONModel<>(mTable);
            for (int i = 0; i < length; i++) {
                try {
                    jsonModel.mData = mJsonArray.getJSONObject(i);
                    jsonModel.save();
                } catch (JSONException e) {
                    FlowLog.logError(e);
                }
            }
        }
    }

    @Override
    public void delete() {
        if (mJsonArray != null && mJsonArray.length() > 0) {
            int length = mJsonArray.length();
            JSONModel<ModelClass> jsonModel = new JSONModel<>(mTable);
            for (int i = 0; i < length; i++) {
                try {
                    jsonModel.mData = mJsonArray.getJSONObject(i);
                    jsonModel.delete();
                } catch (JSONException e) {
                    FlowLog.logError(e);
                }
            }
        }
    }

    @Override
    public void update() {
        if (mJsonArray != null && mJsonArray.length() > 0) {
            int length = mJsonArray.length();
            JSONModel<ModelClass> jsonModel = new JSONModel<>(mTable);
            for (int i = 0; i < length; i++) {
                try {
                    jsonModel.mData = mJsonArray.getJSONObject(i);
                    jsonModel.update();
                } catch (JSONException e) {
                    FlowLog.logError(e);
                }
            }
        }
    }

    @Override
    public void insert() {
        if (mJsonArray != null && mJsonArray.length() > 0) {
            int length = mJsonArray.length();
            JSONModel<ModelClass> jsonModel = new JSONModel<>(mTable);
            for (int i = 0; i < length; i++) {
                try {
                    jsonModel.mData = mJsonArray.getJSONObject(i);
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
            exists = new JSONModel<>(mJsonArray.getJSONObject(index), mTable).exists();
        } catch (JSONException e) {
            FlowLog.logError(e);
        }

        return exists;
    }
}

