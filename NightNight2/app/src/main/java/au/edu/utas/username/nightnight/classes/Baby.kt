package au.edu.utas.username.nightnight.classes

import com.google.firebase.firestore.Exclude

class Baby (
    @get:Exclude var id : String? = null,

    var babyName : String? = null,
    var dateOfBirth: String? = null,
    var gender: String? = null,
    var image: String? = null
)