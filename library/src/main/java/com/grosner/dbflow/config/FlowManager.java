package com.grosner.dbflow.config;

import android.content.Context;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.structure.InvalidDBConfiguration;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelAdapter;
import com.grosner.dbflow.structure.ModelPathManager;
import com.grosner.dbflow.structure.container.ContainerAdapter;

import java.util.HashMap;

/**
 * Author: andrewgrosner
 * Description: Holds information about the database and wraps some of the methods.
 */
public class FlowManager {

    private static HashMap<Class<? extends Model>, BaseFlowManager> mManagerMap = new HashMap<>();

    private static Context context;

    private static FlowStaticManagerInterface mStaticManager;

    /**
     * Returns the table name for the specific model class
     *
     * @param table The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return The table name, which can be different than the {@link com.grosner.dbflow.structure.Model} class name
     */
    public static String getTableName(Class<? extends Model> table) {
        return FlowManager.getModelAdapter(table).getTableName();
    }
    static void setStaticManagerInterface(FlowStaticManagerInterface staticManagerInterface) {
        mStaticManager = staticManagerInterface;
    }

    /**
     * Returns the corresponding {@link com.grosner.dbflow.config.FlowManager} for the specified model
     *
     * @param table
     * @return
     */
    public static BaseFlowManager getManagerForTable(Class<? extends Model> table) {
        BaseFlowManager flowManager = mManagerMap.get(table);
        if (flowManager == null) {
            throw new InvalidDBConfiguration();
        }
        return flowManager;
    }

    /**
     * Returns the primary where query for a specific table. Its the WHERE statement containing columnName = ?.
     *
     * @param table The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return The primary where query
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ConditionQueryBuilder<ModelClass> getPrimaryWhereQuery(Class<ModelClass> table) {
        return getManagerForTable(table).getModelAdapterForTable(table).getPrimaryModelWhere();
    }

    /**
     * Puts which manager corresponds to the model class.
     *
     * @param modelClass
     * @param manager
     */
     static void putManagerForTable(Class<? extends Model> modelClass, BaseFlowManager manager) {
        mManagerMap.put(modelClass, manager);
    }

    /**
     * Will throw an exception if this class is not initialized yet in {@link #initialize(android.content.Context, DBConfiguration)}
     *
     * @return
     */
    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException("Context cannot be null for FlowManager");
        }
        return context;
    }

    public static void setContext(Context context) {
        FlowManager.context = context;
        ModelPathManager.addPath(context.getPackageName());
    }

    /**
     * Returns the specific {@link com.grosner.dbflow.converter.TypeConverter} for this model. It defines
     * how the class is stored in the DB
     *
     * @param modelClass   The class that implements {@link com.grosner.dbflow.structure.Model}
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return
     */
    public static TypeConverter getTypeConverterForClass(Class<?> modelClass) {
        return mStaticManager.getTypeConverterForClass(modelClass);
    }

    // region Getters

    /**
     * Releases references to the structure, configuration, and closes the DB.
     */
    public static synchronized void destroy() {
        context = null;
    }

    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ModelAdapter<ModelClass > getModelAdapter(Class<ModelClass> modelClass) {
        return FlowManager.getManagerForTable(modelClass).getModelAdapterForTable(modelClass);
    }

    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ContainerAdapter<ModelClass> getContainerAdapter(Class<ModelClass> modelClass) {
        return FlowManager.getManagerForTable(modelClass).getModelContainerAdapterForTable(modelClass);
    }

    // endregion

}
