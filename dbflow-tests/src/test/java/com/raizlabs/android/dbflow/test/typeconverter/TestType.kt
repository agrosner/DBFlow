package com.raizlabs.android.dbflow.test.typeconverter

import android.location.Location
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import org.json.JSONObject
import java.util.*

@Table(database = TestDatabase::class, useBooleanGetterSetters = false)
class TestType : TestModel1() {

    @Column
    var nativeBoolean: Boolean = false

    @Column
    var aBoolean: Boolean? = null

    @Column
    var calendar: Calendar? = null

    @Column
    var date: Date? = null

    @Column
    var sqlDate: java.sql.Date? = null

    @Column
    var json: JSONObject? = null

    @Column
    var location: Location? = null

    @Column(typeConverter = CustomBooleanConverter::class)
    var thisHasCustom: Boolean? = null

    @Column(typeConverter = EnumOverriderConverter::class)
    var testEnum: EnumOverriderConverter.TestEnum? = null

    @Column
    var blobable: Blobable? = null

    @ForeignKey
    var primary: UPrimary? = null
}
