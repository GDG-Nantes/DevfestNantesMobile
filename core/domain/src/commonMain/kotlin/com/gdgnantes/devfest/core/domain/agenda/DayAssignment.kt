package com.gdgnantes.devfest.core.domain.agenda

import com.gdgnantes.devfest.core.model.utils.toInstantOrNull
import com.gdgnantes.devfest.core.model.utils.toLocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/**
 * How [GroupSessionsUseCase] assigns a session to a conference day (D-07). Both platform strategies
 * exist side by side; the configured days are always all emitted, in order, with `dayIndex` equal to
 * the position plus one.
 */
sealed interface DayAssignment

/**
 * Android strategy: fixed 24 hour UTC windows, exactly like `Agenda.Builder`. [dayStarts] are ISO
 * instants such as `Agenda.DAY_ONE_ISO`; a session belongs to the first day whose start is less than
 * 24 hours before it (`(start - dayStart).inWholeDays == 0`), and sessions outside every window are
 * dropped. `DaySchedule.date` is the configured string. The time zone passed to `group` is ignored
 * by design (D-19). A start that does not parse matches nothing.
 */
data class UtcDayWindows(val dayStarts: List<String>) : DayAssignment

/**
 * iOS strategy: local calendar days. [dates] are `yyyy-MM-dd` dates; a session belongs to the first
 * date equal to the calendar date of its start in the time zone passed to `group`.
 * `DaySchedule.date` is the configured string.
 */
data class LocalDateDays(val dates: List<String>) : DayAssignment

/** The configured day labels, in order: the `DaySchedule.date` of each emitted day. */
internal fun DayAssignment.dayLabels(): List<String> =
    when (this) {
        is UtcDayWindows -> dayStarts
        is LocalDateDays -> dates
    }

/**
 * The 0-based position of the day [start] belongs to, or null when it belongs to none. Never
 * throws: an unparseable day start simply matches nothing. [UtcDayWindows] does not read
 * [timeZone].
 */
internal fun DayAssignment.dayPosition(start: Instant, timeZone: TimeZone): Int? =
    when (this) {
        is UtcDayWindows ->
            dayStarts.indexOfFirst { dayStart ->
                val parsed = dayStart.toInstantOrNull()
                parsed != null && (start - parsed).inWholeDays == 0L
            }.takeIf { it >= 0 }

        is LocalDateDays -> dates.indexOf(start.toLocalDate(timeZone).toString()).takeIf { it >= 0 }
    }
