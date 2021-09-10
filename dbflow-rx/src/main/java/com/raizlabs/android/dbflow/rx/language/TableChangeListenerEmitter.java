package com.raizlabs.android.dbflow.rx.language;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.OnTableChangedListener;
import com.raizlabs.android.dbflow.runtime.TableNotifierRegister;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseModel;

import rx.Emitter;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Description: Emits when table changes occur for the related table on the {@link ModelQueriable}.
 * If the {@link ModelQueriable} relates to a {@link Join}, this can be multiple tables.
 */
public class TableChangeListenerEmitter<TModel> implements Action1<Emitter<ModelQueriable<TModel>>> {

    private final ModelQueriable<TModel> modelQueriable;

    public TableChangeListenerEmitter(ModelQueriable<TModel> modelQueriable) {
        this.modelQueriable = modelQueriable;
    }

    @Override
    public void call(Emitter<ModelQueriable<TModel>> modelQueriableEmitter) {
        modelQueriableEmitter.setSubscription(
            new FlowContentObserverSubscription(modelQueriableEmitter, modelQueriable.getTable()));
    }

    private class FlowContentObserverSubscription implements Subscription {

        private final Emitter<ModelQueriable<TModel>> modelQueriableEmitter;

        private final TableNotifierRegister register;

        private FlowContentObserverSubscription(
            Emitter<ModelQueriable<TModel>> modelQueriableEmitter, Class<TModel> table) {
            this.modelQueriableEmitter = modelQueriableEmitter;
            register = FlowManager.newRegisterForTable(table);

            From<TModel> from = null;
            if (modelQueriable instanceof From) {
                from = (From<TModel>) modelQueriable;
            } else if (modelQueriable instanceof Where
                && ((Where) modelQueriable).getWhereBase() instanceof From) {
                //noinspection unchecked
                from = (From<TModel>) ((Where) modelQueriable).getWhereBase();
            }

            // From could be part of many joins, so we register for all affected tables here.
            if (from != null) {
                java.util.Set<Class<?>> associatedTables = from.getAssociatedTables();
                for (Class<?> associated : associatedTables) {
                    register.register(associated);
                }
            } else {
                register.register(table);
            }

            register.setListener(onTableChangedListener);
        }

        @Override
        public void unsubscribe() {
            register.unregisterAll();
        }

        @Override
        public boolean isUnsubscribed() {
            return !register.isSubscribed();
        }

        private final OnTableChangedListener onTableChangedListener
            = new OnTableChangedListener() {
            @Override
            public void onTableChanged(@Nullable Class<?> tableChanged, @NonNull BaseModel.Action action) {
                if (modelQueriable.getTable().equals(tableChanged)) {
                    modelQueriableEmitter.onNext(modelQueriable);
                }
            }
        };
    }
}
