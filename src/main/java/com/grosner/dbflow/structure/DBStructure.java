package com.grosner.dbflow.structure;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.converter.DefaultForeignKeyConverter;
import com.grosner.dbflow.converter.ForeignKeyConverter;
import com.grosner.dbflow.sql.builder.WhereQueryBuilder;

import java.io.IOException;
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

    /**
     * Holds onto the {@link com.grosner.dbflow.structure.TableStructure} for each Model class
     */
    private Map<Class<? extends Model>, TableStructure> mTableStructure;

    /**
     * Holds onto the {@link com.grosner.dbflow.structure.ModelView} for each class
     */
    private Map<Class<? extends ModelView>, ModelView> mModelViews;

    /**
     * Holds onto the {@link com.grosner.dbflow.converter.ForeignKeyConverter} for each specified class
     */
    private Map<Class<?>, ForeignKeyConverter> mForeignKeyConverters = new HashMap<Class<?>, ForeignKeyConverter>();

    /**
     * Holds onto the {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder} for each {@link com.grosner.dbflow.structure.Model}
     * class so we only created these once. Useful for many select statements on a specific table.
     */
    private Map<Class<? extends Model>, WhereQueryBuilder> mPrimaryWhereQueryBuilderMap;

    /**
     * Holds the database information here.
     */
    private FlowManager mManager;

    /**
     * Constructs a new instance with the specified {@link com.grosner.dbflow.config.FlowManager}
     * and {@link com.grosner.dbflow.config.DBConfiguration}
     *
     * @param flowManager     The db manager
     * @param dbConfiguration The configuration for this db
     */
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
     * @param dbConfiguration The configuration for this db
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

    /**
     * Returns a {@link com.grosner.dbflow.structure.TableStructure} for a specific model class
     *
     * @param modelClass   The table class we want to retrieve
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return the table structure for this model class
     */
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
     *
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

    /**
     * Adds a {@link com.grosner.dbflow.converter.ForeignKeyConverter} to be referenced later
     *
     * @param foreignKeyConverterClass
     */
    public void putForeignKeyConverterForClass(Class<? extends ForeignKeyConverter> foreignKeyConverterClass) {
        try {
            ForeignKeyConverter foreignKeyConverter = foreignKeyConverterClass.newInstance();
            mForeignKeyConverters.put(foreignKeyConverter.getModelClass(), foreignKeyConverter);
        } catch (Throwable e) {
            FlowLog.logError(e);
        }
    }

    /**
     * Returns the {@link com.grosner.dbflow.structure.TableStructure} map for this database
     *
     * @return
     */
    public Map<Class<? extends Model>, TableStructure> getTableStructure() {
        return mTableStructure;
    }

    /**
     * Returns the {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder} map for this database
     *
     * @return
     */
    public Map<Class<? extends Model>, WhereQueryBuilder> getWhereQueryBuilderMap() {
        return mPrimaryWhereQueryBuilderMap;
    }

    /**
     * Returns the {@link com.grosner.dbflow.converter.ForeignKeyConverter} map for this database
     *
     * @return
     */
    public Map<Class<?>, ForeignKeyConverter> getForeignKeyConverterMap() {
        return mForeignKeyConverters;
    }

    /**
     * Returns the {@link com.grosner.dbflow.structure.ModelView} map for this database
     *
     * @return
     */
    public Map<Class<? extends ModelView>, ModelView> getModelViews() {
        return mModelViews;
    }
}
