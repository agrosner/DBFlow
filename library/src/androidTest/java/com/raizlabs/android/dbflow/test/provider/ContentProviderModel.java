package com.raizlabs.android.dbflow.test.provider;

import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.provider.BaseProviderModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME)
public class ContentProviderModel extends BaseProviderModel<ContentProviderModel> {

    @Column(columnType = Column.PRIMARY_KEY_AUTO_INCREMENT)
    long id;

    @Column
    String notes;

    @Column
    String title;

    @Override
    public Uri getDeleteUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }

    @Override
    public Uri getInsertUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }

    @Override
    public Uri getUpdateUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }

    @Override
    public Uri getQueryUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }
}
