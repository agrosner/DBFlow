package com.grosner.dbflow.structure;

import android.location.Location;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.converter.CalendarConverter;
import com.grosner.dbflow.converter.DateConverter;
import com.grosner.dbflow.converter.DefaultForeignKeyConverter;
import com.grosner.dbflow.converter.ForeignKeyConverter;
import com.grosner.dbflow.converter.JsonConverter;
import com.grosner.dbflow.converter.LocationConverter;
import com.grosner.dbflow.converter.SqlDateConverter;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.sql.builder.WhereQueryBuilder;

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

    private Map<Class<? extends ModelView>, ModelView> mModelViews;

    private Map<Class<?>, ForeignKeyConverter> mForeignKeyConverters = new HashMap<Class<?>, ForeignKeyConverter>();

    private Map<Class<? extends Model>, WhereQueryBuilder> mPrimaryWhereQueryBuilderMap;

    private FlowManager mManager;

    public DBStructure(FlowManager flowManager, DBConfiguration dbConfiguration) {
        mManager = flowManager;
        mTableStructure = new HashMap<Class<? extends Model>, TableStructure>();
        mPrimaryWhereQueryBuilderMap = new HashMap<Class<? extends Model>, WhereQueryBuilder>();
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
                modelList = StructureUtils.generateModelFromSource(mManager);
            } catch (IOException e) {
                FlowLog.logError(e);
            }
        }

        if (modelList != null) {
            for (Class<? extends Model> modelClass : modelList) {
                @SuppressWarnings("unchecked")
                TableStructure tableStructure = new TableStructure(mManager, modelClass);
                mTableStructure.put(modelClass, tableStructure);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <ModelClass extends Model> TableStructure<ModelClass> getTableStructureForClass(Class<ModelClass> modelClass) {
        return getTableStructure().get(modelClass);
    }

    /**
     * Returns the Where Primary key query string from the cache for a specific model.
     *
     * @param modelTable
     * @return
     */
    @SuppressWarnings("unchecked")
    public <ModelClass extends Model> WhereQueryBuilder<ModelClass> getPrimaryWhereQuery(Class<ModelClass> modelTable) {
        WhereQueryBuilder<ModelClass> whereQueryBuilder = getWhereQueryBuilderMap().get(modelTable);
        if (whereQueryBuilder == null) {
            whereQueryBuilder = new WhereQueryBuilder<ModelClass>(mManager, modelTable).emptyPrimaryParams();
            getWhereQueryBuilderMap().put(modelTable, whereQueryBuilder);
        }
        return whereQueryBuilder;
    }

    /**
     * Returns either the specified {@link com.grosner.dbflow.converter.ForeignKeyConverter} or the
     * {@link com.grosner.dbflow.converter.DefaultForeignKeyConverter} if not found.
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

    public Map<Class<? extends Model>, WhereQueryBuilder> getWhereQueryBuilderMap() {
        return mPrimaryWhereQueryBuilderMap;
    }

    public Map<Class<?>, ForeignKeyConverter> getForeignKeyConverterMap() {
        return mForeignKeyConverters;
    }

    public Map<Class<? extends ModelView>, ModelView> getModelViews() {
        return mModelViews;
    }
}
