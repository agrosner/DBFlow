package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@ModelContainer
@Table(databaseName = TestDatabase.NAME)
public class AIContainerForeign extends AutoIncrementContainer {


    @Column
    @ForeignKey(references = {
            @ForeignKeyReference(columnType = Long.class, columnName = "ai_container_id", foreignKeyColumnName = "id")})
    AutoIncrementContainer foreignModel;

    @Column
    @ForeignKey(references = {
            @ForeignKeyReference(columnType = Long.class, columnName = "ai_container_id_container",
                                 foreignKeyColumnName = "id")})
    ForeignKeyContainer<AutoIncrementContainer> container;

    public void setContainer(AutoIncrementContainer autoIncrementContainer) {
        container = new ForeignKeyContainer<>(AutoIncrementContainer.class);
        container.put(AutoIncrementContainer$Table.A_ID, autoIncrementContainer.a_id);
        container.put(AutoIncrementContainer$Table.NAME, autoIncrementContainer.name);
        container.put(AutoIncrementContainer$Table.ID, autoIncrementContainer.id);
    }
}
