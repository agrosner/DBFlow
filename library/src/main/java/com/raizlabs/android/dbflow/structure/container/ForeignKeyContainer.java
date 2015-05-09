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

    /**
     * Attemps to load the model from the DB using a {@link Select} query.
     *
     * @return the result of running a primary where query on the contained data.
     */
    public ModelClass load() {
        if (model == null && data != null) {
            model = new Select().from(modelAdapter.getModelClass())
                    .where(modelContainerAdapter.getPrimaryModelWhere(this)).limit(1).querySingle();
        }
        return model;
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
