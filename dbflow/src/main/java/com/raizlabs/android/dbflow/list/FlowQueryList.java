package com.raizlabs.android.dbflow.list;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorList.OnCursorRefreshListener;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Description: Operates very similiar to a {@link java.util.List} except its backed by a table cursor. All of
 * the {@link java.util.List} modifications default to the main thread, but it can be set to
 * run on the {@link DefaultTransactionQueue}. Register a {@link Transaction.Success}
 * on this list to know when the results complete. NOTE: any modifications to this list will be reflected
 * on the underlying table.
 */
public class FlowQueryList<TModel> extends FlowContentObserver
        implements List<TModel>, IFlowCursorIterator<TModel> {

    private static final Handler REFRESH_HANDLER = new Handler(Looper.myLooper());

    /**
     * Holds the table cursor
     */
    private final FlowCursorList<TModel> internalCursorList;
    private final Transaction.Success successCallback;
    private final Transaction.Error errorCallback;

    /**
     * If true, we will make all modifications on the {@link DefaultTransactionQueue}, else
     * we will run it on the main thread.
     */
    private boolean transact = false;

    private boolean changeInTransaction = false;

    private boolean pendingRefresh = false;


    private FlowQueryList(Builder<TModel> builder) {
        super(StringUtils.isNotNullOrEmpty(builder.contentAuthority)
                ? builder.contentAuthority
                : FlowManager.DEFAULT_AUTHORITY);
        transact = builder.transact;
        changeInTransaction = builder.changeInTransaction;
        successCallback = builder.success;
        errorCallback = builder.error;
        internalCursorList = new FlowCursorList.Builder<>(builder.table)
                .cursor(builder.cursor)
                .cacheModels(builder.cacheModels)
                .modelQueriable(builder.modelQueriable)
                .modelCache(builder.modelCache)
                .build();
    }

    /**
     * Registers the list for model change events. Internally this refreshes the underlying {@link FlowCursorList}. Call
     * {@link #beginTransaction()} to bunch up calls to model changes and then {@link #endTransactionAndNotify()} to dispatch
     * and refresh this list when completed.
     */
    public void registerForContentChanges(@NonNull Context context) {
        super.registerForContentChanges(context, internalCursorList.table());
    }

    public void addOnCursorRefreshListener(@NonNull OnCursorRefreshListener<TModel> onCursorRefreshListener) {
        internalCursorList.addOnCursorRefreshListener(onCursorRefreshListener);
    }

    public void removeOnCursorRefreshListener(@NonNull OnCursorRefreshListener<TModel> onCursorRefreshListener) {
        internalCursorList.removeOnCursorRefreshListener(onCursorRefreshListener);
    }

    @Override
    public void registerForContentChanges(Context context, Class<?> table) {
        throw new RuntimeException(
                "This method is not to be used in the FlowQueryList. We should only ever receive" +
                        " notifications for one class here. Call registerForContentChanges(Context) instead");
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (!isInTransaction) {
            refreshAsync();
        } else {
            changeInTransaction = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if (!isInTransaction) {
            refreshAsync();
        } else {
            changeInTransaction = true;
        }
    }

    /**
     * @return a mutable list that does not reflect changes on the underlying DB.
     */
    @NonNull
    public List<TModel> getCopy() {
        return internalCursorList.getAll();
    }

    @NonNull
    public FlowCursorList<TModel> cursorList() {
        return internalCursorList;
    }

    @Nullable
    public Transaction.Error error() {
        return errorCallback;
    }

    @Nullable
    public Transaction.Success success() {
        return successCallback;
    }

    public boolean changeInTransaction() {
        return changeInTransaction;
    }

    public boolean transact() {
        return transact;
    }

    @NonNull
    ModelAdapter<TModel> getModelAdapter() {
        return internalCursorList.getModelAdapter();
    }

    @NonNull
    InstanceAdapter<TModel> getInstanceAdapter() {
        return internalCursorList.getInstanceAdapter();
    }

    /**
     * @return Constructs a new {@link Builder} that reuses the underlying {@link Cursor}, cache,
     * callbacks, and other properties.
     */
    @NonNull
    public Builder<TModel> newBuilder() {
        return new Builder<>(internalCursorList)
                .success(successCallback)
                .error(errorCallback)
                .changeInTransaction(changeInTransaction)
                .transact(transact);
    }

    /**
     * Refreshes the content backing this list.
     */
    public void refresh() {
        internalCursorList.refresh();
    }

    /**
     * Will refresh content at a slightly later time, and multiple subsequent calls to this method within
     * a short period of time will be combined into one call.
     */
    public void refreshAsync() {
        synchronized (this) {
            if (pendingRefresh) {
                return;
            }
            pendingRefresh = true;
        }
        REFRESH_HANDLER.post(refreshRunnable);
    }

    @Override
    public void endTransactionAndNotify() {
        if (changeInTransaction) {
            changeInTransaction = false;
            refresh();
        }
        super.endTransactionAndNotify();
    }

    /**
     * Adds an item to this table, but does not allow positonal insertion. Same as calling
     * {@link #add(TModel)}
     *
     * @param location Not used.
     * @param model    The model to save
     */
    @Override
    public void add(int location, @Nullable TModel model) {
        add(model);
    }

    /**
     * Adds an item to this table
     *
     * @param model The model to save
     * @return always true
     */
    @Override
    public boolean add(@Nullable TModel model) {
        if (model != null) {
            Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.table())
                    .beginTransactionAsync(new ProcessModelTransaction.Builder<>(saveModel)
                            .add(model).build())
                    .error(internalErrorCallback)
                    .success(internalSuccessCallback).build();

            if (transact) {
                transaction.execute();
            } else {
                transaction.executeSync();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds all items to this table, but
     * does not allow positional insertion. Same as calling {@link #addAll(java.util.Collection)}
     *
     * @param location   Not used.
     * @param collection The list of items to add to the table
     * @return always true
     */
    @Override
    public boolean addAll(int location, @NonNull Collection<? extends TModel> collection) {
        return addAll(collection);
    }

    /**
     * Adds all items to this table.
     *
     * @param collection The list of items to add to the table
     * @return always true
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(@NonNull Collection<? extends TModel> collection) {
        // cast to normal collection, we do not want subclasses of this table saved
        final Collection<TModel> tmpCollection = (Collection<TModel>) collection;

        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.table())
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(saveModel)
                        .addAll(tmpCollection).build())
                .error(internalErrorCallback)
                .success(internalSuccessCallback).build();

        if (transact) {
            transaction.execute();
        } else {
            transaction.executeSync();
        }
        return true;
    }

    /**
     * Deletes all items from the table. Be careful as this will clear data!
     */
    @Override
    public void clear() {
        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.table())
                .beginTransactionAsync(new QueryTransaction.Builder<>(
                        SQLite.delete().from(internalCursorList.table())).build())
                .error(internalErrorCallback)
                .success(internalSuccessCallback)
                .build();

        if (transact) {
            transaction.execute();
        } else {
            transaction.executeSync();
        }
    }

    /**
     * Checks to see if the table contains the object only if its a {@link TModel}
     *
     * @param object A model class. For interface purposes, this must be an Object.
     * @return always false if its anything other than the current table. True if {@link com.raizlabs.android.dbflow.structure.Model#exists()} passes.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(@Nullable Object object) {
        boolean contains = false;
        if (object != null && internalCursorList.table().isAssignableFrom(object.getClass())) {
            TModel model = ((TModel) object);
            contains = internalCursorList.getInstanceAdapter().exists(model);
        }

        return contains;
    }

    /**
     * If the collection is null or empty, we return false.
     *
     * @param collection The collection to check if all exist within the table.
     * @return true if all items exist in table, false if at least one fails.
     */
    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        boolean contains = !(collection.isEmpty());
        if (contains) {
            for (Object o : collection) {
                if (!contains(o)) {
                    contains = false;
                    break;
                }
            }
        }
        return contains;
    }

    @Override
    public long getCount() {
        return internalCursorList.getCount();
    }

    @Nullable
    @Override
    public TModel getItem(long position) {
        return internalCursorList.getItem(position);
    }

    @Nullable
    @Override
    public Cursor cursor() {
        return internalCursorList.cursor();
    }

    /**
     * Returns the item from the backing {@link FlowCursorList}. First call
     * will load the model from the cursor, while subsequent calls will use the cache.
     *
     * @param row the row from the internal {@link FlowCursorList} query that we use.
     * @return A model converted from the internal {@link FlowCursorList}. For
     * performance improvements, ensure caching is turned on.
     */
    @Nullable
    @Override
    public TModel get(int row) {
        return internalCursorList.getItem(row);
    }

    @Override
    public int indexOf(Object object) {
        throw new UnsupportedOperationException(
                "We cannot determine which index in the table this item exists at efficiently");
    }

    @Override
    public boolean isEmpty() {
        return internalCursorList.isEmpty();
    }

    /**
     * @return An iterator from {@link FlowCursorList#getAll()}.
     * Be careful as this method will convert all data under this table into a list of {@link TModel} in the UI thread.
     */
    @NonNull
    @Override
    public FlowCursorIterator<TModel> iterator() {
        return new FlowCursorIterator<>(this);
    }

    @NonNull
    @Override
    public FlowCursorIterator<TModel> iterator(int startingLocation, long limit) {
        return new FlowCursorIterator<>(this, startingLocation, limit);
    }

    @Override
    public int lastIndexOf(Object object) {
        throw new UnsupportedOperationException(
                "We cannot determine which index in the table this item exists at efficiently");
    }

    /**
     * @return A list iterator from the {@link FlowCursorList#getAll()}.
     * Be careful as this method will convert all data under this table into a list of {@link TModel} in the UI thread.
     */
    @NonNull
    @Override
    public ListIterator<TModel> listIterator() {
        return new FlowCursorIterator<>(this);
    }

    /**
     * @param location The index to start the iterator.
     * @return A list iterator from the {@link FlowCursorList#getAll()}.
     * Be careful as this method will convert all data under this table into a list of {@link TModel} in the UI thread.
     */
    @NonNull
    @Override
    public ListIterator<TModel> listIterator(int location) {
        return new FlowCursorIterator<>(this, location);
    }

    /**
     * Deletes a {@link TModel} at a specific position within the stored {@link Cursor}.
     * If {@link #transact} is true, the delete does not happen immediately. Avoid using this operation
     * many times. If you need to remove multiple, use {@link #removeAll(Collection)}
     *
     * @param location The location within the table to remove the item from
     * @return The removed item.
     */
    @Override
    public TModel remove(int location) {
        TModel model = internalCursorList.getItem(location);

        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.table())
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(deleteModel)
                        .add(model).build())
                .error(internalErrorCallback)
                .success(internalSuccessCallback).build();

        if (transact) {
            transaction.execute();
        } else {
            transaction.executeSync();
        }
        return model;
    }

    /**
     * Removes an item from this table on the {@link DefaultTransactionQueue} if
     * {@link #transact} is true.
     *
     * @param object A model class. For interface purposes, this must be an Object.
     * @return true if the item was removed. Always false if the object is not from the same table as this list.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object object) {
        boolean removed = false;

        // if its a ModelClass
        if (internalCursorList.table().isAssignableFrom(object.getClass())) {
            TModel model = ((TModel) object);
            Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.table())
                    .beginTransactionAsync(new ProcessModelTransaction.Builder<>(deleteModel)
                            .add(model).build())
                    .error(internalErrorCallback)
                    .success(internalSuccessCallback).build();

            if (transact) {
                transaction.execute();
            } else {
                transaction.executeSync();
            }
            removed = true;
        }

        return removed;
    }

    /**
     * Removes all items from this table in one transaction based on the list passed. This may happen in the background
     * if {@link #transact} is true.
     *
     * @param collection The collection to remove.
     * @return Always true. Will cause a {@link ClassCastException} if the collection is not of type {@link TModel}
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {

        // if its a ModelClass
        Collection<TModel> modelCollection = (Collection<TModel>) collection;
        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.table())
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(deleteModel)
                        .addAll(modelCollection).build())
                .error(internalErrorCallback)
                .success(internalSuccessCallback).build();

        if (transact) {
            transaction.execute();
        } else {
            transaction.executeSync();
        }

        return true;
    }

    /**
     * Retrieves the full list of {@link TModel} items from the table, removes these from the list, and
     * then deletes the remaining members. This is not that efficient.
     *
     * @param collection The collection if models to keep in the table.
     * @return Always true.
     */
    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        List<TModel> tableList = internalCursorList.getAll();
        tableList.removeAll(collection);
        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.table())
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(tableList, deleteModel)
                        .build())
                .error(internalErrorCallback)
                .success(internalSuccessCallback).build();

        if (transact) {
            transaction.execute();
        } else {
            transaction.executeSync();
        }
        return true;
    }

    /**
     * Will not use the index, rather just call a {@link Model#update()}
     *
     * @param location Not used.
     * @param object   The object to update
     * @return the updated model.
     */
    @Override
    public TModel set(int location, TModel object) {
        return set(object);
    }

    /**
     * Updates a Model {@link Model#update()} . If {@link #transact}
     * is true, this update happens in the BG, otherwise it happens immediately.
     *
     * @param object The object to update
     * @return The updated model.
     */
    public TModel set(TModel object) {
        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.table())
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(updateModel)
                        .add(object)
                        .build())
                .error(internalErrorCallback)
                .success(internalSuccessCallback).build();

        if (transact) {
            transaction.execute();
        } else {
            transaction.executeSync();
        }
        return object;
    }

    @Override
    public int size() {
        return (int) internalCursorList.getCount();
    }

    @NonNull
    @Override
    public List<TModel> subList(int start, int end) {
        List<TModel> tableList = internalCursorList.getAll();
        return tableList.subList(start, end);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        List<TModel> tableList = internalCursorList.getAll();
        return tableList.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] array) {
        List<TModel> tableList = internalCursorList.getAll();
        return tableList.toArray(array);
    }

    @Override
    public void close() {
        internalCursorList.close();
    }

    private final ProcessModelTransaction.ProcessModel<TModel> saveModel =
            new ProcessModelTransaction.ProcessModel<TModel>() {
                @Override
                public void processModel(TModel model, DatabaseWrapper wrapper) {
                    getModelAdapter().save(model);
                }
            };

    private final ProcessModelTransaction.ProcessModel<TModel> updateModel =
            new ProcessModelTransaction.ProcessModel<TModel>() {
                @Override
                public void processModel(TModel model, DatabaseWrapper wrapper) {
                    getModelAdapter().update(model);
                }
            };

    private final ProcessModelTransaction.ProcessModel<TModel> deleteModel =
            new ProcessModelTransaction.ProcessModel<TModel>() {
                @Override
                public void processModel(TModel model, DatabaseWrapper wrapper) {
                    getModelAdapter().delete(model);
                }
            };

    private final Transaction.Error internalErrorCallback = new Transaction.Error() {
        @Override
        public void onError(@NonNull Transaction transaction, @NonNull Throwable error) {

            if (errorCallback != null) {
                errorCallback.onError(transaction, error);
            }
        }
    };

    private final Transaction.Success internalSuccessCallback = new Transaction.Success() {
        @Override
        public void onSuccess(@NonNull Transaction transaction) {
            if (!isInTransaction) {
                refreshAsync();
            } else {
                changeInTransaction = true;
            }

            if (successCallback != null) {
                successCallback.onSuccess(transaction);
            }
        }
    };

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                pendingRefresh = false;
            }
            refresh();
        }
    };

    public static class Builder<TModel> {

        private final Class<TModel> table;

        private boolean transact;
        private boolean changeInTransaction;
        private Cursor cursor;
        private boolean cacheModels = true;
        private ModelQueriable<TModel> modelQueriable;
        private ModelCache<TModel, ?> modelCache;

        private Transaction.Success success;
        private Transaction.Error error;

        private String contentAuthority;

        private Builder(FlowCursorList<TModel> cursorList) {
            table = cursorList.table();
            cursor = cursorList.cursor();
            cacheModels = cursorList.cachingEnabled();
            modelQueriable = cursorList.modelQueriable();
            modelCache = cursorList.modelCache();
        }

        public Builder(Class<TModel> table) {
            this.table = table;
        }

        public Builder(@NonNull ModelQueriable<TModel> modelQueriable) {
            this(modelQueriable.getTable());
            modelQueriable(modelQueriable);
        }

        public Builder<TModel> cursor(Cursor cursor) {
            this.cursor = cursor;
            return this;
        }

        public Builder<TModel> modelQueriable(ModelQueriable<TModel> modelQueriable) {
            this.modelQueriable = modelQueriable;
            return this;
        }

        public Builder<TModel> transact(boolean transact) {
            this.transact = transact;
            return this;
        }

        public Builder<TModel> modelCache(ModelCache<TModel, ?> modelCache) {
            this.modelCache = modelCache;
            return this;
        }

        public Builder<TModel> contentAuthority(String contentAuthority) {
            this.contentAuthority = contentAuthority;
            return this;
        }

        /**
         * If true, when an operation occurs when we call endTransactionAndNotify, we refresh content.
         */
        public Builder<TModel> changeInTransaction(boolean changeInTransaction) {
            this.changeInTransaction = changeInTransaction;
            return this;
        }

        public Builder<TModel> cacheModels(boolean cacheModels) {
            this.cacheModels = cacheModels;
            return this;
        }

        public Builder<TModel> success(Transaction.Success success) {
            this.success = success;
            return this;
        }

        public Builder<TModel> error(Transaction.Error error) {
            this.error = error;
            return this;
        }

        public FlowQueryList<TModel> build() {
            return new FlowQueryList<>(this);
        }
    }


}
