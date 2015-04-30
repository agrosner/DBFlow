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
            @ForeignKeyReference(columnType = Long.class, columnName = "ai_container_id", foreignColumnName = "id")})
    AutoIncrementContainer foreignModel;

    @Column
    @ForeignKey(references = {
            @ForeignKeyReference(columnType = Long.class, columnName = "ai_container_id_container",
                                 foreignColumnName = "id")})
    ForeignKeyContainer<AutoIncrementContainer> container;
}
