package com.grosner.dbflow.jack.tests;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.annotation.ModelViewQuery;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModelView;

/**
 * Description:
 */
@ModelView(database = AppDatabase.class)
public class AModelView extends BaseModelView {

    @ModelViewQuery
    public static final Query QUERY = new Select(AModel_Table.time)
            .from(AModel.class).where(AModel_Table.time.greaterThan(0l));

    @Column
    long time;
}
