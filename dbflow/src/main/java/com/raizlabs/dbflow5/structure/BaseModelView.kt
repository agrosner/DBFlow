package com.raizlabs.dbflow5.structure

import com.raizlabs.dbflow5.annotation.ModelView
import com.raizlabs.dbflow5.annotation.ModelViewQuery

/**
 * Description: Provides a base implementation for a ModelView. Use a [ModelView]
 * annotation to register it properly. Also you need to specify a singular
 * field via [ModelViewQuery].
 */
@Deprecated("No subclass needed. Use extension methods and modeladapters")
abstract class BaseModelView : NoModificationModel()
