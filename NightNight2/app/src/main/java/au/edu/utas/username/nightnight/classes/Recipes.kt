package au.edu.utas.username.nightnight.classes

import com.google.firebase.firestore.Exclude

class Recipes {
    @get:Exclude var id: String? = null

    var recipeName: String? = null
    var category: String? = null
    var recipeDes: String? = null
}