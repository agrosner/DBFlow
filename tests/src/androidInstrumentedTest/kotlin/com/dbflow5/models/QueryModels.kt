package com.dbflow5.models

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.Query
import com.dbflow5.converter.TypeConverter
import com.dbflow5.data.Blob

@Query(allFields = true)
class AuthorNameQuery(
    var blogName: String = "",
    var authorId: Int = 0, var blogId: Int = 0
)

@Query
class CustomBlobModel(@Column var myBlob: MyBlob? = null) {

    class MyBlob(val blob: ByteArray)

    @com.dbflow5.annotation.TypeConverter
    class MyTypeConverter : TypeConverter<Blob, MyBlob> {

        override fun getDBValue(model: MyBlob) = Blob(model.blob)

        override fun getModelValue(data: Blob) = MyBlob(data.blob)
    }
}

@Query(allFields = true)
class AllFieldsQueryModel(var fieldModel: String? = null)