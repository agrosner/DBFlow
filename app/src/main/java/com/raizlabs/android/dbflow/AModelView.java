package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.structure.BaseModelView;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@ModelView(query = "SELECT time from AModel where time > 0")
public class AModelView extends BaseModelView<AModel> {

    @Column
    long time;
}
