package au.edu.utas.username.nightnight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.edu.utas.username.nightnight.databinding.ActivityAddEditRecipeBinding

class AddEditRecipe : AppCompatActivity() {

    private lateinit var ui: ActivityAddEditRecipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddEditRecipeBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.rAddBackButton.setOnClickListener {
            finish()
        }
    }
}