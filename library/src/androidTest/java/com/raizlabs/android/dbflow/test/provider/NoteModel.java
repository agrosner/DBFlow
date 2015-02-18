package com.raizlabs.android.dbflow.test.provider;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME)
public class NoteModel extends BaseModel {


    @Column(columnType = Column.PRIMARY_KEY_AUTO_INCREMENT)
    long id;

    @Column(columnType = Column.FOREIGN_KEY,
            references = {@ForeignKeyReference(columnName = "providerModel",
                    columnType = long.class, foreignColumnName = "id")})
    ContentProviderModel contentProviderModel;

    @Column
    String note;
}
