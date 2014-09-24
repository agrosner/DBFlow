package com.grosner.dbflow.structure.json;

import android.database.Cursor;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.structure.Ignore;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.TableStructure;

import org.json.JSONObject;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This eliminates the need for converting json into a {@link com.grosner.dbflow.structure.Model}
 * and then saving to the DB. Let this class handle the saving for you. There are some restrictions to this class:
 *
 * <br />
 * <li>The names of the keys must match the column names</li>
 */
@Ignore
public class JSONModel<ModelClass extends Model> implements Model {

    /**
     * The {@link org.json.JSONObject} that we query from
     */
    JSONObject mJson;

    /**
     * The {@link ModelClass} that the json corresponds to. Use {@link #toModel()} to retrieve this value.
     */
    ModelClass mModel;

    /**
     * The {@link com.grosner.dbflow.structure.TableStructure} that is defined for this {@link org.json.JSONObject}
     */
    TableStructure<ModelClass> mTableStructure;

    /**
     * The manager to use for this model
     */
    FlowManager mManager;

    /**
     * Constructs this object with the {@link org.json.JSONObject} for the specified {@link ModelClass} table.
     * @param flowManager
     * @param jsonObject
     * @param table
     */
    public JSONModel(FlowManager flowManager, JSONObject jsonObject, Class<ModelClass> table) {
        mManager = flowManager;
        mTableStructure = flowManager.getTableStructureForClass(table);
        mJson = jsonObject;
    }

    public JSONModel(JSONObject jsonObject, Class<ModelClass> table) {
        this(FlowManager.getInstance(), jsonObject, table);
    }

    public JSONModel(FlowManager flowManager, Class<ModelClass> table) {
        this(flowManager, new JSONObject(), table);
    }

    public JSONModel(Class<ModelClass> table) {
        this(FlowManager.getInstance(), table);
    }

    @Override
    public void save(boolean async) {
        JsonStructureUtils.save(this, async, SqlUtils.SAVE_MODE_DEFAULT, false);
    }

    @Override
    public void delete(boolean async) {
        JsonStructureUtils.delete(this, async);
    }

    @Override
    public void update(boolean async) {
        JsonStructureUtils.save(this, async, SqlUtils.SAVE_MODE_UPDATE, false);
    }

    /**
     * Loads the cursor into the the {@link org.json.JSONObject} contained in this class. This will never
     * be called
     * @param cursor The cursor to load.
     */
    @Override
    public void load(Cursor cursor) {
        JsonStructureUtils.loadFromCursor(this, cursor);
    }

    @Override
    public boolean exists() {
        return JsonStructureUtils.exists(this);
    }

    /**
     * Will convert the json into {@link ModelClass}
     * @return The model from this json.
     */
    public ModelClass toModel() {
        if(mModel == null) {
            mModel = JsonStructureUtils.toModel(this);
        }
        return mModel;
    }

    public Class<ModelClass> getTable() {
        return mTableStructure.getModelType();
    }
}
