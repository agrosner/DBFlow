package com.raizlabs.android.dbflow.test.structure

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.converter.TypeConverter
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.structure.BaseQueryModel
import com.raizlabs.android.dbflow.test.TestDatabase

@QueryModel(database = TestDatabase::class)
class CustomBlobModel : BaseQueryModel() {

    class MyBlob(val blob: ByteArray)

    @com.raizlabs.android.dbflow.annotation.TypeConverter
    class MyTypeConverter : TypeConverter<Blob, MyBlob>() {

        override fun getDBValue(model: MyBlob): Blob {
            return Blob(model.blob)
        }

        override fun getModelValue(data: Blob): MyBlob {
            return MyBlob(data.blob)
        }
    }

    @Column
    var myBlob: MyBlob? = null
}