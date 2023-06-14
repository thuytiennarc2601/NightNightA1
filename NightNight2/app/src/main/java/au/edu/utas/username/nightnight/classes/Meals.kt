package au.edu.utas.username.nightnight.classes

import com.google.firebase.firestore.Exclude

class Meals {
    @get:Exclude var id: String? = null

    var startTime: String? = null
    var dish: Recipes? = null
    var mealNote: String? = null
}