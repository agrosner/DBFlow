package com.raizlabs.android.dbflow.rx.language;

import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
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
            new FlowContentObserverSubscription(modelQueriableEmitter));
    }

    private class FlowContentObserverSubscription implements Subscription {

        private final Emitter<ModelQueriable<TModel>> modelQueriableEmitter;

        private final FlowContentObserver flowContentObserver = new FlowContentObserver();

        private FlowContentObserverSubscription(
            Emitter<ModelQueriable<TModel>> modelQueriableEmitter) {
            this.modelQueriableEmitter = modelQueriableEmitter;


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
                for (Class<?> table : associatedTables) {
                    flowContentObserver.registerForContentChanges(FlowManager.getContext(), table);
                }
            } else {
                flowContentObserver.registerForContentChanges(FlowManager.getContext(),
                    modelQueriable.getTable());
            }

            flowContentObserver.addOnTableChangedListener(onTableChangedListener);
        }

        @Override
        public void unsubscribe() {
            flowContentObserver.unregisterForContentChanges(FlowManager.getContext());
            flowContentObserver.removeTableChangedListener(onTableChangedListener);
        }

        @Override
        public boolean isUnsubscribed() {
            return !flowContentObserver.isSubscribed();
        }

        private final FlowContentObserver.OnTableChangedListener onTableChangedListener
            = new FlowContentObserver.OnTableChangedListener() {
            @Override
            public void onTableChanged(@Nullable Class<?> tableChanged, BaseModel.Action action) {
                if (modelQueriable.getTable().equals(tableChanged)) {
                    modelQueriableEmitter.onNext(modelQueriable);
                }
            }
        };
    }
}
