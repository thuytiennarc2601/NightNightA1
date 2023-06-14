package au.edu.utas.username.nightnight.classes

import com.google.firebase.firestore.Exclude

class Feed{
    @get:Exclude var id: String? = null

    var type: String? = null
    var date: String? = null
    var dateID: String? = null
    var startTime: String? = null
    var note: String? = null

    //breastfeed
    var startSide: String? = null
    var endSide: String? = null
    var leftDuration: Long = 0
    var rightDuration: Long = 0
    var totalDuration: Long = 0

    //bottlefeed
    var amount: Float = 0.0f

    //babymeals
    var recipe: String? = null

    fun clearData()
    {
        id = null
        dateID = null
        startTime = null
        startSide = null
        endSide = null
        leftDuration = 0
        rightDuration = 0
        note = null
        totalDuration = 0
        amount = 0.0f
        recipe = null
    }
}
/**
class Breastfeeding {
    @get:Exclude var id: String? = null

    var type: String? = null
    var date: String? = null
    var dateID: String? = null
    var startTime: String? = null

    //breastfeed
    var startSide: String? = null
    var endSide: String? = null
    var leftDuration: Long = 0
    var rightDuration: Long = 0
    var note: String? = null
    var totalDuration: Long = 0

    fun clearData()
    {
        id = null
        dateID = null
        startTime = null
        startSide = null
        endSide = null
        leftDuration = 0
        rightDuration = 0
        note = null
        totalDuration = 0
    }
}

class BottleFeed{
    @get:Exclude var id: String? = null

    var amount: Int? = null
    var dateID: String? = null
    var note: String? = null
    var startTime: String? = null

    fun clearData()
    {
        dateID = null
        id = null
        startTime = null
        amount = null
        note = null
    }
}

class BabyMeal{
    @get:Exclude var id: String? = null

    var startTime: String? = null
    var dish: String? = null
    var note: String? = null

    fun clearData()
    {
        id = null
        startTime = null
        dish = null
        note = null
    }
}
 **/