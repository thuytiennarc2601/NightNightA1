package au.edu.utas.username.nightnight.classes

import com.google.firebase.firestore.Exclude

class Nappy {
    @get:Exclude var id: String? = null

    var dateID: String? = null
    var changingTime: String? = null
    var date: String? = null
    var type: String? = null
    var image: String? = null
    var note: String? = null

    fun clearData()
    {
        dateID = null
        changingTime = null
        date = null
        type = null
        image = null
        note = null
    }
}