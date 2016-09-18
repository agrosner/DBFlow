package com.raizlabs.android.dbflow.test.structure.onetomany;

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
@Table(database = TestDatabase.class)
public class OneToManyModel extends BaseModel {

    @PrimaryKey
    String name;

    List<OneToManyModelNonModel> orders;

    @OneToMany(methods = {OneToMany.Method.ALL})
    public List<OneToManyModelNonModel> getOrders() {
        if (orders == null) {
            orders = new Select().from(OneToManyModelNonModel.class)
                    .queryList();
        }
        return orders;
    }

}
