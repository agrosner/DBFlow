package com.raizlabs.android.dbflow.runtime;

/**
 * Description: Defines how {@link ModelNotifier} registers listeners. Abstracts that away.
 */
public interface TableNotifierRegister {

    <T> void register(Class<T> tClass);

    <T> void unregister(Class<T> tClass);

    void unregisterAll();

    void setListener(OnTableChangedListener listener);

    boolean isSubscribed();
}
