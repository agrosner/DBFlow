package com.raizlabs.android.dbflow.test.typeconverter

import com.raizlabs.android.dbflow.converter.TypeConverter
import com.raizlabs.android.dbflow.data.Blob

/**
 * Description: Test type of converter that converts to blob.
 */
@com.raizlabs.android.dbflow.annotation.TypeConverter
class BlobableConverter : TypeConverter<Blob, Blobable>() {
    override fun getDBValue(model: Blobable?): Blob? {
        return model?.blob
    }

    override fun getModelValue(data: Blob?): Blobable? {
        if (data == null) {
            return null
        } else {
            val blobable = Blobable()
            blobable.blob = data
            return blobable
        }
    }
}
