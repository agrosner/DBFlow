package com.raizlabs.android.dbflow.rx.language;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Emitter;
import rx.Observable;
import rx.Single;

import static rx.Single.fromCallable;

/**
 * Description: Represents {@link BaseModelQueriable} in RX form.
 */
public class RXModelQueriableImpl<T> extends RXQueriableImpl implements RXModelQueriable<T> {

    private final ModelQueriable<T> modelQueriable;

    RXModelQueriableImpl(ModelQueriable<T> modelQueriable) {
        super(modelQueriable.getTable(), modelQueriable);
        this.modelQueriable = modelQueriable;
    }

    private ModelQueriable<T> getInnerModelQueriable() {
        return modelQueriable;
    }

    @NonNull
    @Override
    public Observable<T> queryStreamResults() {
        return Observable.create(new CursorResultSubscriber<>(this));
    }

    @NonNull
    @Override
    public Single<CursorResult<T>> queryResults() {
        return fromCallable(new Callable<CursorResult<T>>() {
            @Override
            public CursorResult<T> call() throws Exception {
                return getInnerModelQueriable().queryResults();
            }
        });
    }

    @NonNull
    @Override
    public Single<List<T>> queryList() {
        return fromCallable(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                return getInnerModelQueriable().queryList();
            }
        });
    }

    @NonNull
    @Override
    public Single<List<T>> queryList(final DatabaseWrapper wrapper) {
        return fromCallable(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                return getInnerModelQueriable().queryList(wrapper);
            }
        });
    }

    @NonNull
    @Override
    public Single<T> querySingle() {
        return fromCallable(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return getInnerModelQueriable().querySingle();
            }
        });
    }

    @NonNull
    @Override
    public Single<T> querySingle(final DatabaseWrapper wrapper) {
        return fromCallable(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return getInnerModelQueriable().querySingle(wrapper);
            }
        });
    }

    @NonNull
    @Override
    public Class<T> getTable() {
        return getInnerModelQueriable().getTable();
    }

    @NonNull
    @Override
    public Single<FlowCursorList<T>> cursorList() {
        return fromCallable(new Callable<FlowCursorList<T>>() {
            @Override
            public FlowCursorList<T> call() throws Exception {
                return getInnerModelQueriable().cursorList();
            }
        });
    }

    @NonNull
    @Override
    public Single<FlowQueryList<T>> flowQueryList() {
        return fromCallable(new Callable<FlowQueryList<T>>() {
            @Override
            public FlowQueryList<T> call() throws Exception {
                return getInnerModelQueriable().flowQueryList();
            }
        });
    }

    @NonNull
    @Override
    public <TQueryModel> Single<List<TQueryModel>> queryCustomList(
        final Class<TQueryModel> tQueryModelClass) {
        return fromCallable(new Callable<List<TQueryModel>>() {
            @Override
            public List<TQueryModel> call() throws Exception {
                return getInnerModelQueriable().queryCustomList(tQueryModelClass);
            }
        });
    }

    @NonNull
    @Override
    public <TQueryModel> Single<TQueryModel> queryCustomSingle(
        final Class<TQueryModel> tQueryModelClass) {
        return fromCallable(new Callable<TQueryModel>() {
            @Override
            public TQueryModel call() throws Exception {
                return getInnerModelQueriable().queryCustomSingle(tQueryModelClass);
            }
        });
    }

    @NonNull
    @Override
    public RXModelQueriable<T> disableCaching() {
        getInnerModelQueriable().disableCaching();
        return this;
    }

    @NonNull
    @Override
    public Observable<ModelQueriable<T>> observeOnTableChanges() {
        return Observable.create(new TableChangeListenerEmitter<>(getInnerModelQueriable()),
            Emitter.BackpressureMode.LATEST);
    }
}
