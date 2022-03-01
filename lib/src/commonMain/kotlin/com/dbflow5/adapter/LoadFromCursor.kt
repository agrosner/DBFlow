package com.dbflow5.adapter

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor

typealias LoadFromCursor<QueryType> = suspend DatabaseWrapper.(cursor: FlowCursor) -> QueryType
