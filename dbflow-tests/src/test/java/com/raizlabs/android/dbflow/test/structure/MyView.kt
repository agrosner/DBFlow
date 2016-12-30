package com.raizlabs.android.dbflow.test.structure

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ModelView
import com.raizlabs.android.dbflow.annotation.ModelViewQuery
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.test.TestDatabase

@ModelView(database = TestDatabase::class, name = "v_view", priority = 3)
class MyView {

    enum class TestEnum {
        YES, NO
    }

    @Column
    var value: TestEnum? = null

    @Column(getterName = "isSet", setterName = "setSet")
    var isSet: Boolean? = null

    @Column(name = "is_up_next", getterName = "isUpNext", setterName = "setUpNext")
    var isUpNext: Boolean = false

    @Column(name = "is_favorite", getterName = "isFavorite", setterName = "setFavorite")
    var isFavorite: Boolean = false

    companion object {

        @JvmField
        @ModelViewQuery
        val QUERY: Query = SQLite.select().from(TestModel1::class.java)
    }
}
