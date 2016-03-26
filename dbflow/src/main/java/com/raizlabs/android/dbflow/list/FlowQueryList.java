package com.raizlabs.android.dbflow.list;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.cache.ModelLruCache;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Description: Operates very similiar to a {@link java.util.List} except its backed by a table cursor. All of
 * the {@link java.util.List} modifications default to the main thread, but it can be set to
 * run on the {@link DefaultTransactionQueue}. Register a {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener}
 * on this list to know when the results complete. NOTE: any modifications to this list will be reflected
 * on the underlying table.
 */
public class FlowQueryList<TModel extends Model> extends FlowContentObserver implements List<TModel> {

    /**
     * Holds the table cursor
     */
    private FlowCursorList<TModel> internalCursorList;

    private Transaction.Success successCallback;
    private Transaction.Error errorCallback;

    /**
     * If true, we will make all modifications on the {@link DefaultTransactionQueue}, else
     * we will run it on the main thread.
     */
    private boolean transact = false;

    /**
     * Constructs an instance of this list with the specified conditions.
     *
     * @param table      The table to load into this list.
     * @param conditions The set of conditions to use when querying the DB.
     */
    public FlowQueryList(Class<TModel> table, SQLCondition... conditions) {
        super(null);
        internalCursorList = new FlowCursorList<TModel>(true, table, conditions) {
            @Override
            protected ModelCache<TModel, ?> getBackingCache() {
                return FlowQueryList.this.getBackingCache(getCacheSize());
            }
        };
    }

    /**
     * Constructs an instance of this list with the specfied {@link ModelQueriable} object.
     *
     * @param modelQueriable The object that can query from a database.
     */
    public FlowQueryList(ModelQueriable<TModel> modelQueriable) {
        super(null);
        internalCursorList = new FlowCursorList<TModel>(transact, modelQueriable) {
            @Override
            protected ModelCache<TModel, ?> getBackingCache() {
                return FlowQueryList.this.getBackingCache(getCacheSize());
            }
        };
    }

    /**
     * @param count The size of the underlying {@link FlowCursorList}
     * @return The cache backing this query. Override to provide a custom {@link com.raizlabs.android.dbflow.structure.cache.ModelCache}
     * instead. If the count is somehow 0, it will default to a size of 50.
     * If you override this method, be careful to call an empty cache to the {@link com.raizlabs.android.dbflow.structure.cache.ModelLruCache}
     */
    public ModelCache<TModel, ?> getBackingCache(int count) {
        return ModelLruCache.newInstance(count);
    }

    /**
     * Called when the count for the underlying cache is needed.
     *
     * @return 50 as default. Override for different. Note: some {@link ModelCache} do not respect the size of the cache.
     */
    public int getCacheSize() {
        return 50;
    }

    /**
     * Registers the list for model change events. Internally this refreshes the underlying {@link FlowCursorList}. Call
     * {@link #beginTransaction()} to bunch up calls to model changes and then {@link #endTransactionAndNotify()} to dispatch
     * and refresh this list when completed.
     */
    public void registerForContentChanges(Context context) {
        super.registerForContentChanges(context, internalCursorList.getTable());
    }

    @Override
    public void registerForContentChanges(Context context, Class<? extends Model> table) {
        throw new RuntimeException(
                "This method is not to be used in the FlowQueryList. call registerForContentChanges(Context) instead");
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (!isInTransaction) {
            internalCursorList.refresh();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if (!isInTransaction) {
            internalCursorList.refresh();
        }
    }

    /**
     * Register for callbacks when data is successfully processed.
     *
     * @param successCallback The callback.
     */
    public void setSuccessCallback(Transaction.Success successCallback) {
        this.successCallback = successCallback;
    }

    /**
     * Register for callbacks when there is a problem with data processed here.
     *
     * @param errorCallback The callback that receives errors.
     */
    public void setErrorCallback(Transaction.Error errorCallback) {
        this.errorCallback = errorCallback;
    }

    /**
     * If true, we will transact all modifications on the {@link ITransactionQueue}
     *
     * @param transact true to transact all modifications in the background.
     */
    public void setTransact(boolean transact) {
        this.transact = transact;
    }

    /**
     * @return a mutable list that does not reflect changes on the underlying DB.
     */
    public List<TModel> getCopy() {
        return internalCursorList.getAll();
    }

    /**
     * @return The {@link FlowCursorList} that backs this table list.
     */
    public FlowCursorList<TModel> getCursorList() {
        return internalCursorList;
    }

    /**
     * Refreshes the content backing this list.
     */
    public void refresh() {
        internalCursorList.refresh();
    }

    /**
     * Registers itself for content changes on the specific table that this list is for. When
     * any model data is changed via the {@link Model} methods, we call {@link #refresh()} on this underlying data.
     * To prevent many refreshes, call {@link #beginTransaction()} before making changes to a set of models,
     * and then when finished call {@link #endTransactionAndNotify()}.
     */
    public void enableSelfRefreshes(Context context) {
        registerForContentChanges(context);
        addModelChangeListener(new OnModelStateChangedListener() {
            @Override
            public void onModelStateChanged(@Nullable Class<? extends Model> table, BaseModel.Action action, @NonNull SQLCondition[] primaryKeyValues) {
                if (internalCursorList.getTable().equals(table)) {
                    refresh();
                }
            }
        });
    }


    /**
     * Adds an item to this table, but does not allow positonal insertion. Same as calling
     * {@link #add(com.raizlabs.android.dbflow.structure.Model)}
     *
     * @param location Not used.
     * @param model    The model to save
     */
    @Override
    public void add(int location, TModel model) {
        add(model);
    }

    /**
     * Adds an item to this table
     *
     * @param model The model to save
     * @return always true
     */
    @Override
    public boolean add(TModel model) {
        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.getTable())
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(saveModel)
                        .add(model).build())
                .error(errorCallback)
                .success(internalSuccessCallback).build();

        if (transact) {
            transaction.execute();
        } else {
            transaction.executeSync();
        }
        return true;
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
    public boolean addAll(int location, Collection<? extends TModel> collection) {
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
    public boolean addAll(Collection<? extends TModel> collection) {
        // cast to normal collection, we do not want subclasses of this table saved
        final Collection<TModel> tmpCollection = (Collection<TModel>) collection;

        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.getTable())
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(saveModel)
                        .addAll(tmpCollection).build())
                .error(errorCallback)
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
        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.getTable())
                .beginTransactionAsync(new QueryTransaction.Builder<>(
                        SQLite.delete().from(internalCursorList.getTable())).build())
                .error(errorCallback)
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
    public boolean contains(Object object) {
        boolean contains = false;
        if (internalCursorList.getTable().isAssignableFrom(object.getClass())) {
            TModel model = ((TModel) object);
            contains = model.exists();
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

    /**
     * Returns the item from the backing {@link FlowCursorList}. First call
     * will load the model from the cursor, while subsequent calls will use the cache.
     *
     * @param row the row from the internal {@link FlowCursorList} query that we use.
     * @return A model converted from the internal {@link FlowCursorList}. For
     * performance improvements, ensure caching is turned on.
     */
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
    public Iterator<TModel> iterator() {
        List<TModel> tableList = internalCursorList.getAll();
        return tableList.iterator();
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
        List<TModel> tableList = internalCursorList.getAll();
        return tableList.listIterator();
    }

    /**
     * @param location The index to start the iterator.
     * @return A list iterator from the {@link FlowCursorList#getAll()}.
     * Be careful as this method will convert all data under this table into a list of {@link TModel} in the UI thread.
     */
    @NonNull
    @Override
    public ListIterator<TModel> listIterator(int location) {
        List<TModel> tableList = internalCursorList.getAll();
        return tableList.listIterator(location);
    }

    /**
     * Removes the {@link TModel} from its table on the {@link DefaultTransactionQueue} .
     * If {@link #transact} is true, the delete does not happen immediately.
     *
     * @param location The location within the table to remove the item from
     * @return The removed item.
     */
    @Override
    public TModel remove(int location) {
        TModel model = internalCursorList.getItem(location);

        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.getTable())
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(deleteModel)
                        .add(model).build())
                .error(errorCallback)
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
        if (internalCursorList.getTable().isAssignableFrom(object.getClass())) {
            TModel model = ((TModel) object);
            Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.getTable())
                    .beginTransactionAsync(new ProcessModelTransaction.Builder<>(deleteModel)
                            .add(model).build())
                    .error(errorCallback)
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
        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.getTable())
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(deleteModel)
                        .addAll(modelCollection).build())
                .error(errorCallback)
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
        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.getTable())
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(tableList, deleteModel)
                        .build())
                .error(errorCallback)
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
        Transaction transaction = FlowManager.getDatabaseForTable(internalCursorList.getTable())
                .beginTransactionAsync(new ProcessModelTransaction.Builder<>(updateModel)
                        .add(object)
                        .build())
                .error(errorCallback)
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
        return internalCursorList.getCount();
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

    private final ProcessModelTransaction.ProcessModel<TModel> saveModel =
            new ProcessModelTransaction.ProcessModel<TModel>() {
                @Override
                public void processModel(TModel model) {
                    model.save();
                }
            };

    private final ProcessModelTransaction.ProcessModel<TModel> insertModel =
            new ProcessModelTransaction.ProcessModel<TModel>() {
                @Override
                public void processModel(TModel model) {
                    model.insert();
                }
            };

    private final ProcessModelTransaction.ProcessModel<TModel> updateModel =
            new ProcessModelTransaction.ProcessModel<TModel>() {
                @Override
                public void processModel(TModel model) {
                    model.update();
                }
            };

    private final ProcessModelTransaction.ProcessModel<TModel> deleteModel =
            new ProcessModelTransaction.ProcessModel<TModel>() {
                @Override
                public void processModel(TModel model) {
                    model.delete();
                }
            };

    private final Transaction.Error internalErrorCallback = new Transaction.Error() {
        @Override
        public void onError(Transaction transaction, Throwable error) {

            if (errorCallback != null) {
                errorCallback.onError(transaction, error);
            }
        }
    };

    private final Transaction.Success internalSuccessCallback = new Transaction.Success() {
        @Override
        public void onSuccess(Transaction transaction) {
            refresh();

            if (successCallback != null) {
                successCallback.onSuccess(transaction);
            }
        }
    };


}
