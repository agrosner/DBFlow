package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.converter.TypeConverter
import com.raizlabs.android.dbflow.data.Blob

@QueryModel(database = TestDatabase::class)
class AuthorNameQuery(@Column var blogName: String = "",
                      @Column var authorId: Int = 0, @Column var blogId: Int = 0)


@QueryModel(database = TestDatabase::class)
class CustomBlobModel(@Column var myBlob: MyBlob? = null) {

    class MyBlob(val blob: ByteArray)

    @com.raizlabs.android.dbflow.annotation.TypeConverter
    class MyTypeConverter : TypeConverter<Blob, MyBlob>() {

        override fun getDBValue(model: MyBlob) = Blob(model.blob)

        override fun getModelValue(data: Blob) = MyBlob(data.blob)
    }
}

@QueryModel(database = TestDatabase::class, allFields = true)
class AllFieldsQueryModel(var model: String? = null)