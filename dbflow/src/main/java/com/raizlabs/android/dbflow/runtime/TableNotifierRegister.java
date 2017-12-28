package com.raizlabs.android.dbflow.runtime;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Description: Defines how {@link ModelNotifier} registers listeners. Abstracts that away.
 */
public interface TableNotifierRegister {

    <T> void register(@NonNull Class<T> tClass);

    <T> void unregister(@NonNull Class<T> tClass);

    void unregisterAll();

    void setListener(@Nullable OnTableChangedListener listener);

    boolean isSubscribed();
}
