package com.raizlabs.android.dbflow.structure.container;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.structure.InvalidDBConfiguration;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * Description: The base class that all ModelContainers should extend.
 */
public abstract class BaseModelContainer<ModelClass extends Model, DataClass> implements ModelContainer<ModelClass, DataClass>, Model {

    /**
     * The {@link ModelClass} that the json corresponds to. Use {@link #toModel()} to retrieve this value.
     */
    ModelClass mModel;

    /**
     * The {@link com.raizlabs.android.dbflow.structure.ModelAdapter} that is defined for this {@link org.json.JSONObject}
     */
    ModelAdapter<ModelClass> mModelAdapter;

    ContainerAdapter<ModelClass> mContainerAdapter;

    /**
     * The data thats stored in the container
     */
    DataClass mData;

    public BaseModelContainer(Class<ModelClass> table) {
        mModelAdapter = FlowManager.getModelAdapter(table);
        mContainerAdapter = FlowManager.getContainerAdapter(table);
        if (mContainerAdapter == null) {
            throw new InvalidDBConfiguration("The table" + FlowManager.getTableName(table) + " did not specify the ContainerAdapter" +
                    "annotation. Please add and rebuild");
        }
    }

    public BaseModelContainer(Class<ModelClass> table, DataClass data) {
        this(table);
        mData = data;
    }

    @Override
    public ModelClass toModel() {
        if (mModel == null && mData != null) {
            mModel = mContainerAdapter.toModel(this);
        }

        return mModel;
    }

    /**
     * @param inValue     The value of data for a specified field.
     * @param columnClass The class of the specified field/column
     * @return A created instance to be used for fields that are model containers.
     */
    public abstract BaseModelContainer getInstance(Object inValue, Class<? extends Model> columnClass);


    @SuppressWarnings("unchecked")
    protected Object getModelValue(Object inValue, String columnName) {
        ContainerAdapter<? extends Model> containerAdapter = FlowManager.getContainerAdapter(getTable());
        Class<? extends Model> columnClass = (Class<? extends Model>) containerAdapter.getClassForColumn(columnName);
        ContainerAdapter<? extends Model> columnAdapter = FlowManager.getContainerAdapter(columnClass);
        if (columnAdapter != null) {
            inValue = columnAdapter.toModel(getInstance(inValue, columnClass));
        } else {
            throw new RuntimeException("Column: " + columnName + "'s class needs to add the @ContainerAdapter annotation");
        }
        return inValue;
    }

    @Override
    public DataClass getData() {
        return mData;
    }

    /**
     * Sets the data for this container
     *
     * @param data The data object that backs this container
     */
    @Override
    public void setData(DataClass data) {
        mData = data;
        mModel = null;
    }

    @Override
    public abstract Object getValue(String columnName);

    @Override
    public abstract void put(String columnName, Object value);

    @Override
    public ModelAdapter<ModelClass> getModelAdapter() {
        return mModelAdapter;
    }

    @Override
    public Class<ModelClass> getTable() {
        return mModelAdapter.getModelClass();
    }

    @Override
    public void save(boolean async) {
        mContainerAdapter.save(async, this);
    }

    @Override
    public void delete(boolean async) {
        mContainerAdapter.delete(async, this);
    }

    @Override
    public void update(boolean async) {
        mContainerAdapter.update(async, this);
    }

    @Override
    public void insert(boolean async) {
        mContainerAdapter.insert(async, this);
    }

    @Override
    public boolean exists() {
        return mContainerAdapter.exists(this);
    }
}
