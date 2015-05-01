package com.raizlabs.android.dbflow.structure.container;

import com.raizlabs.android.dbflow.config.FlowManager;
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

    ModelContainerAdapter<ModelClass> mModelContainerAdapter;

    /**
     * The data thats stored in the container
     */
    DataClass mData;

    public BaseModelContainer(Class<ModelClass> table) {
        mModelAdapter = FlowManager.getModelAdapter(table);
        mModelContainerAdapter = FlowManager.getContainerAdapter(table);
        if (mModelContainerAdapter == null) {
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
            mModel = mModelContainerAdapter.toModel(this);
        }

        return mModel;
    }

    /**
     * Sets a model to back the container. NOTE: this method invalidates results in any underlying data to
     * be ignored. To resume using this underlying data, set this method wil a null param.
     *
     * @param model
     */
    public void setModel(ModelClass model) {
        mModel = model;
    }

    /**
     * Invalidates the underlying model. In the next {@link #toModel()} call, it will re-query the DB with
     * the underlying data.
     */
    public void invalidateModel() {
        setModel(null);
    }

    /**
     * @param inValue     The value of data for a specified field.
     * @param columnClass The class of the specified field/column
     * @return A created instance to be used for fields that are model containers.
     */
    public abstract BaseModelContainer getInstance(Object inValue, Class<? extends Model> columnClass);


    @SuppressWarnings("unchecked")
    protected Object getModelValue(Object inValue, String columnName) {
        ModelContainerAdapter<? extends Model> modelContainerAdapter = FlowManager.getContainerAdapter(getTable());
        Class<? extends Model> columnClass = (Class<? extends Model>) modelContainerAdapter.getClassForColumn(columnName);
        ModelContainerAdapter<? extends Model> columnAdapter = FlowManager.getContainerAdapter(columnClass);
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
    public void save() {
        mModelContainerAdapter.save(this);
    }

    @Override
    public void delete() {
        mModelContainerAdapter.delete(this);
    }

    @Override
    public void update() {
        mModelContainerAdapter.update(this);
    }

    @Override
    public void insert() {
        mModelContainerAdapter.insert(this);
    }

    @Override
    public boolean exists() {
        return mModelContainerAdapter.exists(this);
    }
}
