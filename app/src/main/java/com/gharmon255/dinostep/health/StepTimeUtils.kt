package com.gharmon255.dinostep.health

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object StepTimeUtils {
    fun startOfToday(zoneId: ZoneId = ZoneId.systemDefault()): Instant {
        return LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()
    }

    fun startOfTodayMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return startOfToday(zoneId).toEpochMilli()
    }

    fun startOfYesterday(zoneId: ZoneId = ZoneId.systemDefault()): Instant {
        return LocalDate.now(zoneId).minusDays(1).atStartOfDay(zoneId).toInstant()
    }

    fun startOfYesterdayMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return startOfYesterday(zoneId).toEpochMilli()
    }
}
