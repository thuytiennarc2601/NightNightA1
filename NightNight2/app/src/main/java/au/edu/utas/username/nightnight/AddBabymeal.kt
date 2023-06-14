package au.edu.utas.username.nightnight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.edu.utas.username.nightnight.databinding.ActivityAddBabymealBinding

class AddBabymeal : AppCompatActivity() {

    private lateinit var ui: ActivityAddBabymealBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = ActivityAddBabymealBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.bmBackButton.setOnClickListener {
            finish()
        }

    }
}