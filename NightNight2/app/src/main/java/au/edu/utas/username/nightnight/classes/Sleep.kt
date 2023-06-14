package au.edu.utas.username.nightnight.classes

import com.google.firebase.firestore.Exclude

class Sleep {
    @get:Exclude var id: String? = null

    var dateID: String? = null
    var date: String? = null
    var startTime: String? = null
    var duration: Long = 0
    var note: String? = null

    fun clearData()
    {
        id = null
        date = null
        dateID = null
        startTime = null
        duration = 0
        note = null
    }
}