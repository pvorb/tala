package de.vorb.tala

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

object Utils {
    val utc: TimeZone = TimeZone.getTimeZone("UTC")
    private val f: DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    def dateToISO8601(date: Date): String = f.format(date)
}
