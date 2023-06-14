package au.edu.utas.username.nightnight.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import au.edu.utas.username.nightnight.AddSleep
import au.edu.utas.username.nightnight.R
import au.edu.utas.username.nightnight.ViewLists
import au.edu.utas.username.nightnight.ViewSummaries
import au.edu.utas.username.nightnight.adapter.DataStorer
import au.edu.utas.username.nightnight.databinding.FragmentSleepBinding
import com.google.android.material.appbar.MaterialToolbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Sleep.newInstance] factory method to
 * create an instance of this fragment.
 */
class Sleep : Fragment() {

    interface MyInterface {
        fun onValuePassed(activity: String?, timerCounting: Boolean, secondTimerCounting: Boolean,
                          startTime: String?, stopTime: String?, secondStartTime: String?, secondStopTime: String?)
    }

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var ui: FragmentSleepBinding? = null
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
        val ui = FragmentSleepBinding.inflate(inflater, container, false)
        val view = ui.root

        dataStorer = DataStorer(requireContext().applicationContext)

        ui.addSleepBar.setOnClickListener {
            if(dataStorer.currentActivity() == "none" || dataStorer.currentActivity() == "sleeping") {
                val addSleep = Intent(activity, AddSleep::class.java)
                val bundle = Bundle()
                bundle.putString("currentAction", "addSleep")
                bundle.putString("sleepID", "newSleep")
                addSleep.putExtras(bundle)
                launcher.launch(addSleep)
            }
            else
            {
                val builder = AlertDialog.Builder(requireContext())
                val dialogError = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_error, null)
                val message = dialogError.findViewById<TextView>(R.id.errorText)
                val icon = dialogError.findViewById<ImageView>(R.id.errorImage)
                val dialogContainer = dialogError.findViewById<MaterialToolbar>(R.id.errorContainer)
                message.text = getString(R.string.current_event)
                icon.setImageResource(R.drawable.playing)
                dialogContainer.setBackgroundResource(R.drawable.rectangle_grey_border)
                builder.setView(dialogError)
                builder.setCancelable(true)
                builder.create().show()
            }
        }

        ui.sleepListBar.setOnClickListener {
            val list = Intent(requireContext(), ViewLists::class.java)
            list.putExtra("currentList", "sleep")
            startActivity(list)
        }

        ui.sleepSummaryBar.setOnClickListener {
            val list = Intent(requireContext(), ViewSummaries::class.java)
            list.putExtra("currentList", "sleep")
            startActivity(list)
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

    override fun onDestroyView() {
        super.onDestroyView()
        ui = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Sleep.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Sleep().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}