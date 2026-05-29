package com.gharmon255.dinostep.shared.wear

enum class WearSyncEventType {
    NONE,
    HATCHED,
    GREW,
    COMPLETED,
    ;

    companion object {
        fun fromRaw(value: String?): WearSyncEventType {
            return entries.find { it.name == value } ?: NONE
        }
    }
}
