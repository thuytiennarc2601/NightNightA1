package au.edu.utas.username.nightnight.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import au.edu.utas.username.nightnight.*
import au.edu.utas.username.nightnight.adapter.DataStorer
import au.edu.utas.username.nightnight.databinding.FragmentFeedingBinding
import com.google.android.material.appbar.MaterialToolbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Feeding.newInstance] factory method to
 * create an instance of this fragment.
 */

class Feeding : Fragment() {

    interface MyInterface {
        fun onValuePassed(activity: String?, timerCounting: Boolean, secondTimerCounting: Boolean,
                    startTime: String?, stopTime: String?, secondStartTime: String?, secondStopTime: String?)
    }
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var ui: FragmentFeedingBinding
    private lateinit var dataStorer: DataStorer
    private lateinit var myInterface: MyInterface
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == android.app.Activity.RESULT_OK)
        {
            val bundle = it.data?.extras
            val activity = bundle?.getString("currentActivity")
            val timerCounting = bundle!!.getBoolean("timerCounting", false)
            val secondTimerCounting = bundle.getBoolean("secondTimerCounting", false)
            val startTime = bundle.getString("startTime")
            val stopTime = bundle.getString("stopTime")
            val secondStartTime = bundle.getString("secondStartTime")
            val secondStopTime = bundle.getString("secondStopTime")
            myInterface.onValuePassed(activity, timerCounting, secondTimerCounting, startTime, stopTime, secondStartTime, secondStopTime)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        ui = FragmentFeedingBinding.inflate(inflater, container, false)
        val view = ui.root

        dataStorer = DataStorer(requireContext().applicationContext)

        val builder = AlertDialog.Builder(requireContext())
        val dialogError = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_error, null)
        val message = dialogError.findViewById<TextView>(R.id.errorText)
        val icon = dialogError.findViewById<ImageView>(R.id.errorImage)
        val dialogContainer = dialogError.findViewById<MaterialToolbar>(R.id.errorContainer)
        message.setText(R.string.current_event)
        icon.setImageResource(R.drawable.playing)
        dialogContainer.setBackgroundResource(R.drawable.rectangle_grey_border)
        builder.setView(dialogError)
        builder.setCancelable(true)
        val dialog = builder.create()

        ui.breastfeedBar.setOnClickListener {
            if(dataStorer.currentActivity() == "none" || dataStorer.currentActivity() == "feeding")
            {
                val breastFeeding = Intent(activity, AddBreastFeeding::class.java)
                val bundle = Bundle()
                bundle.putString("currentAction", "addBreast")
                bundle.putString("feedID", "newFeed")
                breastFeeding.putExtras(bundle)
                launcher.launch(breastFeeding)
            }
            else
            {
                dialog.show()
            }
        }

        ui.bottlefeedBar.setOnClickListener {
            if(dataStorer.currentActivity() == "none" || dataStorer.currentActivity() == "bottle")
            {
                val bottleFeeding = Intent(activity, AddBottleFeeding::class.java)
                val bundle = Bundle()
                bundle.putString("currentAction", "addBottle")
                bundle.putString("feedID", "newFeed")
                bottleFeeding.putExtras(bundle)
                launcher.launch(bottleFeeding)
            }
            else
            {
                dialog.show()
            }
        }

        ui.babymealBar.setOnClickListener {
            //Dialogs().showDialog(requireContext(), R.layout.baby_meal_options, true).show()
            val builder2 = AlertDialog.Builder(requireContext())
            val optionDialog = LayoutInflater.from(requireContext()).inflate(R.layout.baby_meal_options, null)
            builder2.setView(optionDialog)
            builder2.setCancelable(true)
            val optionBox = builder.create()
            optionBox.show()
            val addButton = optionDialog.findViewById<MaterialToolbar>(R.id.bmAddOption)
            addButton.setOnClickListener {
                val intent = Intent(requireContext(), AddBabymeal::class.java)
                startActivity(intent)
            }
            val recipeButton = optionDialog.findViewById<MaterialToolbar>(R.id.bmRecipeOption)
            recipeButton.setOnClickListener {
                val intent = Intent(requireContext(), RecipeList::class.java)
                startActivity(intent)
            }
            val closeButton = optionDialog.findViewById<Button>(R.id.bmCloseButton)
            closeButton.setOnClickListener {
                optionBox.dismiss()
            }
        }

        ui.feedingListBar.setOnClickListener{
            val intent = Intent(requireContext(), ViewLists::class.java)
            intent.putExtra("currenList", "feeding")
            startActivity(intent)
        }

        ui.feedingSummaryBar.setOnClickListener {
            val intent = Intent(requireContext(), ViewSummaries::class.java)
            intent.putExtra("currenList", "feeding")
            startActivity(intent)
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            myInterface = context as MyInterface
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement MyInterface")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Feeding.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Feeding().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}