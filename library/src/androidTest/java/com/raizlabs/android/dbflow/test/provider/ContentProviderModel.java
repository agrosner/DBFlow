package com.raizlabs.android.dbflow.test.provider;

import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.provider.BaseProviderModel;
import com.raizlabs.android.dbflow.structure.provider.ContentUtils;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@TableEndpoint(value = ContentProviderModel.NAME, contentProviderName = "ContentDatabase")
@Table(databaseName = ContentDatabase.NAME, value = ContentProviderModel.NAME)
public class ContentProviderModel extends BaseProviderModel<ContentProviderModel> {

    public static final String NAME = "ContentProviderModel";

    @ContentUri(path = NAME, type = ContentUri.ContentType.VND_MULTIPLE + NAME)
    public static final Uri CONTENT_URI = ContentUtils.buildUri(ContentDatabase.AUTHORITY);

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
