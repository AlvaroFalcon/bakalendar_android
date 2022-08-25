package com.frostfel.animelist.data

enum class DayOfWeek(val value: Int, val strValue: String) {
    MONDAY(1, "mondays"),
    TUESDAY(2, "tuesdays"),
    WEDNESDAY(3, "wednesdays"),
    THURSDAY(4, "thursdays"),
    FRIDAY(5, "fridays"),
    SATURDAY(6, "saturdays"),
    SUNDAY(7, "sundays"),
    UNKNOWN(8, "UNKNOWN");

    companion object {
        fun from(day: String?): DayOfWeek {
            return when (day?.lowercase()) {
                MONDAY.strValue -> MONDAY
                TUESDAY.strValue -> TUESDAY
                WEDNESDAY.strValue -> WEDNESDAY
                THURSDAY.strValue -> THURSDAY
                FRIDAY.strValue -> FRIDAY
                SATURDAY.strValue -> SATURDAY
                SUNDAY.strValue -> SUNDAY
                else -> UNKNOWN
            }
        }
    }
}