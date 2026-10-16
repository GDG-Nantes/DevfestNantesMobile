package com.gdgnantes.devfest.core.model.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Parses an ISO-8601 instant with an offset (`2025-10-16T11:00:00+02:00`) or the UTC `Z` form
 * (`2025-10-16T09:00:00Z`). Returns null instead of throwing on anything unparseable, including
 * empty, date-only and zone-less input.
 */
@OptIn(ExperimentalTime::class, ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun String.toInstantOrNull(): Instant? = Instant.parseOrNull(this)

/**
 * The calendar date of this instant in [timeZone] (device zone by default). [LocalDate.toString]
 * is always ASCII `yyyy-MM-dd`, whatever the device locale.
 */
@OptIn(ExperimentalTime::class, ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun Instant.toLocalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    toLocalDateTime(timeZone).date
