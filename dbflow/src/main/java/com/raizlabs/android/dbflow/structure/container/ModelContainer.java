package com.raizlabs.android.dbflow.structure.container;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.Iterator;

/**
 * Description: The primary interface for converting data that acts like a {@link Model} into a model object
 * that it corresponds to. It is also used in {@link ForeignKey} to save and retrieve values of objects.
 */
public interface ModelContainer<TModel extends Model, DataClass> extends Model {

    /**
     * Will convert the object into {@link TModel}. It simply converts whatever is stored in this object
     * into a {@link TModel} representation of it. If fields are missing, they will be empty or null.
     *
     * @return A model from this object's data. This will be cached for performance. To force a reload call {@link #toModelForce()}.
     */
    @Nullable
    TModel toModel();

    /**
     * Forces a re-conversion of the underlying data into a {@link TModel}/
     *
     * @return The model from this object's data.
     */
    @Nullable
    TModel toModelForce();

    /**
     * @return The model object contained (if converted).
     */
    @Nullable
    TModel getModel();

    /**
     * @return The underlying data object that this container uses to imitate a model.
     */
    @Nullable
    DataClass getData();

    /**
     * Changes the underlying data. This method should also invalidate the {@link TModel} in this container.
     *
     * @param data The data to back this object.
     */
    void setData(DataClass data);

    /**
     * @return New instance of underlying data for parsing
     */
    @NonNull
    DataClass newDataInstance();

    /**
     * @param inValue     The data to construct a new instance from
     * @param columnClass The Model type
     * @return new instance of this container with a different Model class when we have
     * children who are {@link Model}
     */
    BaseModelContainer getInstance(Object inValue, Class<? extends Model> columnClass);

    /**
     * @param key The key to lookup.
     * @return true if this container contains a value with the specified key. Nulls do not count.
     */
    boolean containsValue(String key);

    @Nullable
    Iterator<String> iterator();

    /**
     * @param key The key in the container.
     * @return the value with the specified key
     */
    Object getValue(String key);

    /**
     * @param property The property to retrieve.
     * @return The value with the specified property {@link IProperty#getContainerKey()}.
     */
    Object getValue(IProperty property);

    /**
     * Converts the value into a safe value that's represented by the return type.
     * If the class has a type converter, it will use this method to upconvert the value
     * if its not already represented by its type-converter.
     *
     * @param type The type of the value to get.
     * @param key  The key to use.
     * @param <T>  The return type we cast the return to.
     * @return The value that we use to compare in properties.
     */
    <T> T getTypeConvertedPropertyValue(Class<T> type, String key);

    /**
     * @param key The key in the container.
     * @return The value converted into an {@link Integer}.
     */
    Integer getIntegerValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as an int.
     */
    int getIntValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link Long}.
     */
    Long getLongValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link long}.
     */
    long getLngValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link Boolean}.
     */
    Boolean getBooleanValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a a {@link boolean}.
     */
    boolean getBoolValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link String}.
     */
    String getStringValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link Float}.
     */
    Float getFloatValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link float}.
     */
    float getFltValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link Double}.
     */
    Double getDoubleValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link double}.
     */
    double getDbleValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link Short}.
     */
    Short getShortValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link short}.
     */
    short getShrtValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link Byte[]}.
     */
    Byte[] getBlobValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link byte[]}.
     */
    byte[] getBlbValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link Byte}.
     */
    Byte getByteValue(String key);

    /**
     * @param key The key in the container.
     * @return The value evaluated as a {@link byte}.
     */
    byte getBytValue(String key);

    /**
     * Puts the value with the specified key and value to the object
     *
     * @param columnName The name of the column
     * @param value      The value of the item in Model form.
     */
    void put(String columnName, Object value);

    /**
     * Puts a {@link Property} with the specified key and value to the object.
     *
     * @param property The property to put.
     * @param value    The value for the property.
     */
    void put(IProperty property, Object value);

    /**
     * Puts the default value of this {@link ModelContainer} as data. It may be null for objects,
     * or 0 for primitives.
     *
     * @param columnName The name of the column to put.
     */
    void putDefault(String columnName);

    /**
     * Puts the default value of this {@link ModelContainer} as data. It may be null for objects,
     * or 0 for primitives.
     *
     * @param property The property to put.
     */
    void putDefault(IProperty property);

    /**
     * @return The associated model adapter from the table for this {@link ModelContainer}
     */
    ModelAdapter<TModel> getModelAdapter();

    /**
     * @return the table that's associated with this container
     */
    Class<TModel> getTable();
}
