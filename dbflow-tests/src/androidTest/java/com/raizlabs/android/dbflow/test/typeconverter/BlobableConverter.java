package com.raizlabs.android.dbflow.test.typeconverter;

import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.data.Blob;

/**
 * Description: Test type of converter that converts to blob.
 */
@com.raizlabs.android.dbflow.annotation.TypeConverter
public class BlobableConverter extends TypeConverter<Blob, Blobable> {
    @Override
    public Blob getDBValue(Blobable model) {
        return model == null ? null : model.blob;
    }

    @Override
    public Blobable getModelValue(Blob data) {
        if (data == null) {
            return null;
        } else {
            Blobable blobable = new Blobable();
            blobable.blob = data;
            return blobable;
        }
    }
}
