package au.edu.utas.username.nightnight.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import au.edu.utas.username.nightnight.R
import au.edu.utas.username.nightnight.adapter.Dialogs
import au.edu.utas.username.nightnight.adapter.TimeDataSupporter
import au.edu.utas.username.nightnight.databinding.FragmentSleepSummaryBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SleepSummary.newInstance] factory method to
 * create an instance of this fragment.
 */
class SleepSummary : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var ui: FragmentSleepSummaryBinding
    private val timeSupporter = TimeDataSupporter()
    private var sleep = au.edu.utas.username.nightnight.classes.Sleep()
    private val db = Firebase.firestore
    private val sleeps = db.collection("sleep")
    private var totalSleepTimes = 0
    private var totalSleepDuration = 0L
    private val dateFormate  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var sleepDate = dateFormate.format(Date())

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
        ui = FragmentSleepSummaryBinding.inflate(inflater, container, false)
        val view = ui.root

        ui.sumSleepDate.text = sleepDate
        ui.totalSleepTimes.text = getString(R.string.total_sleep_time, totalSleepTimes)
        ui.totalSleepDuration.text = timeSupporter.timeStringFromLong(totalSleepDuration, true)

        ui.sSleepDate.setOnClickListener {
            Dialogs().openDatePicker(requireContext(), ui.sumSleepDate)
        }

        ui.sumSleepDate.addTextChangedListener {
            sleepDate = it.toString()
            getSummary()
        }

        getSummary()
        return view
    }

    private fun getSummary()
    {
        totalSleepTimes = 0
        totalSleepDuration = 0L
        sleeps
            .get()
            .addOnCompleteListener { result ->
                if(result.isSuccessful)
                {
                    if(!result.result.isEmpty)
                    {
                        for(document in result.result)
                        {
                            sleep = document.toObject()
                            if(sleep.date == sleepDate)
                            {
                                totalSleepTimes += 1
                                totalSleepDuration += sleep.duration
                            }
                        }
                        ui.totalSleepTimes.text = getString(R.string.total_sleep_time, totalSleepTimes)
                        ui.totalSleepDuration.text = timeSupporter.timeStringFromLong(totalSleepDuration, true)
                    }
                }
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SleepSummary.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SleepSummary().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}