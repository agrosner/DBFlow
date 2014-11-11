package com.grosner.dbflow.structure.container;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.config.BaseDatabaseDefinition;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.sql.language.Delete;
import com.grosner.dbflow.structure.BaseModel;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelAdapter;

/**
 * Author: andrewgrosner
 * Description: Provides helper methods for handling {@link com.grosner.dbflow.structure.container.ModelContainer} classes.
 * These wrap around {@link com.grosner.dbflow.structure.Model} to provide more convenient means of interacting with the db.
 */
public class ModelContainerUtils {

    /**
     * Syncs a {@link com.grosner.dbflow.structure.container.BaseModelContainer} to the database.
     *
     * @param modelContainer The container model to save
     * @param async          Where it goes on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} or done immediately/
     * @param mode           The save mode, can be {@link com.grosner.dbflow.sql.SqlUtils#SAVE_MODE_DEFAULT},
     *                       {@link com.grosner.dbflow.sql.SqlUtils#SAVE_MODE_INSERT}, {@link com.grosner.dbflow.sql.SqlUtils#SAVE_MODE_UPDATE}
     * @param <ModelClass>   The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> void sync(boolean async, ModelContainer<ModelClass, ?> modelContainer, ContentValues contentValues, @SqlUtils.SaveMode int mode) {
        if (!async) {

            BaseDatabaseDefinition flowManager = FlowManager.getManagerForTable(modelContainer.getTable());
            ContainerAdapter<ModelClass> containerAdapter = flowManager.getModelContainerAdapterForTable(modelContainer.getTable());
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
                exists = (db.update(modelAdapter.getTableName(), contentValues, containerAdapter.getPrimaryModelWhere(modelContainer).getQuery(), null) != 0);
            }

            if (!exists) {
                long id = db.insert(modelAdapter.getTableName(), null, contentValues);
                if(id != 0) {

                }

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
     * Deletes {@link com.grosner.dbflow.structure.Model} from the database using the specfied {@link com.grosner.dbflow.config.FlowManager}
     *
     * @param modelContainer The jsonModel that corresponds to an item in the DB we want to delete
     * @param async          Whether it goes on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} or done immediately.
     * @param <ModelClass>   The class that implements {@link com.grosner.dbflow.structure.Model}
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
