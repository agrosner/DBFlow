package com.grosner.dbflow.structure.container;

import android.database.Cursor;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.TableStructure;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class BaseModelContainer<ModelClass extends Model, DataClass> implements ModelContainer<ModelClass, DataClass>, Model {

    /**
     * The {@link ModelClass} that the json corresponds to. Use {@link #toModel()} to retrieve this value.
     */
    ModelClass mModel;

    /**
     * The {@link com.grosner.dbflow.structure.TableStructure} that is defined for this {@link org.json.JSONObject}
     */
    TableStructure<ModelClass> mTableStructure;

    /**
     * The data thats stored in the container
     */
    DataClass mData;

    public BaseModelContainer(Class<ModelClass> table) {
        mTableStructure = FlowManager.getManagerForTable(table).getTableStructureForClass(table);
    }

    public BaseModelContainer(Class<ModelClass> table, DataClass data) {
        mTableStructure = FlowManager.getManagerForTable(table).getTableStructureForClass(table);
        mData = data;
    }

    @Override
    public ModelClass toModel() {
        if (mModel == null) {
            mModel = ModelContainerUtils.toModel(this);
        }

        return mModel;
    }

    @Override
    public DataClass getData() {
        return mData;
    }

    public void setData(DataClass data) {
        mData = data;
    }

    @Override
    public abstract Object getValue(String columnName);

    @Override
    public abstract void put(String columnName, Object value);

    @Override
    public TableStructure<ModelClass> getTableStructure() {
        return mTableStructure;
    }

    @Override
    public Class<ModelClass> getTable() {
        return mTableStructure.getModelType();
    }

    @Override
    public void save(boolean async) {
        ModelContainerUtils.save(this, async, SqlUtils.SAVE_MODE_DEFAULT);
    }

    @Override
    public void delete(boolean async) {
        ModelContainerUtils.delete(this, async);
    }

    @Override
    public void update(boolean async) {
        ModelContainerUtils.save(this, async, SqlUtils.SAVE_MODE_UPDATE);
    }

    @Override
    public void insert(boolean async) {
        ModelContainerUtils.save(this, async, SqlUtils.SAVE_MODE_INSERT);
    }

    /**
     * Loads the cursor into the the data contained in this class. This will never
     * be called unless we want to use the data in native format
     *
     * @param cursor The cursor to load.
     */
    @Override
    public void load(Cursor cursor) {
        ModelContainerUtils.loadFromCursor(this, cursor);
    }

    @Override
    public boolean exists() {
        return ModelContainerUtils.exists(this);
    }
}
