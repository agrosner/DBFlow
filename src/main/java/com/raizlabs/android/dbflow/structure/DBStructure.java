package com.raizlabs.android.dbflow.structure;

import android.location.Location;

import com.raizlabs.android.dbflow.config.DBConfiguration;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.converter.CalendarConverter;
import com.raizlabs.android.dbflow.converter.DateConverter;
import com.raizlabs.android.dbflow.converter.DefaultForeignKeyConverter;
import com.raizlabs.android.dbflow.converter.ForeignKeyConverter;
import com.raizlabs.android.dbflow.converter.JsonConverter;
import com.raizlabs.android.dbflow.converter.LocationConverter;
import com.raizlabs.android.dbflow.converter.SqlDateConverter;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.sql.builder.AbstractWhereQueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.PrimaryWhereQueryBuilder;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class defines the structure of the DB. It contains tables, type converters,
 * and other information pertaining to this DB.
 */
public class DBStructure {

    private Map<Class<? extends Model>, TableStructure> mTableStructure;

    private Map<Class<?>, TypeConverter> mTypeConverters = new HashMap<Class<?>, TypeConverter>() {
        {
            put(Calendar.class, new CalendarConverter());
            put(java.sql.Date.class, new SqlDateConverter());
            put(java.util.Date.class, new DateConverter());
            put(Location.class, new LocationConverter());
            put(JSONObject.class, new JsonConverter());
        }
    };

    private Map<Class<? extends ModelView>, ModelView> mModelViews;

    private Map<Class<?>, ForeignKeyConverter> mForeignKeyConverters = new HashMap<Class<?>, ForeignKeyConverter>();

    private Map<Class<? extends Model>, PrimaryWhereQueryBuilder> mPrimaryWhereQueryBuilderMap;


    public DBStructure(DBConfiguration dbConfiguration) {
        mTableStructure = new HashMap<Class<? extends Model>, TableStructure>();
        mPrimaryWhereQueryBuilderMap = new HashMap<Class<? extends Model>, PrimaryWhereQueryBuilder>();
        mModelViews = new HashMap<Class<? extends ModelView>, ModelView>();

        initializeStructure(dbConfiguration);
    }

    /**
     * This will construct the runtime structure of our DB for reference while the app is running.
     *
     * @param dbConfiguration
     */
    private void initializeStructure(DBConfiguration dbConfiguration) {
        List<Class<? extends Model>> modelList = null;
        if (dbConfiguration.hasModelClasses()) {
            modelList = dbConfiguration.getModelClasses();
        } else {
            try {
                modelList = StructureUtils.generateModelFromSource();
            } catch (IOException e) {
                FlowLog.logError(e);
            }
        }

        if (modelList != null) {
            for (Class<? extends Model> modelClass : modelList) {
                @SuppressWarnings("unchecked")
                TableStructure tableStructure = new TableStructure(modelClass);
                mTableStructure.put(modelClass, tableStructure);
            }
        }
    }

    public TableStructure getTableStructureForClass(Class<? extends Model> modelClass) {
        return getTableStructure().get(modelClass);
    }

    @SuppressWarnings("unchecked")
    public <ModelClass> TypeConverter<?, ModelClass> getTypeConverterForClass(Class<ModelClass> modelClass) {
        return mTypeConverters.get(modelClass);
    }

    void putTypeConverterForClass(Class typeConverterClass) {
        try {
            TypeConverter typeConverter = (TypeConverter) typeConverterClass.newInstance();
            mTypeConverters.put(typeConverter.getModelType(), typeConverter);
        } catch (Throwable e) {
            FlowLog.logError(e);
        }
    }

    /**
     * Returns the Where Primary key query string from the cache for a specific model.
     *
     * @param modelTable
     * @return
     */
    @SuppressWarnings("unchecked")
    public <ModelClass extends Model> AbstractWhereQueryBuilder<ModelClass> getPrimaryWhereQuery(Class<ModelClass> modelTable) {
        AbstractWhereQueryBuilder<ModelClass> abstractWhereQueryBuilder = getWhereQueryBuilderMap().get(modelTable);
        if (abstractWhereQueryBuilder == null) {
            abstractWhereQueryBuilder = new PrimaryWhereQueryBuilder<ModelClass>(modelTable);
            getWhereQueryBuilderMap().put(modelTable, (PrimaryWhereQueryBuilder) abstractWhereQueryBuilder);
        }
        return abstractWhereQueryBuilder;
    }

    /**
     * Returns either the specified {@link com.raizlabs.android.dbflow.converter.ForeignKeyConverter} or the
     * {@link com.raizlabs.android.dbflow.converter.DefaultForeignKeyConverter} if not found.
     * @param modelClass
     * @return
     */
    public ForeignKeyConverter getForeignKeyConverterForclass(Class<? extends Model> modelClass) {
        ForeignKeyConverter foreignKeyConverter = getForeignKeyConverterMap().get(modelClass);
        if (foreignKeyConverter == null) {
            foreignKeyConverter = DefaultForeignKeyConverter.getSharedConverter();
        }
        return foreignKeyConverter;
    }

    public void putForeignKeyConverterForClass(Class<? extends ForeignKeyConverter> foreignKeyConverterClass) {
        try {
            ForeignKeyConverter foreignKeyConverter = foreignKeyConverterClass.newInstance();
            mForeignKeyConverters.put(foreignKeyConverter.getModelClass(), foreignKeyConverter);
        } catch (Throwable e) {
            FlowLog.logError(e);
        }
    }

    public Map<Class<? extends Model>, TableStructure> getTableStructure() {
        return mTableStructure;
    }

    public Map<Class<? extends Model>, PrimaryWhereQueryBuilder> getWhereQueryBuilderMap() {
        return mPrimaryWhereQueryBuilderMap;
    }

    public Map<Class<?>, ForeignKeyConverter> getForeignKeyConverterMap() {
        return mForeignKeyConverters;
    }

    public Map<Class<? extends ModelView>, ModelView> getModelViews() {
        return mModelViews;
    }
}
