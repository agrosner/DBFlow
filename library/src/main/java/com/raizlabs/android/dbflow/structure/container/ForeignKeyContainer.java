package com.raizlabs.android.dbflow.structure.container;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description: Holds onto {@link com.raizlabs.android.dbflow.annotation.Column#FOREIGN_KEY} data so
 * the {@link com.raizlabs.android.dbflow.structure.Model} can lazy-load the foreign key data. Overrides
 * most {@link com.raizlabs.android.dbflow.structure.Model} methods to save the associated model data instead
 * of its actual underlying data, which in this case is just the primary keys. Any {@link com.raizlabs.android.dbflow.structure.Model}
 * method will force this object to load the referenced Model from the DB to interact with.
 */
public class ForeignKeyContainer<ModelClass extends Model> extends BaseModelContainer<ModelClass, Map<String, Object>> {

    /**
     * Constructs a new instance with the specified table.
     *
     * @param table The table to associate the container with
     */
    public ForeignKeyContainer(Class<ModelClass> table) {
        this(table, new LinkedHashMap<String, Object>());
    }

    /**
     * Constructs a new instance with table and default data.
     *
     * @param table The table to associate the container with
     * @param data  The data to store in this container
     */
    @SuppressWarnings("unchecked")
    public ForeignKeyContainer(Class<ModelClass> table, Map<String, Object> data) {
        super(table, data);
    }

    @Override
    public Map<String, Object> newDataInstance() {
        return new LinkedHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public BaseModelContainer getInstance(Object inValue, Class<? extends Model> columnClass) {
        return new ForeignKeyContainer(columnClass, (Map<String, Object>) inValue);
    }

    @Override
    public Object getValue(String columnName) {
        return getData().get(columnName);
    }

    @Override
    public void put(String columnName, Object value) {
        getData().put(columnName, value);
    }

    @Override
    public ModelClass toModel() {
        if (mModel == null && mData != null) {
            mModel = new Select().from(mModelAdapter.getModelClass())
                    .where(mContainerAdapter.getPrimaryModelWhere(this)).querySingle();
        }
        return mModel;
    }

    @Override
    public boolean exists() {
        return mModelAdapter.exists(toModel());
    }

    @Override
    public void save() {
        mModelAdapter.save(toModel());
    }

    @Override
    public void delete() {
        mModelAdapter.delete(toModel());
    }

    @Override
    public void update() {
        mModelAdapter.update(toModel());
    }

    @Override
    public void insert() {
        mModelAdapter.insert(toModel());
    }
}
