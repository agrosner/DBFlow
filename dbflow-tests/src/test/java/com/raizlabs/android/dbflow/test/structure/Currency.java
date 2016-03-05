package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.List;

@Table(database = TestDatabase.class)
public class Currency extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    @Unique
    public String symbol;

    @Column
    public String shortName;

    @Column
    @Unique
    public String name;

    public static List<Currency> getCurrencies() {
        List<Currency> currencies = SQLite.select().from(Currency.class).queryList();
        if (currencies.isEmpty()) {
            //GCMCurrencyData.getCurrencyData().loadCurrenciesInToDatabase();
            currencies = SQLite.select().from(Currency.class).queryList();
        }
        return currencies;
    }
}
