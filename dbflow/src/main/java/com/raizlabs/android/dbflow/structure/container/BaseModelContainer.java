package com.raizlabs.android.dbflow.structure.container;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.InvalidDBConfiguration;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.Iterator;

/**
 * Description: The base class that all ModelContainers should extend.
 */
public abstract class BaseModelContainer<TModel extends Model, DataClass> implements ModelContainer<TModel, DataClass>, Model {

    /**
     * The {@link TModel} that the json corresponds to. Use {@link #toModel()} to retrieve this value.
     */
    TModel model;

    /**
     * The {@link com.raizlabs.android.dbflow.structure.ModelAdapter} that is defined for this {@link org.json.JSONObject}
     */
    ModelAdapter<TModel> modelAdapter;

    ModelContainerAdapter<TModel> modelContainerAdapter;

    /**
     * The data thats stored in the container
     */
    DataClass data;

    public BaseModelContainer(Class<TModel> table) {
        modelAdapter = FlowManager.getModelAdapter(table);
        modelContainerAdapter = FlowManager.getContainerAdapter(table);
        if (modelContainerAdapter == null) {
            throw new InvalidDBConfiguration("The table " + FlowManager.getTableName(table) + " did not specify the " +
                    com.raizlabs.android.dbflow.annotation.ModelContainer.class.getSimpleName() + " annotation." +
                    " Please decorate " + table.getName() +
                    " with annotation @" + com.raizlabs.android.dbflow.annotation.ModelContainer.class.getSimpleName() + ".");
        }
    }

    public BaseModelContainer(Class<TModel> table, DataClass data) {
        this(table);
        this.data = data;
    }

    /**
     * Copy constructor from one {@link ModelContainer} into this {@link ModelContainer}. It simply
     * places the values from the container into this object.
     *
     * @param existingContainer The existing container to load into this one.
     */
    public BaseModelContainer(@NonNull ModelContainer<TModel, ?> existingContainer) {
        this(existingContainer.getTable());

        Iterator<String> keys = existingContainer.iterator();
        if (keys != null) {
            while (keys.hasNext()) {
                String key = keys.next();
                put(key, existingContainer.getValue(key));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getTypeConvertedPropertyValue(Class<T> type, String key) {
        Object value = getValue(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        } else {
            TypeConverter<Object, T> converter = FlowManager.getTypeConverterForClass(type);
            if (converter != null) {
                return converter.getModelValue(value);
            } else {
                return null;
            }
        }
    }

    @Nullable
    @Override
    public TModel toModel() {
        if (model == null && data != null) {
            model = modelContainerAdapter.toModel(this);
        }
        return model;
    }

    @Nullable
    @Override
    public TModel toModelForce() {
        model = null;
        return toModel();
    }

    @Nullable
    @Override
    public TModel getModel() {
        return model;
    }

    /**
     * Sets a model to back the container. NOTE: this method invalidates results in any underlying data to
     * be ignored. To resume using this underlying data, set this method wil a null param.
     *
     * @param model
     */
    public void setModel(TModel model) {
        this.model = model;
    }

    /**
     * Invalidates the underlying model. In the next {@link #toModel()} call, it will re-query the underlying data
     * and recreate a {@link TModel}.
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

    @Nullable
    @Override
    public DataClass getData() {
        return data;
    }

    /**
     * Sets the data for this container
     *
     * @param data The data object that backs this container
     */
    @Override
    public void setData(DataClass data) {
        this.data = data;
        model = null;
    }

    @Override
    public abstract Object getValue(String key);

    @Override
    public Object getValue(IProperty property) {
        return getValue(property.getContainerKey());
    }

    @Override
    public abstract void put(String columnName, Object value);

    @Override
    public void put(IProperty property, Object value) {
        put(property.getContainerKey(), value);
    }

    @Override
    public void putDefault(String columnName) {
        Class columnClass = modelContainerAdapter.getColumnMap().get(columnName);
        if (columnClass.isPrimitive()) {
            if (columnClass.equals(boolean.class)) {
                put(columnName, false);
            } else if (columnClass.equals(char.class)) {
                put(columnName, '\u0000');
            } else {
                put(columnName, 0);
            }
        } else {
            put(columnName, null);
        }
    }

    @Override
    public void putDefault(IProperty property) {
        putDefault(property.getContainerKey());
    }

    @Override
    public ModelAdapter<TModel> getModelAdapter() {
        return modelAdapter;
    }

    @Override
    public Class<TModel> getTable() {
        return modelAdapter.getModelClass();
    }

    @Override
    public void save() {
        modelContainerAdapter.save(this);
    }

    @Override
    public void delete() {
        modelContainerAdapter.delete(this);
    }

    @Override
    public void update() {
        modelContainerAdapter.update(this);
    }

    @Override
    public void insert() {
        modelContainerAdapter.insert(this);
    }

    @Override
    public boolean exists() {
        return modelContainerAdapter.exists(this);
    }
}
