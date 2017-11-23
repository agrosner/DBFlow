package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.annotation.ModelView
import com.raizlabs.android.dbflow.annotation.ModelViewQuery

/**
 * Description: Provides a base implementation for a ModelView. Use a [ModelView]
 * annotation to register it properly. Also you need to specify a singular
 * field via [ModelViewQuery].
 */
@Deprecated("No subclass needed. Use extension methods and modeladapters")
abstract class BaseModelView : NoModificationModel()
