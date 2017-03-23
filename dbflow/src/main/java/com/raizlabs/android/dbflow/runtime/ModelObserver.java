package com.raizlabs.android.dbflow.runtime;

/**
 * Description: Main interface by which observers listen for changes thrown through {@link ModelNotifier}.
 */
public interface ModelObserver {

    void beginTransaction();

    void endTransactionAndNotify();

    <T> void registerForModelChanges(Class<T> table);

    <T> void unregisterForModelChanges(Class<T> table);
}
