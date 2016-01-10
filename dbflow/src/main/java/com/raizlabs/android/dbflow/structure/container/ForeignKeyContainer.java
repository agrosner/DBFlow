package com.raizlabs.android.dbflow.structure.container;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description: Holds onto {@link com.raizlabs.android.dbflow.annotation.ForeignKey} data so the {@link
 * com.raizlabs.android.dbflow.structure.Model} can lazy-load the foreign key data. Overrides most {@link
 * com.raizlabs.android.dbflow.structure.Model} methods to save the associated model data instead of its actual
 * underlying data, which in this case is just the primary keys. Any {@link com.raizlabs.android.dbflow.structure.Model}
 * method will force this object to load the referenced Model from the DB to interact with.
 */
public class ForeignKeyContainer<ModelClass extends Model> extends SimpleModelContainer<ModelClass, Map<String, Object>> {

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

    public ForeignKeyContainer(@NonNull ModelContainer<ModelClass, ?> existingContainer) {
        super(existingContainer);
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
    public boolean containsValue(String key) {
        return getData() != null && getData().containsKey(key) && getData().get(key) != null;
    }

    @Override
    public Object getValue(String key) {
        return getData().get(key);
    }

    @Override
    public void put(String columnName, Object value) {
        getData().put(columnName, value);
    }

    @Override
    public void setModel(ModelClass model) {
        super.setModel(model);


    }

    /**
     * Attemps to load the model from the DB using a {@link Select} query.
     *
     * @return the result of running a primary where query on the contained data.
     */
    public ModelClass load() {
        if (model == null && data != null) {
            model = new Select().from(modelAdapter.getModelClass()).where(modelContainerAdapter.getPrimaryConditionClause(this)).querySingle();
        }
        return model;
    }

    /**
     * @return forces a reload of the underlying model and returns it.
     */
    public ModelClass reload() {
        model = null;
        return load();
    }

    @Override
    public boolean exists() {
        ModelClass model = toModel();
        return model != null && modelAdapter.exists(model);
    }

    @Override
    public void save() {
        throw new InvalidMethodCallException("Cannot call save() on a foreign key container. Call load() instead");
    }

    @Override
    public void delete() {
        throw new InvalidMethodCallException("Cannot call delete() on a foreign key container. Call load() instead");
    }

    @Override
    public void update() {
        throw new InvalidMethodCallException("Cannot call update() on a foreign key container. Call load() instead");
    }

    @Override
    public void insert() {
        throw new InvalidMethodCallException("Cannot call insert() on a foreign key container. Call load() instead");
    }

    private static class InvalidMethodCallException extends RuntimeException {

        public InvalidMethodCallException(String detailMessage) {
            super(detailMessage);
        }
    }
}
