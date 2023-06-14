package au.edu.utas.username.nightnight.adapter

import android.content.Context
import android.content.SharedPreferences

class DataStorer (context: Context) {

    private var sharePref : SharedPreferences = context.getSharedPreferences(DATA_PREFERENCE, Context.MODE_PRIVATE)

    private var currentActivity: String? = "None"
    private var eventStartTime: String? = "00:00"
    private var eventDescription: String? = "No description"
    private var breastFeedStartSide: String? = "None"
    private var breastFeedEndSide: String? = "None"
    private var eventID: String? = "defaultID"

    init
    {
        eventStartTime = sharePref.getString(START_EVENT_TIME_KEY, "00:00")
        eventDescription = sharePref.getString(DESCRIPTION, "No description")
        breastFeedStartSide = sharePref.getString(START_SIDE_KEY, "none")
        breastFeedEndSide = sharePref.getString(END_SIDE_KEY, "none")
        eventID = sharePref.getString(ID_KEY, "none")
        currentActivity = sharePref.getString(CURRENT_ACTIVITY_KEY, "none")
    }

    fun eventStartTime(): String? = eventStartTime
    fun setEventStartTime(timeString: String?)
    {
        eventStartTime = timeString
        with(sharePref.edit())
        {
            val string = if (timeString == "" || timeString == "00:00") "00:00" else timeString
            putString(START_EVENT_TIME_KEY, string)
            apply()
        }
    }

    fun eventDescription(): String? = eventDescription
    fun setEventDescription(desString: String?)
    {
        eventDescription = desString
        with(sharePref.edit())
        {
            val string = if (desString == "" || desString == "Describe the event...") "No description" else desString
            putString(DESCRIPTION, string)
            apply()
        }
    }

    fun breastFeedStartSide(): String? = breastFeedStartSide
    fun setBreastFeedStartSide(side: String?)
    {
        breastFeedStartSide = side
        with(sharePref.edit())
        {
            val string = if (side == "" || side == "none") "none" else side
            putString(START_SIDE_KEY, string)
            apply()
        }
    }

    fun breastFeedEndSide(): String? = breastFeedEndSide
    fun setBreastFeedEndSide(side: String?)
    {
        breastFeedEndSide = side
        with(sharePref.edit())
        {
            val string = if (side == "" || side == "none") "none" else side
            putString(END_SIDE_KEY, string)
            apply()
        }
    }

    fun eventID(): String? = eventID
    fun setEventID(id: String?)
    {
        eventID = id
        with(sharePref.edit())
        {
            val string = if (id == "" || id == "defaultID") "none" else id
            putString(ID_KEY, string)
            apply()
        }
    }

    fun currentActivity(): String? = currentActivity
    fun setCurrentActivity(activity: String?)
    {
        currentActivity = activity
        with(sharePref.edit())
        {
            val string = if (activity == "" || activity == "none") "none" else activity
            putString(CURRENT_ACTIVITY_KEY, string)
            apply()
        }
    }

    companion object{
        const val START_EVENT_TIME_KEY = "event_start_time"
        const val DESCRIPTION = "description"
        const val START_SIDE_KEY = "start_side_key"
        const val END_SIDE_KEY = "end_side_key"
        const val DATA_PREFERENCE = "data_preference"
        const val ID_KEY = "id_key"
        const val CURRENT_ACTIVITY_KEY = "current_activity_key"
    }

}