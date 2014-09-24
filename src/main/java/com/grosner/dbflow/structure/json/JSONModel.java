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
     * The {@link com.grosner.dbflow.structure.TableStructure} that is defined for this {@link org.json.JSONObject}
     */
    TableStructure<ModelClass> mTableStructure;

    /**
     * The manager to use for this model
     */
    FlowManager mManager;

    public JSONModel(FlowManager flowManager, JSONObject jsonObject, Class<ModelClass> table) {
        mManager = flowManager;
        mTableStructure = flowManager.getTableStructureForClass(table);
        mJson = jsonObject;
    }

    public JSONModel(JSONObject jsonObject, Class<ModelClass> table) {
        this(FlowManager.getInstance(), jsonObject, table);
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

    }

    @Override
    public void load(Cursor cursor) {

    }

    @Override
    public boolean exists() {
        return false;
    }

    /**
     * Will convert the json into {@link ModelClass}
     * @return The model from this json.
     */
    public ModelClass toModel() {

    }

    public Class<ModelClass> getTable() {
        return mTableStructure.getModelType();
    }
}
