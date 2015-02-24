package com.raizlabs.android.dbflow.test.provider;

import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.provider.BaseProviderModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = ContentDatabase.NAME)
public class NoteModel extends BaseProviderModel<NoteModel> {


    @Column(columnType = Column.PRIMARY_KEY_AUTO_INCREMENT)
    long id;

    @Column(columnType = Column.FOREIGN_KEY,
            references = {@ForeignKeyReference(columnName = "providerModel",
                    columnType = long.class, foreignColumnName = "id")})
    ContentProviderModel contentProviderModel;

    @Column
    String note;

    @Column
    boolean isOpen;

    @Override
    public Uri getDeleteUri() {
        return TestContentProvider.NoteModel.CONTENT_URI;
    }

    @Override
    public Uri getInsertUri() {
        return TestContentProvider.NoteModel.CONTENT_URI;
    }

    @Override
    public Uri getUpdateUri() {
        return TestContentProvider.NoteModel.CONTENT_URI;
    }

    @Override
    public Uri getQueryUri() {
        return TestContentProvider.NoteModel.CONTENT_URI;
    }
}
