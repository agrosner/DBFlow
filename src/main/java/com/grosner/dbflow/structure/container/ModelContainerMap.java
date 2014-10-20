package com.grosner.dbflow.structure.container;

import com.grosner.dbflow.structure.Model;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Description: Holds the static map for {@link com.grosner.dbflow.structure.container.ModelContainer} for
 * quick lookup on a model to see if there is a container for it.
 */
public class ModelContainerMap {

    static final HashMap<Class<?>,Class<? extends ModelContainer>> mMap = new HashMap<Class<?>, Class<? extends ModelContainer>>(){
        {
            put(JSONObject.class, JSONModel.class);
            put(Map.class, MapModel.class);
        }
    };

    /**
     * Whether there is a model container
     * @param value The value to check for its type
     * @return
     */
    public static boolean containsValue(Object value) {
        boolean contains = false;
        if(value != null){
            Set<Class<?>> keyset = mMap.keySet();
            for(Class<?> clazz: keyset) {
                if(clazz.isAssignableFrom(value.getClass())){
                    contains = true;
                    break;
                }
            }
        }

        return contains;
    }

    /**
     * Checks the value to return the appropriate model container class. We don't use reflection here as it may slow down processing.
     * @param table The model table to create the container for
     * @param value The value to check for its type
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ModelContainer<ModelClass, ?> getModelContainerInstance(Class<ModelClass> table, Object value) {
        ModelContainer<ModelClass, ?> modelContainer;
        if(value instanceof ModelContainer) {
            modelContainer = ((ModelContainer) value);
        } else  if(value instanceof JSONObject) {
            modelContainer = new JSONModel<ModelClass>(((JSONObject) value), table);
        } else if(Map.class.isAssignableFrom(value.getClass())) {
            modelContainer = new MapModel<ModelClass>(((Map<String, Object>) value), table);
        } else {
            modelContainer = null;
        }

        return modelContainer;
    }
}
