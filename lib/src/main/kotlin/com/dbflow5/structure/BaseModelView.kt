package com.dbflow5.structure

import com.dbflow5.annotation.ModelView
import com.dbflow5.annotation.ModelViewQuery

/**
 * Description: Provides a base implementation for a ModelView. Use a [ModelView]
 * annotation to register it properly. Also you need to specify a singular
 * field via [ModelViewQuery].
 */
abstract class BaseModelView : NoModificationModel()
