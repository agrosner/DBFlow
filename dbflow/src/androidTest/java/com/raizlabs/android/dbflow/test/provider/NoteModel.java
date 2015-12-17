package com.raizlabs.android.dbflow.test.provider;

import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.provider.BaseProviderModel;

/**
 * Description:
 */
@Table(database = ContentDatabase.class)
public class NoteModel extends BaseProviderModel<NoteModel> {


    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "providerModel",
            columnType = long.class, foreignKeyColumnName = "id")})
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
