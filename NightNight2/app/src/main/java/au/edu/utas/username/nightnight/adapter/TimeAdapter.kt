package au.edu.utas.username.nightnight.adapter

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

//The reference of the code is from a youtube_link: https://www.youtube.com/watch?v=lDpd3mLWYK4
class TimeAdapter (context: Context) {
    private var sharePref : SharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE)
    private var dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())

    private var timerCounting = false
    private var secondTimerCounting = false
    private var startTime: Date? = null
    private var secondStartTime: Date? = null
    private var stopTime: Date? = null
    private var secondStopTime: Date? = null
    private var leftDuration: Long = 0
    private var rightDuration: Long = 0

    init
    {
        /**
        setStartTime(null)
        setStopTime(null)
        setSecondStartTime(null)
        setSecondStopTime(null)
        setTimerCounting(false)
        setSecondTimerCounting(false)
        **/

        timerCounting = sharePref.getBoolean(COUNTING_KEY, false)
        secondTimerCounting = sharePref.getBoolean(SECOND_COUNTING_KEY, false)
        leftDuration = sharePref.getLong(LEFT_DURATION_KEY, 0)
        rightDuration = sharePref.getLong(RIGHT_DURATION_KEY, 0)
        val startString = sharePref.getString(START_TIME_KEY, null)
        if(startString != null)
        {
            startTime = dateFormat.parse(startString)
        }

        val secondStartString = sharePref.getString(SECOND_START_TIME_KEY, null)
        if(secondStartString != null)
        {
            secondStartTime = dateFormat.parse(secondStartString)
        }

        val stopString = sharePref.getString(STOP_TIME_KEY, null)
        if(stopString != null)
        {
            stopTime = dateFormat.parse(stopString)
        }

        val secondStopString = sharePref.getString(SECOND_STOP_TIME_KEY, null)
        if(secondStopString != null)
        {
            secondStopTime = dateFormat.parse(secondStopString)
        }
    }

    fun startTime(): Date? = startTime
    //set and save start time to the share reference
    fun setStartTime(date: Date?)
    {
        startTime = date
        with(sharePref.edit())
        {
            val dateString = if (date == null) null else dateFormat.format(date)
            putString(START_TIME_KEY, dateString)
            apply()
        }
    }

    fun secondStartTime(): Date? = secondStartTime
    //set and save start time to the share reference
    fun setSecondStartTime(date: Date?)
    {
        secondStartTime = date
        with(sharePref.edit())
        {
            val dateString = if (date == null) null else dateFormat.format(date)
            putString(SECOND_START_TIME_KEY, dateString)
            apply()
        }
    }

    fun stopTime(): Date? = stopTime
    //set and save stop time to the share reference
    fun setStopTime(date: Date?)
    {
        stopTime = date
        with(sharePref.edit())
        {
            val dateString = if (date == null) null else dateFormat.format(date)
            putString(STOP_TIME_KEY, dateString)
            apply()
        }
    }

    fun secondStopTime(): Date? = secondStopTime
    //set and save stop time to the share reference
    fun setSecondStopTime(date: Date?)
    {
        secondStopTime = date
        with(sharePref.edit())
        {
            val dateString = if (date == null) null else dateFormat.format(date)
            putString(SECOND_STOP_TIME_KEY, dateString)
            apply()
        }
    }

    fun timerCounting(): Boolean = timerCounting
    fun setTimerCounting(value: Boolean)
    {
        timerCounting = value
        with(sharePref.edit())
        {
           putBoolean(COUNTING_KEY, value)
            apply()
        }
    }

    fun secondTimerCounting(): Boolean = secondTimerCounting
    fun setSecondTimerCounting(value: Boolean)
    {
        secondTimerCounting = value
        with(sharePref.edit())
        {
            putBoolean(SECOND_COUNTING_KEY, value)
            apply()
        }
    }

    fun leftDuration(): Long = leftDuration
    fun setLeftDuration(value: Long)
    {
        leftDuration = value
        with(sharePref.edit())
        {
            putLong(LEFT_DURATION_KEY, value)
            apply()
        }
    }

    fun rightDuration(): Long = rightDuration
    fun setRightDuration(value: Long)
    {
        rightDuration = value
        with(sharePref.edit())
        {
            putLong(RIGHT_DURATION_KEY, value)
            apply()
        }
    }

    fun calcRestartTime(start: Long, stop: Long): Date {
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

    fun millisecondsFromString(timeString: String): Long
    {
        var milliseconds = 0L
        if(isValidTimeFormat(timeString))
        {
            val timeSection = timeString.split(":").map {it.toInt()}
            val hours = timeSection[0]
            val minutes = timeSection[1]
            val seconds = timeSection[2]
            milliseconds = ((hours * 3600 + minutes * 60 + seconds) * 1000).toLong()
        }
        return milliseconds
    }

    fun isValidTimeFormat(time: String): Boolean {
        val regex = Regex("^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)\$")
        return regex.matches(time)
    }

    companion object
    {
        const val PREFERENCE = "preferences"
        const val START_TIME_KEY = "start_time"
        const val SECOND_START_TIME_KEY = "second_start_time"
        const val STOP_TIME_KEY = "stop_time"
        const val SECOND_STOP_TIME_KEY = "second_stop_time"
        const val COUNTING_KEY = "counting"
        const val SECOND_COUNTING_KEY = "left_counting"
        const val LEFT_DURATION_KEY = "left_duration"
        const val RIGHT_DURATION_KEY = "right_duration"
    }

}