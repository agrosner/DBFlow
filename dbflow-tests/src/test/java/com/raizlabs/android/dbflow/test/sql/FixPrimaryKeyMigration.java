package com.raizlabs.android.dbflow.test.sql;

import android.database.Cursor;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.sql.queriable.StringQuery;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Example of fixing a bug where Primary Key was treated as a {@link PrimaryKey#rowID()},
 * not a {@link PrimaryKey#autoincrement()}.
 */
public abstract class FixPrimaryKeyMigration<TableClass extends Model> extends BaseMigration {

    @Override
    public void migrate(DatabaseWrapper database) {
        Cursor tableSchema = getSelectTableQuery().query();

        if (tableSchema != null && tableSchema.moveToFirst()) {
            String creationQuery = tableSchema.getString(0);
            // we run query if it was incorrectly categorized this way
            if (validateCreationQuery(creationQuery)) {
                /// create table
                database.execSQL(getTempCreationQuery());

                String insertQuery = getInsertTransferQuery();
                database.execSQL(insertQuery);

                database.execSQL(String.format("DROP TABLE %1s", FlowManager.getTableName(getTableClass())));

                database.execSQL(String.format("ALTER TABLE %1s RENAME to %1s", QueryBuilder.quote(getTempTableName()),
                    FlowManager.getTableName(getTableClass())));
            } else {
                FlowLog.log(FlowLog.Level.I, String.format("Creation Query %1s is already in correct format.", creationQuery));
            }

        }

        if (tableSchema != null) {
            tableSchema.close();
        }
    }

    StringQuery<TableClass> getSelectTableQuery() {
        return new StringQuery<>(getTableClass(),
            String.format("SELECT sql FROM sqlite_master WHERE name='%1s'", getTableName()));
    }

    String getTempCreationQuery() {
        ModelAdapter adapter = FlowManager.getModelAdapter(getTableClass());
        String adapterCreationQuery = adapter.getCreationQuery();
        adapterCreationQuery = adapterCreationQuery.replace(getTableName(), getTempTableName());
        return adapterCreationQuery;
    }

    private String getTempTableName() {
        return getTableName() + "_temp";
    }

    boolean validateCreationQuery(String query) {
        return query.startsWith(
            String.format("CREATE TABLE %1s(%1s INTEGER,", QueryBuilder.quote(getTableName()),
                QueryBuilder.quote(FlowManager.getModelAdapter(getTableClass()).getAutoIncrementingColumnName())));
    }

    String getTableName() {
        return QueryBuilder.stripQuotes(FlowManager.getTableName(getTableClass()));
    }

    String getInsertTransferQuery() {
        String query = SQLite.insert(getTableClass())
            .asColumns()
            .select(SQLite
                .select(FlowManager.getModelAdapter(getTableClass())
                    .getAllColumnProperties()).from(getTableClass())).getQuery();
        query = query.replaceFirst(getTableName(), getTempTableName());
        return query;
    }

    protected abstract Class<TableClass> getTableClass();
}
