package com.raizlabs.android.dbflow.structure.container;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * Author: andrewgrosner
 * Description: Provides helper methods for handling {@link com.raizlabs.android.dbflow.structure.container.ModelContainer} classes.
 * These wrap around {@link com.raizlabs.android.dbflow.structure.Model} to provide more convenient means of interacting with the db.
 */
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
    public static <ModelClass extends Model> void sync(boolean async, ModelContainer<ModelClass, ?> modelContainer, ContainerAdapter<ModelClass> containerAdapter, @SqlUtils.SaveMode int mode) {
        if (!async) {

            BaseDatabaseDefinition flowManager = FlowManager.getDatabaseForTable(modelContainer.getTable());
            ModelAdapter<ModelClass> modelAdapter = modelContainer.getModelAdapter();

            final SQLiteDatabase db = flowManager.getWritableDatabase();

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
                ContentValues contentValues = new ContentValues();
                containerAdapter.bindToContentValues(contentValues, modelContainer);
                exists = (db.update(modelAdapter.getTableName(), contentValues, containerAdapter.getPrimaryModelWhere(modelContainer).getQuery(), null)!=0);
            }

            if (!exists) {
                SQLiteStatement insertStatement = modelAdapter.getInsertStatement();
                containerAdapter.bindToStatement(insertStatement, modelContainer);
                long id = insertStatement.executeInsert();
                //containerAdapter.updateAutoIncrement(modelContainer, id);

                /*Collection<Field> primaryKeys = tableStructure.getPrimaryKeys();
                for (Field field : primaryKeys) {
                    if (StructureUtils.isPrimaryKeyAutoIncrement(field)) {
                        field.setAccessible(true);
                        try {
                            field.set(mode, id);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }*/
            }

            SqlUtils.notifyModelChanged(modelContainer.getTable(), action);

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
    public static <ModelClass extends Model> void delete(final ModelContainer<ModelClass, ?> modelContainer,
                                                         ContainerAdapter<ModelClass> containerAdapter, boolean async) {
        if (!async) {
            new Delete().from(modelContainer.getTable()).where(containerAdapter.getPrimaryModelWhere(modelContainer)).query();
            SqlUtils.notifyModelChanged(modelContainer.getTable(), BaseModel.Action.DELETE);
        } else {
            TransactionManager.getInstance().delete(ProcessModelInfo.withModels(modelContainer));
        }
    }
}
