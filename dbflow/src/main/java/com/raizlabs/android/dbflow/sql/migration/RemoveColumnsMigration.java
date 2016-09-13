package com.raizlabs.android.dbflow.sql.migration;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Allows you to remove columns from an existing {@link Model}.
 */
public class RemoveColumnsMigration <TModel extends Model, BModel extends Model>
    extends BaseMigration {
    private Class<TModel> mModel;
    private Class<BModel> mBackupModel;

    public RemoveColumnsMigration (Class<TModel> model, Class<BModel> backupModel) {
        this.mModel = model;
        this.mBackupModel = backupModel;
    }

    @Override
    public final void migrate (DatabaseWrapper database) {
        ModelAdapter<TModel> modelAdapter = FlowManager.getModelAdapter (this.mModel);
        ModelAdapter<BModel> backupModelAdapter = FlowManager.getModelAdapter (this.mBackupModel);

        // 0. Drop the backup table if it already exists. This is a necessary step since
        // DBFlow will already have created the table by this point.
        QueryBuilder dropBackupQuery =
            new QueryBuilder ("DROP TABLE IF EXISTS")
                .appendSpace ()
                .append (backupModelAdapter.getTableName ());

        database.execSQL (dropBackupQuery.getQuery ());

        // 1. Rename the original table as backup.
        QueryBuilder renameQuery =
            new QueryBuilder ("ALTER TABLE")
                .append (modelAdapter.getTableName ())
                .appendSpace ()
                .append ("RENAME TO")
                .appendSpace ()
                .append (backupModelAdapter.getTableName ());

        database.execSQL (renameQuery.getQuery ());

        // 2. Recreate the original table with fewer columns.
        database.execSQL (modelAdapter.getCreationQuery ());

        // 3. Copy data from the backup table to the new table.
        SQLite.insert (this.mModel)
              .columns (modelAdapter.getAllColumnProperties ())
              .select (SQLite.select (modelAdapter.getAllColumnProperties ()).from (this.mBackupModel))
              .execute (database);

        // 4. Drop the backup table.
        database.execSQL (dropBackupQuery.getQuery ());
    }
}
