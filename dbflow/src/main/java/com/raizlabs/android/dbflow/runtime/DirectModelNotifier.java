package com.raizlabs.android.dbflow.runtime;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Description: Directly notifies about model changes. Users should use {@link #get()} to use the shared
 * instance in {@link DatabaseConfig.Builder}
 */
@SuppressWarnings("unchecked")
public class DirectModelNotifier implements ModelNotifier {

    private static DirectModelNotifier notifier;

    public static DirectModelNotifier get() {
        if (notifier == null) {
            notifier = new DirectModelNotifier();
        }
        return notifier;
    }

    public interface ModelChangedListener<T> {

        void onModelChanged(T model, BaseModel.Action action);

        void onTableChanged(Class<T> table, BaseModel.Action action);
    }

    private final Map<Class<?>, Set<ModelChangedListener>> modelChangedListenerMap = new LinkedHashMap<>();

    @Override
    public <T> void notifyModelChanged(@Nullable T model, @NonNull ModelAdapter<T> adapter,
                                       @NonNull BaseModel.Action action) {
        final Set<ModelChangedListener> listeners = modelChangedListenerMap.get(adapter.getModelClass());
        if (listeners != null) {
            for (ModelChangedListener listener : listeners) {
                if (listener != null) {
                    listener.onModelChanged(model, action);
                }
            }
        }
    }

    @Override
    public <T> void notifyTableChanged(@NonNull Class<T> table, @NonNull BaseModel.Action action) {
        final Set<ModelChangedListener> listeners = modelChangedListenerMap.get(table);
        if (listeners != null) {
            for (ModelChangedListener listener : listeners) {
                if (listener != null) {
                    listener.onTableChanged(table, action);
                }
            }
        }
    }

    public <T> void registerForModelChanges(Class<T> table, ModelChangedListener<T> listener) {
        Set<ModelChangedListener> listeners = modelChangedListenerMap.get(table);
        if (listeners == null) {
            listeners = new LinkedHashSet<>();
            modelChangedListenerMap.put(table, listeners);
        }
        listeners.add(listener);
    }

    public <T> void unregisterForModelChanges(Class<T> table, ModelChangedListener<T> listener) {
        Set<ModelChangedListener> listeners = modelChangedListenerMap.get(table);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
}
