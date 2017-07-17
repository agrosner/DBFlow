package com.raizlabs.android.dbflow.rx2.language;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.OnTableChangedListener;
import com.raizlabs.android.dbflow.runtime.TableNotifierRegister;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseModel;

import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.disposables.Disposables;

/**
 * Description: Emits when table changes occur for the related table on the {@link ModelQueriable}.
 * If the {@link ModelQueriable} relates to a {@link Join}, this can be multiple tables.
 */
public class TableChangeOnSubscribe<TModel> implements FlowableOnSubscribe<ModelQueriable<TModel>> {

    private final ModelQueriable<TModel> modelQueriable;

    private final TableNotifierRegister register;
    private FlowableEmitter<ModelQueriable<TModel>> flowableEmitter;

    public TableChangeOnSubscribe(ModelQueriable<TModel> modelQueriable) {
        this.modelQueriable = modelQueriable;
        register = FlowManager.newRegisterForTable(modelQueriable.getTable());
    }

    @Override
    public void subscribe(FlowableEmitter<ModelQueriable<TModel>> e) throws Exception {
        flowableEmitter = e;
        flowableEmitter.setDisposable(Disposables.fromRunnable(new Runnable() {
            @Override
            public void run() {
                register.unregisterAll();
            }
        }));

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
                register.register(table);
            }
        } else {
            register.register(modelQueriable.getTable());
        }

        register.setListener(onTableChangedListener);
        flowableEmitter.onNext(modelQueriable);
    }

    private final OnTableChangedListener onTableChangedListener
        = new OnTableChangedListener() {
        @Override
        public void onTableChanged(@Nullable Class<?> tableChanged, @NonNull BaseModel.Action action) {
            if (modelQueriable.getTable().equals(tableChanged)) {
                flowableEmitter.onNext(modelQueriable);
            }
        }
    };

}
