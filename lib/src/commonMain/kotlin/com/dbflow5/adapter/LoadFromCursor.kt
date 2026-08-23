package com.dbflow5.adapter

import com.dbflow5.database.DatabaseConnection
import com.dbflow5.database.FlowCursor

typealias LoadFromCursor<QueryType> = suspend DatabaseConnection.(cursor: FlowCursor) -> QueryType
