package com.raizlabs.android.dbflow.runtime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description: Directly notifies about model changes. Users should use {@link #get()} to use the shared
 * instance in {@link DatabaseConfig.Builder}
 */
@SuppressWarnings("unchecked")
public class DirectModelNotifier implements ModelNotifier {

    private static DirectModelNotifier notifier;

    @NonNull
    public static DirectModelNotifier get() {
        if (notifier == null) {
            notifier = new DirectModelNotifier();
        }
        return notifier;
    }

    public interface OnModelStateChangedListener<T> {

        void onModelChanged(@NonNull T model, @NonNull BaseModel.Action action);

    }

    public interface ModelChangedListener<T> extends OnModelStateChangedListener<T>, OnTableChangedListener {

    }

    private final Map<Class<?>, Set<OnModelStateChangedListener>> modelChangedListenerMap = new LinkedHashMap<>();

    private final Map<Class<?>, Set<OnTableChangedListener>> tableChangedListenerMap = new LinkedHashMap<>();


    private final TableNotifierRegister singleRegister = new DirectTableNotifierRegister();

    /**
     * Private constructor. Use shared {@link #get()} to ensure singular instance.
     */
    private DirectModelNotifier() {
        if (notifier != null) {
            throw new IllegalStateException("Cannot instantiate more than one DirectNotifier. Use DirectNotifier.get()");
        }
    }

    @Override
    public <T> void notifyModelChanged(@NonNull T model, @NonNull ModelAdapter<T> adapter,
                                       @NonNull BaseModel.Action action) {
        final Set<OnModelStateChangedListener> listeners = modelChangedListenerMap.get(adapter.getModelClass());
        if (listeners != null) {
            for (OnModelStateChangedListener listener : listeners) {
                if (listener != null) {
                    listener.onModelChanged(model, action);
                }
            }
        }
    }

    @Override
    public <T> void notifyTableChanged(@NonNull Class<T> table, @NonNull BaseModel.Action action) {
        final Set<OnTableChangedListener> listeners = tableChangedListenerMap.get(table);
        if (listeners != null) {
            for (OnTableChangedListener listener : listeners) {
                if (listener != null) {
                    listener.onTableChanged(table, action);
                }
            }
        }
    }

    @Override
    public TableNotifierRegister newRegister() {
        return singleRegister;
    }

    public <T> void registerForModelChanges(@NonNull Class<T> table,
                                            @NonNull ModelChangedListener<T> listener) {
        registerForModelStateChanges(table, listener);
        registerForTableChanges(table, listener);
    }

    public <T> void registerForModelStateChanges(@NonNull Class<T> table,
                                                 @NonNull OnModelStateChangedListener<T> listener) {
        Set<OnModelStateChangedListener> listeners = modelChangedListenerMap.get(table);
        if (listeners == null) {
            listeners = new LinkedHashSet<>();
            modelChangedListenerMap.put(table, listeners);
        }
        listeners.add(listener);
    }

    public <T> void registerForTableChanges(@NonNull Class<T> table,
                                            @NonNull OnTableChangedListener listener) {
        Set<OnTableChangedListener> listeners = tableChangedListenerMap.get(table);
        if (listeners == null) {
            listeners = new LinkedHashSet<>();
            tableChangedListenerMap.put(table, listeners);
        }
        listeners.add(listener);
    }

    public <T> void unregisterForModelChanges(@NonNull Class<T> table,
                                              @NonNull ModelChangedListener<T> listener) {
        unregisterForModelStateChanges(table, listener);
        unregisterForTableChanges(table, listener);
    }


    public <T> void unregisterForModelStateChanges(@NonNull Class<T> table,
                                                   @NonNull OnModelStateChangedListener<T> listener) {
        Set<OnModelStateChangedListener> listeners = modelChangedListenerMap.get(table);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public <T> void unregisterForTableChanges(@NonNull Class<T> table,
                                              @NonNull OnTableChangedListener listener) {
        Set<OnTableChangedListener> listeners = tableChangedListenerMap.get(table);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private class DirectTableNotifierRegister implements TableNotifierRegister {
        private List<Class> registeredTables = new ArrayList<>();

        @Nullable
        private OnTableChangedListener modelChangedListener;

        @Override
        public <T> void register(@NonNull Class<T> tClass) {
            registeredTables.add(tClass);
            registerForTableChanges(tClass, internalChangeListener);
        }

        @Override
        public <T> void unregister(@NonNull Class<T> tClass) {
            registeredTables.remove(tClass);
            unregisterForTableChanges(tClass, internalChangeListener);
        }

        @Override
        public void unregisterAll() {
            for (Class table : registeredTables) {
                unregisterForTableChanges(table, internalChangeListener);
            }
            this.modelChangedListener = null;
        }

        @Override
        public void setListener(@Nullable OnTableChangedListener modelChangedListener) {
            this.modelChangedListener = modelChangedListener;
        }

        @Override
        public boolean isSubscribed() {
            return !registeredTables.isEmpty();
        }

        private final OnTableChangedListener internalChangeListener
            = new OnTableChangedListener() {

            @Override
            public void onTableChanged(@Nullable Class<?> table, @NonNull BaseModel.Action action) {
                if (modelChangedListener != null) {
                    modelChangedListener.onTableChanged(table, action);
                }
            }
        };
    }

}
