package com.dbflow5.adapter

import com.dbflow5.config.DBFlowDatabase

/**
 * Description: Generated [VirtualTable] class.
 */
abstract class VirtualTableAdapter<T : Any>(db: DBFlowDatabase) : RetrievalAdapter<T>(db), CreationAdapter