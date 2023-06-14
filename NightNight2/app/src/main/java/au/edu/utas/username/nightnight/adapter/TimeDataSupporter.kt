package au.edu.utas.username.nightnight.adapter

import java.util.*

class TimeDataSupporter {
    fun calcRestartTime(start: Long, stop: Long, extra: Long): Date {
        val diff = start - stop
        return Date(System.currentTimeMillis() + diff)
    }

    fun timeStringFromLong(milliseconds: Long?, textDisplay: Boolean): String {
        var stringTime = ""
        if(milliseconds != null) {
            val seconds = (milliseconds / 1000) % 60
            val minutes = (milliseconds / (1000 * 60) % 60)
            val hours = ((milliseconds / (1000 * 60 * 60)) % 24)
            stringTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            if (textDisplay) {
                stringTime = String.format("%02dh%02dm%02ds", hours, minutes, seconds)
            }
        }
        return stringTime
    }
}