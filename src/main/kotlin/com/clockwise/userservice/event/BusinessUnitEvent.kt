package com.clockwise.orgservice

enum class EventType {
    CREATED,
    UPDATED,
    DELETED
}

data class BusinessUnitEvent(
    val id: String,
    val name: String,
    val type: EventType
) 