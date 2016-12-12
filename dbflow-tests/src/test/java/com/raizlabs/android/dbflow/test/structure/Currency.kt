package com.raizlabs.android.dbflow.test.structure

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.annotation.Unique
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

@Table(database = TestDatabase::class)
class Currency : BaseModel() {

    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    @Unique
    var symbol: String? = null

    @Column
    var shortName: String? = null

    @Column
    @Unique
    var name: String? = null

    companion object {

        //GCMCurrencyData.getCurrencyData().loadCurrenciesInToDatabase();
        val currencies: List<Currency>
            get() {
                var currencies = SQLite.select().from(Currency::class.java).queryList()
                if (currencies.isEmpty()) {
                    currencies = SQLite.select().from(Currency::class.java).queryList()
                }
                return currencies
            }
    }
}
