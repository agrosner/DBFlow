package com.raizlabs.android.dbflow.sql;

import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 */
public class ForeignKeyCreationBuilder extends QueryBuilder<ForeignKeyCreationBuilder> {

    private final List<String> foreignColumns = new ArrayList<>();

    private final List<String> columns = new ArrayList<>();

    private final List<ForeignKeyAction> updateConflicts = new ArrayList<>();

    private final List<ForeignKeyAction> deleteConflicts = new ArrayList<>();

    public ForeignKeyCreationBuilder() {
        super("FOREIGN KEY(");
    }

    /**
     * Adds a reference to this builder.
     * @param foreignKeyReference The reference to use.
     * @param updateConflict The update cof
     * @param deleteConflict
     * @return
     */
    public ForeignKeyCreationBuilder addReference(ForeignKeyReference foreignKeyReference, ForeignKeyAction updateConflict, ForeignKeyAction deleteConflict){
        foreignColumns.add(foreignKeyReference.foreignKeyColumnName());
        columns.add(foreignKeyReference.columnName());
        updateConflicts.add(updateConflict);
        deleteConflicts.add(deleteConflict);

        return this;
    }
}
