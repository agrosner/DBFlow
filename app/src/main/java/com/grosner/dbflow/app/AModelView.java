package com.grosner.dbflow.app;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ModelView;
import com.grosner.dbflow.structure.BaseModelView;

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
