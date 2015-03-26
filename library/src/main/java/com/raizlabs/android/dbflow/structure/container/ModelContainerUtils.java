package com.raizlabs.android.dbflow.structure.container;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.raizlabs.android.dbflow.SQiteCompatibilityUtils;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.InsertModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * @see {@link com.raizlabs.android.dbflow.sql.SqlUtils}
 * @deprecated Now we consolidated methods to elminate duplication. These methods will no longer be updated.
 */
@Deprecated
public class ModelContainerUtils {

    /**
     * Syncs a {@link com.raizlabs.android.dbflow.structure.container.BaseModelContainer} to the database.
     *
     * @param modelContainer The container model to save
     * @param async          Where it goes on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} or done immediately/
     * @param mode           The save mode, can be {@link com.raizlabs.android.dbflow.sql.SqlUtils#SAVE_MODE_DEFAULT},
     *                       {@link com.raizlabs.android.dbflow.sql.SqlUtils#SAVE_MODE_INSERT}, {@link com.raizlabs.android.dbflow.sql.SqlUtils#SAVE_MODE_UPDATE}
     * @param <ModelClass>   The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <ModelClass extends Model> void sync(boolean async, ModelContainer<ModelClass, ?> modelContainer, ContainerAdapter<ModelClass> containerAdapter, @SqlUtils.SaveMode int mode) {
        if (!async) {

            if (modelContainer == null) {
                throw new IllegalArgumentException("Model from " + containerAdapter.getModelClass() + " was null");
            }

            boolean exists = false;
            BaseModel.Action action = BaseModel.Action.SAVE;
            if (mode == SqlUtils.SAVE_MODE_DEFAULT) {
                exists = containerAdapter.exists(modelContainer);
            } else if (mode == SqlUtils.SAVE_MODE_UPDATE) {
                exists = true;
                action = BaseModel.Action.UPDATE;
            } else {
                action = BaseModel.Action.INSERT;
            }

            if (exists) {
                exists = update(false, modelContainer, containerAdapter);
            }

            if (!exists) {
                insert(false, modelContainer, containerAdapter);
            }

            if (FlowContentObserver.shouldNotify()) {
                SqlUtils.notifyModelChanged(modelContainer.getTable(), action);
            }

        } else {
            TransactionManager.getInstance().save(ProcessModelInfo.withModels(modelContainer));
        }
    }


    /**
     * Deletes {@link com.raizlabs.android.dbflow.structure.Model} from the database using the specfied {@link com.raizlabs.android.dbflow.config.FlowManager}
     *
     * @param modelContainer The jsonModel that corresponds to an item in the DB we want to delete
     * @param async          Whether it goes on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} or done immediately.
     * @param <ModelClass>   The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <ModelClass extends Model> void delete(final ModelContainer<ModelClass, ?> modelContainer,
                                                         ContainerAdapter<ModelClass> containerAdapter, boolean async) {
        if (!async) {
            new Delete().from(modelContainer.getTable()).where(containerAdapter.getPrimaryModelWhere(modelContainer)).query();
            containerAdapter.updateAutoIncrement(modelContainer, 0);
            if (FlowContentObserver.shouldNotify()) {
                SqlUtils.notifyModelChanged(modelContainer.getTable(), BaseModel.Action.DELETE);
            }
        } else {
            TransactionManager.getInstance().delete(ProcessModelInfo.withModels(modelContainer));
        }
    }

    /**
     * Will attempt to insert the {@link com.raizlabs.android.dbflow.structure.container.ModelContainer} into the DB.
     *
     * @param async          Where it goes on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} or done immediately.
     * @param modelContainer The model container to insert.
     * @param modelAdapter   The container adapter to use.
     * @param <ModelClass>   The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     */
    @Deprecated
    public static <ModelClass extends Model> void insert(boolean async, ModelContainer<ModelClass, ?> modelContainer, ContainerAdapter<ModelClass> modelAdapter) {
        if (!async) {
            ModelAdapter<ModelClass> modelClassModelAdapter = FlowManager.getModelAdapter(modelContainer.getTable());
            SQLiteStatement insertStatement = modelClassModelAdapter.getInsertStatement();
            modelAdapter.bindToStatement(insertStatement, modelContainer);
            long id = insertStatement.executeInsert();
            modelAdapter.updateAutoIncrement(modelContainer, id);
            if (FlowContentObserver.shouldNotify()) {
                SqlUtils.notifyModelChanged(modelAdapter.getModelClass(), BaseModel.Action.INSERT);
            }
        } else {
            TransactionManager.getInstance().addTransaction(new InsertModelTransaction<>(ProcessModelInfo.withModels(modelContainer)
                    .info(DBTransactionInfo.createSave())));
        }
    }

    /**
     * Updates the model container if it exists. If the model does not exist and no rows are changed, we will attempt an insert into the DB.
     *
     * @param async                      Where it goes on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} or done immediately.
     * @param modelContainer             The model container to update
     * @param modelClassContainerAdapter The adapter to use
     * @param <ModelClass>               The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return true if model was inserted, false if not. Also false could mean that it is placed on the
     * {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} using async to true.
     */
    @Deprecated
    public static <ModelClass extends Model> boolean update(boolean async, ModelContainer<ModelClass, ?> modelContainer, ContainerAdapter<ModelClass> modelClassContainerAdapter) {
        boolean exists = false;
        if (!async) {
            ModelAdapter<ModelClass> modelClassModelAdapter = FlowManager.getModelAdapter(modelContainer.getTable());
            SQLiteDatabase db = FlowManager.getDatabaseForTable(modelClassModelAdapter.getModelClass()).getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            modelClassContainerAdapter.bindToContentValues(contentValues, modelContainer);
            exists = (SQiteCompatibilityUtils.updateWithOnConflict(db, modelClassModelAdapter.getTableName(),
                    contentValues, modelClassContainerAdapter.getPrimaryModelWhere(modelContainer).getQuery(), null,
                    ConflictAction.getSQLiteDatabaseAlgorithmInt(modelClassModelAdapter.getUpdateOnConflictAction())) != 0);
            if (!exists) {
                // insert
                insert(false, modelContainer, modelClassContainerAdapter);
            } else if (FlowContentObserver.shouldNotify()) {
                SqlUtils.notifyModelChanged(modelClassModelAdapter.getModelClass(), BaseModel.Action.UPDATE);
            }
        } else {
            TransactionManager.getInstance().update(ProcessModelInfo.withModels(modelContainer).info(DBTransactionInfo.createSave()));
        }
        return exists;

    }
}
