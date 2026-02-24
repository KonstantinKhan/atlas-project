package com.khan366kos.atlas.project.backend.transport

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Рабочий календарь с настройками выходных и праздничных дней.
 *
 * @property weekendDays Дни недели, считающиеся выходными (0 = воскресенье, 6 = суббота). По умолчанию: [0, 6].
 * @property holidays Список праздничных дней в формате ISO 8601 (YYYY-MM-DD). Пример: ["2026-02-23", "2026-03-09"].
 * @property workingWeekends Список рабочих выходных дней в формате ISO 8601 (YYYY-MM-DD), если есть.
 */
@Serializable
data class WorkCalendar(
    @SerialName("weekendDays")
    val weekendDays: List<Int> = listOf(0, 6),
    @SerialName("holidays")
    val holidays: List<String> = listOf(
        "2026-02-23",
        "2026-03-09"
    ),
    @SerialName("workingWeekends")
    val workingWeekends: List<String>? = null,
)
