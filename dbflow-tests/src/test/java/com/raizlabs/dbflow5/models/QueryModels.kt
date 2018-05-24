package com.raizlabs.dbflow5.models

import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.annotation.Column
import com.raizlabs.dbflow5.annotation.QueryModel
import com.raizlabs.dbflow5.converter.TypeConverter
import com.raizlabs.dbflow5.data.Blob

@QueryModel(database = TestDatabase::class, allFields = true)
class AuthorNameQuery(var blogName: String = "",
                      var authorId: Int = 0, var blogId: Int = 0)


@QueryModel(database = TestDatabase::class)
class CustomBlobModel(@Column var myBlob: MyBlob? = null) {

    class MyBlob(val blob: ByteArray)

    @com.raizlabs.dbflow5.annotation.TypeConverter
    class MyTypeConverter : TypeConverter<Blob, MyBlob>() {

        override fun getDBValue(model: MyBlob?) = model?.let { Blob(model.blob) }

        override fun getModelValue(data: Blob?) = data?.blob?.let { MyBlob(it) }
    }
}

@QueryModel(database = TestDatabase::class, allFields = true)
class AllFieldsQueryModel(var model: String? = null)