package com.raizlabs.android.dbflow.structure.onetomany;

import com.raizlabs.android.dbflow.TestDatabase;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.TestModel2;
import com.raizlabs.android.dbflow.structure.TestModel2_Table;

import java.util.List;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
public class OneToManyJavaModel {

    @PrimaryKey
    String name;

    List<TestModel2> orders;

    public void setOrders(List<TestModel2> orders) {
        this.orders = orders;
    }

    @OneToMany(methods = OneToMany.Method.ALL, isVariablePrivate = true,
        variableName = "orders")
    List<? extends TestModel2> getRelatedOrders() {
        if (orders == null) {
            orders = SQLite.select().from(TestModel2.class)
                .where(TestModel2_Table.model_order.greaterThan(3))
                .queryList();
        }
        return orders;
    }
}
