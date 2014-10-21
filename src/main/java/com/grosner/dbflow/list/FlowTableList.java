package com.grosner.dbflow.list;

import android.annotation.TargetApi;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.transaction.BaseTransaction;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.runtime.transaction.process.ProcessModel;
import com.grosner.dbflow.runtime.transaction.process.ProcessModelHelper;
import com.grosner.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.language.Delete;
import com.grosner.dbflow.sql.language.Select;
import com.grosner.dbflow.structure.Model;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Author: andrewgrosner
 * Description: Operates very similiar to a {@link java.util.List} except its backed by a table cursor. All of
 * the {@link java.util.List} modifications default to the main thread, but it can be set to
 * run on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}. Register a {@link com.grosner.dbflow.runtime.transaction.ResultReceiver}
 * on this list to know when the results complete. NOTE: any modifications to this list will be reflected
 * on the underlying table.
 */
public class FlowTableList<ModelClass extends Model> extends ContentObserver implements List<ModelClass> {

    /**
     * We use high priority to assume that this list is used in some visual aspect.
     */
    private static DBTransactionInfo MODIFICATION_INFO = DBTransactionInfo.create(BaseTransaction.PRIORITY_HIGH);

    /**
     * Holds the table cursor
     */
    private FlowCursorList<ModelClass> mCursorList;

    private ResultReceiver<List<ModelClass>> mResultReceiver;
    private ResultReceiver<List<ModelClass>> mInternalResultReceiver = new ResultReceiver<List<ModelClass>>() {
        @Override
        public void onResultReceived(List<ModelClass> modelClasses) {
            mCursorList.refresh();

            if (mResultReceiver != null) {
                mResultReceiver.onResultReceived(modelClasses);
            }
        }
    };
    /**
     * If true, we will make all modifications on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}, else
     * we will run it on the main thread.
     */
    private boolean transact = false;

    public FlowTableList(Class<ModelClass> table) {
        super(null);
        mCursorList = new FlowCursorList<ModelClass>(true, table);
    }

    /**
     * Registers the list for model change events
     */
    public void registerForContentChanges() {
        FlowManager.getContext().getContentResolver().registerContentObserver(SqlUtils.getNotificationUri(mCursorList.getTable(), null), true, this);
    }

    /**
     * Unregisters this list for model change events
     */
    public void unregisterForContentChanges() {
        FlowManager.getContext().getContentResolver().unregisterContentObserver(this);
    }

    @Override
    public void onChange(boolean selfChange) {
        mCursorList.refresh();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        mCursorList.refresh();
    }

    /**
     * Register for callbacks when data is changed on this list.
     *
     * @param resultReceiver
     */
    public void setModificationReceiver(ResultReceiver<List<ModelClass>> resultReceiver) {
        mResultReceiver = resultReceiver;
    }

    /**
     * If true, we will transact all modifications on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     *
     * @param transact
     */
    public void setTransact(boolean transact) {
        this.transact = transact;
    }

    /**
     * @return a mutable list that does not reflect changes on the underlying DB.
     */
    public List<ModelClass> getCopy() {
        return mCursorList.getAll();
    }

    /**
     * Adds an item to this table, but does not allow positonal insertion. Same as calling
     * {@link #add(com.grosner.dbflow.structure.Model)}
     *
     * @param location Not used.
     * @param model    The model to save
     */
    @Override
    public void add(int location, ModelClass model) {
        add(model);
    }

    @SafeVarargs
    protected final ProcessModelInfo<ModelClass> getProcessModelInfo(ModelClass... modelClasses) {
        return ProcessModelInfo.withModels(modelClasses).result(mInternalResultReceiver).info(MODIFICATION_INFO);
    }

    /**
     * Adds an item to this table on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}.
     *
     * @param object
     * @return always true
     */
    @Override
    public boolean add(ModelClass object) {
        if (transact) {
            TransactionManager.getInstance().save(getProcessModelInfo(object));
        } else {
            object.save(false);
            mInternalResultReceiver.onResultReceived(Arrays.asList(object));
        }
        return true;
    }

    /**
     * Adds all items to this table on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}, but
     * does not allow positional insertion. Same as calling {@link #addAll(java.util.Collection)}
     *
     * @param location   Not used.
     * @param collection The list of items to add to the table
     * @return always true
     */
    @Override
    public boolean addAll(int location, Collection<? extends ModelClass> collection) {
        return addAll(collection);
    }

    protected final ProcessModelInfo<ModelClass> getProcessModelInfo(Collection<ModelClass> modelClasses) {
        return ProcessModelInfo.withModels(modelClasses).result(mInternalResultReceiver).info(MODIFICATION_INFO);
    }

    /**
     * Adds all items to this table on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}.
     *
     * @param collection The list of items to add to the table
     * @return always true
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(Collection<? extends ModelClass> collection) {
        // cast to normal collection, we do not want subclasses of this table saved
        final Collection<ModelClass> tmpCollection = (Collection<ModelClass>) collection;
        if (transact) {
            TransactionManager.getInstance().save(getProcessModelInfo(tmpCollection));
        } else {
            ProcessModelHelper.process(tmpCollection, new ProcessModel<ModelClass>() {
                @Override
                public void processModel(ModelClass model) {
                    model.save(false);
                }
            });
            mInternalResultReceiver.onResultReceived((List<ModelClass>) tmpCollection);
        }
        return true;
    }

    /**
     * Deletes all items from the table. Be careful as this will clear data!
     */
    @Override
    public void clear() {
        if (transact) {
            TransactionManager.getInstance().delete(MODIFICATION_INFO, mCursorList.getTable());
        } else {
            Delete.table(mCursorList.getTable());
        }
        mInternalResultReceiver.onResultReceived(null);
    }

    /**
     * Checks to see if the table contains the object only if its a {@link ModelClass}
     *
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object object) {
        boolean contains = false;
        if (mCursorList.getTable().isAssignableFrom(object.getClass())) {
            ModelClass model = ((ModelClass) object);
            contains = model.exists();
        }

        return contains;
    }

    /**
     * If the collection is null or empty, we return false.
     *
     * @param collection
     * @return true if all items exist in table
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
     * Returns the item from the backing {@link com.grosner.dbflow.list.FlowCursorList}. First call
     * will load the model from the cursor, while subsequent calls will use the cache.
     *
     * @param location
     * @return
     */
    @Override
    public ModelClass get(int location) {
        return mCursorList.getItem(location);
    }

    @Override
    public int indexOf(Object object) {
        throw new UnsupportedOperationException("We cannot determine which index in the table this item exists at efficiently");
    }

    @Override
    public boolean isEmpty() {
        return mCursorList.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<ModelClass> iterator() {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        throw new UnsupportedOperationException("We cannot determine which index in the table this item exists at efficiently");
    }

    @NonNull
    @Override
    public ListIterator<ModelClass> listIterator() {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<ModelClass> listIterator(int location) {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.listIterator(location);
    }

    /**
     * Removes the {@link ModelClass} from its table on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     *
     * @param location The location within the table to remove the item from
     * @return The removed item.
     */
    @Override
    public ModelClass remove(int location) {
        ModelClass model = mCursorList.getItem(location);
        if (transact) {
            TransactionManager.getInstance().delete(getProcessModelInfo(model));
        } else {
            model.delete(false);
            mInternalResultReceiver.onResultReceived(Arrays.asList(model));
        }
        return model;
    }

    /**
     * Removes an item from this table on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     *
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object object) {
        boolean removed = false;

        // if its a ModelClass
        if (mCursorList.getTable().isAssignableFrom(object.getClass())) {
            ModelClass model = ((ModelClass) object);
            if (transact) {
                TransactionManager.getInstance().delete(getProcessModelInfo(model));
            } else {
                model.delete(false);
                mInternalResultReceiver.onResultReceived(Arrays.asList(model));
            }
            removed = true;
        }

        return removed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        boolean removed = false;

        // if its a ModelClass
        if (mCursorList.getTable().isAssignableFrom((Class<?>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0])) {
            Collection<ModelClass> modelCollection = (Collection<ModelClass>) collection;
            if (transact) {
                TransactionManager.getInstance().delete(getProcessModelInfo(modelCollection));
            } else {
                ProcessModelHelper.process(modelCollection, new ProcessModel<ModelClass>() {
                    @Override
                    public void processModel(ModelClass model) {
                        model.delete(false);
                    }
                });
                mInternalResultReceiver.onResultReceived((List<ModelClass>) modelCollection);

            }
            removed = true;
        }

        return removed;
    }

    /**
     * Retrieves the full list of {@link ModelClass} items from the table, removes these from the list, and
     * then deletes the remaining members. This is not that efficient.
     *
     * @param collection
     * @return
     */
    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        List<ModelClass> tableList = mCursorList.getAll();
        tableList.removeAll(collection);
        if (transact) {
            TransactionManager.getInstance().delete(getProcessModelInfo(tableList));
        } else {
            ProcessModelHelper.process(tableList, new ProcessModel<ModelClass>() {
                @Override
                public void processModel(ModelClass model) {
                    model.delete(false);
                }
            });
            mInternalResultReceiver.onResultReceived(tableList);
        }
        return true;
    }

    /**
     * Will not use the index, rather just call a {@link com.grosner.dbflow.structure.Model#update(boolean)}
     *
     * @param location Not used.
     * @param object   The object to update
     * @return
     */
    @Override
    public ModelClass set(int location, ModelClass object) {
        return set(object);
    }

    /**
     * Updates a Model {@link com.grosner.dbflow.structure.Model#update(boolean)}
     *
     * @param object The object to update
     * @return
     */
    public ModelClass set(ModelClass object) {
        if (transact) {
            TransactionManager.getInstance().update(getProcessModelInfo(object));
        } else {
            object.update(false);
            mInternalResultReceiver.onResultReceived(Arrays.asList(object));
        }
        return object;
    }

    @Override
    public int size() {
        return mCursorList.getCount();
    }

    @NonNull
    @Override
    public List<ModelClass> subList(int start, int end) {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.subList(start, end);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] array) {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.toArray(array);
    }

    /**
     * Gets a {@link ModelClass} based on a list of {@link com.grosner.dbflow.sql.builder.Condition}
     *
     * @param conditions The list of conditions to retrieve a model from
     * @return
     */
    public ModelClass get(Condition... conditions) {
        return new Select().from(mCursorList.getTable()).where(conditions).querySingle();
    }

    /**
     * Fetches a list of all items in this table
     *
     * @param resultReceiver The callback that will receive the list.
     */
    public void fetchAll(ResultReceiver<List<ModelClass>> resultReceiver) {
        mCursorList.fetchAll(resultReceiver);
    }

    /**
     * Removes all {@link ModelClass} from the table based on the {@link com.grosner.dbflow.sql.builder.Condition}
     *
     * @param conditions The list of conditions to delete models with
     */
    public void removeAll(Condition... conditions) {
        if (transact) {
            TransactionManager.getInstance().delete(getProcessModelInfo(getAll(conditions)));
        } else {
            Delete.table(mCursorList.getTable(), conditions);
            mInternalResultReceiver.onResultReceived(null);
        }
    }

    /**
     * Returns a list of {@link ModelClass} based on the list of {@link com.grosner.dbflow.sql.builder.Condition}
     *
     * @param conditions The list of conditions to retrieve a model from
     * @return
     */
    public List<ModelClass> getAll(Condition... conditions) {
        return new Select().from(mCursorList.getTable()).where(conditions).queryList();
    }

}
