package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.List;

/**
 * Description:
 */
@ModelContainer
@Table(database = TestDatabase.class)
public class OneToManyModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;

    List<TestModel2> orders;

    @OneToMany(methods = {OneToMany.Method.ALL})
    public List<TestModel2> getOrders() {
        if (orders == null) {
            orders = new Select().from(TestModel2.class)
                    .where(TestModel2_Table.model_order.greaterThan(3))
                    .queryList();
        }
        return orders;
    }

}
