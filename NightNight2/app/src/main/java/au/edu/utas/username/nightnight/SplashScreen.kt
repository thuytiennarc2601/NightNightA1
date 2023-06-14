package au.edu.utas.username.nightnight

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.edu.utas.username.nightnight.classes.Baby
import au.edu.utas.username.nightnight.databinding.ActivityLoadingSceneBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

const val FIREBASE_TAG = "FirebaseLogging"

class SplashScreen : AppCompatActivity() {
    private lateinit var ui: ActivityLoadingSceneBinding

    private var baby: Baby? = null

    private var defaultName = "Gon"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityLoadingSceneBinding.inflate(layoutInflater)
        setContentView(ui.root)
        ui.appIcon.alpha = 0f
        ui.appIcon.animate().setDuration(1500).alpha(1f).withEndAction {
            val db = Firebase.firestore
            //Log.d(FIREBASE_TAG, "Firebase connect: ${Firebase.app}")
            val babyDetails = db.collection("baby").document("babyPhoto")
            babyDetails
                .get()
                .addOnCompleteListener { result ->
                    //Log.d(FIREBASE_TAG, "--- Baby ---")
                    if(result.isSuccessful)
                    {
                        if(!result.result.exists())
                        {
                            val babyInfoScreen = Intent(this, BabyProfile::class.java)
                            startActivity(babyInfoScreen)
                        }
                        else
                        {
                            val document = result.result
                            baby = document.toObject<Baby>()

                            if (baby?.babyName.toString() == defaultName)
                            {
                                val babyInfoScreen = Intent(this, BabyProfile::class.java)
                                startActivity(babyInfoScreen)
                            }
                            else
                            {
                                val mainScreen = Intent(this, MainActivity::class.java)
                                startActivity(mainScreen)
                            }

                        }
                    }
                }

        }
    }
}