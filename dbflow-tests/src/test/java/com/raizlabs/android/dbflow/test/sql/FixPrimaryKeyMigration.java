package com.raizlabs.android.dbflow.test.sql;

import android.database.Cursor;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.sql.queriable.StringQuery;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description: Example of fixing a bug where Primary Key was treated as a {@link PrimaryKey#rowID()},
 * not a {@link PrimaryKey#autoincrement()}.
 */
@Migration(database = TestDatabase.class, version = 4)
public abstract class FixPrimaryKeyMigration<TableClass extends Model> extends BaseMigration {

    @Override
    public void migrate(DatabaseWrapper database) {
        Cursor tableSchema = new StringQuery<>(getTableClass(),
                String.format("SELECT sql FROM sqlite_master where name='%1s'", getTableName())).query();

        if (tableSchema != null && tableSchema.moveToFirst()) {
            String creationQuery = tableSchema.getString(0);
            ModelAdapter adapter = FlowManager.getModelAdapter(getTableClass());
            // we run query if it was incorrectly categorized this way
            if (creationQuery.startsWith(
                    String.format("CREATE TABLE %1s(%1s INTEGER,", getTableName(),
                            adapter.getAutoIncrementingColumnName()))) {

                String adapterCreationQuery = adapter.getCreationQuery();
                adapterCreationQuery = adapterCreationQuery.replace(getTableName(), getTableName() + "_temp");

                /// create table
                database.execSQL(adapterCreationQuery);


            }

        }
    }

    String getTableName() {
        return QueryBuilder.stripQuotes(FlowManager.getTableName(getTableClass()));
    }

    protected abstract Class<TableClass> getTableClass();
}
