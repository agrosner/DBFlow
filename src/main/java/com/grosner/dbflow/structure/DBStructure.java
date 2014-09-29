package com.grosner.dbflow.structure;

import android.content.Context;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.converter.DefaultForeignKeyConverter;
import com.grosner.dbflow.converter.ForeignKeyConverter;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.observer.ModelObserver;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
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
     * Holds onto the {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder} for each {@link com.grosner.dbflow.structure.Model}
     * class so we only created these once. Useful for many select statements on a specific table.
     */
    private Map<Class<? extends Model>, ConditionQueryBuilder> mPrimaryWhereQueryBuilderMap;

    /**
     * Holds onto the {@link java.lang.reflect.Constructor} for each {@link com.grosner.dbflow.structure.Model}
     * so we only need to retrieve these once to improve performance. This is used when we convert a {@link android.database.Cursor}
     * to a {@link com.grosner.dbflow.structure.Model}
     */
    private Map<Class<? extends Model>, Constructor<? extends Model>> mModelConstructorMap;

    /**
     * Holds onto {@link com.grosner.dbflow.runtime.observer.ModelObserver} for each {@link com.grosner.dbflow.structure.Model}
     */
    private Map<Class<? extends Model>, List<ModelObserver<? extends Model>>> mModelObserverMap;

    /**
     * Holds the database information here.
     */
    private FlowManager mManager;

    /**
     * Will ignore subsequent calls to reset DB when this is true
     */
    private boolean isResetting = false;

    /**
     * Constructs a new instance with the specified {@link com.grosner.dbflow.config.FlowManager}
     * and {@link com.grosner.dbflow.config.DBConfiguration}
     *
     * @param flowManager     The db manager
     * @param dbConfiguration The configuration for this db
     */
    public DBStructure(FlowManager flowManager) {
        mManager = flowManager;
        initializeStructure(flowManager.getDbConfiguration());
    }

    /**
     * This will construct the runtime structure of our DB for reference while the app is running.
     *
     * @param dbConfiguration The configuration for this db
     */
    private void initializeStructure(DBConfiguration dbConfiguration) {
        mTableStructure = new HashMap<Class<? extends Model>, TableStructure>();
        mPrimaryWhereQueryBuilderMap = new HashMap<Class<? extends Model>, ConditionQueryBuilder>();
        mModelViews = new HashMap<Class<? extends ModelView>, ModelView>();
        mModelConstructorMap = new HashMap<Class<? extends Model>, Constructor<? extends Model>>();
        mModelObserverMap = new HashMap<Class<? extends Model>, List<ModelObserver<? extends Model>>>();

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
     * This will delete and recreate the whole stored database. WARNING: all data stored will be lost.
     * @param context The applications context
     */
    public void reset(Context context) {
        if(!isResetting) {
            isResetting = true;
            context.deleteDatabase(mManager.getDbConfiguration().getDatabaseName());
            initializeStructure(mManager.getDbConfiguration());
            isResetting = false;
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
    public <ModelClass extends Model> ConditionQueryBuilder<ModelClass> getPrimaryWhereQuery(Class<ModelClass> modelTable) {
        ConditionQueryBuilder<ModelClass> conditionQueryBuilder = getWhereQueryBuilderMap().get(modelTable);
        if (conditionQueryBuilder == null) {
            conditionQueryBuilder = new ConditionQueryBuilder<ModelClass>(mManager, modelTable).emptyPrimaryConditions();
            getWhereQueryBuilderMap().put(modelTable, conditionQueryBuilder);
        }
        return conditionQueryBuilder;
    }


    /**
     * Returns the {@link java.lang.reflect.Constructor} for the {@link ModelClass}. It will add the constructor
     * to the map if it does not exist there already.
     *
     * @param modelClass
     * @param <ModelClass>
     * @return
     * @throws java.lang.RuntimeException when the default constructor does not exist.
     */
    @SuppressWarnings("unchecked")
    public <ModelClass extends Model> Constructor<ModelClass> getConstructorForModel(Class<ModelClass> modelClass) {
        Constructor<ModelClass> constructor = (Constructor<ModelClass>) getModelConstructorMap().get(modelClass);
        if (constructor == null) {
            try {
                constructor = modelClass.getConstructor();

                //enable private constructors
                constructor.setAccessible(true);
                getModelConstructorMap().put(modelClass, constructor);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        return constructor;
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
     * Returns the associated {@link com.grosner.dbflow.runtime.observer.ModelObserver}s for the class
     *
     * @param modelClass
     * @return
     */
    public List<ModelObserver<? extends Model>> getModelObserverListForClass(Class<? extends Model> modelClass) {
        return getModelObserverMap().get(modelClass);
    }

    /**
     * Adds a {@link com.grosner.dbflow.runtime.observer.ModelObserver} to the structure
     *
     * @param modelObserver A model observer to listen for model changes/updates
     */
    @SuppressWarnings("unchecked")
    public void addModelObserverForClass(ModelObserver<? extends Model> modelObserver) {
        synchronized (mModelObserverMap) {
            List<ModelObserver<? extends Model>> modelObservers = getModelObserverListForClass(modelObserver.getModelClass());
            if (modelObservers == null) {
                modelObservers = new ArrayList<ModelObserver<? extends Model>>();
                getModelObserverMap().put(modelObserver.getModelClass(), modelObservers);
            }
            if (!modelObservers.contains(modelObserver)) {
                modelObservers.add(modelObserver);
            }
        }
    }

    /**
     * Removes a {@link com.grosner.dbflow.runtime.observer.ModelObserver} from this structure.
     *
     * @param modelObserver A model observer to listen for model changes/updates
     */
    @SuppressWarnings("unchecked")
    public void removeModelObserverForClass(ModelObserver<? extends Model> modelObserver) {
        synchronized (mModelObserverMap) {
            List<ModelObserver<? extends Model>> modelObservers = getModelObserverListForClass(modelObserver.getModelClass());
            if (modelObservers != null) {
                modelObservers.remove(modelObserver);

                if (modelObservers.isEmpty()) {
                    getModelObserverMap().remove(modelObserver.getModelClass());
                }
            }
        }
    }

    /**
     * Performs the listener event
     *
     * @param model
     */
    @SuppressWarnings("unchecked")
    public void fireModelChanged(final Model model, final ModelObserver.Mode mode) {
        synchronized (mModelObserverMap) {
            final List<ModelObserver<? extends Model>> modelObserverList = getModelObserverListForClass(model.getClass());
            if (!modelObserverList.isEmpty()) {
                TransactionManager.getInstance().processOnRequestHandler(new Runnable() {
                    @Override
                    public void run() {
                        for (ModelObserver modelObserver : modelObserverList) {
                            modelObserver.onModelChanged(mManager, model, mode);
                        }
                    }
                });
            }
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
     * Returns the {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder} map for this database
     *
     * @return
     */
    public Map<Class<? extends Model>, ConditionQueryBuilder> getWhereQueryBuilderMap() {
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

    public Map<Class<? extends Model>, Constructor<? extends Model>> getModelConstructorMap() {
        return mModelConstructorMap;
    }

    public Map<Class<? extends Model>, List<ModelObserver<? extends Model>>> getModelObserverMap() {
        return mModelObserverMap;
    }
}
