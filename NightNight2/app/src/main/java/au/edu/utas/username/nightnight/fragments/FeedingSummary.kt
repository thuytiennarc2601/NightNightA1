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
import au.edu.utas.username.nightnight.classes.Feed
import au.edu.utas.username.nightnight.databinding.FragmentFeedingSummaryBinding
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
 * Use the [FeedingSummary.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedingSummary : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var ui: FragmentFeedingSummaryBinding
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var feedingDate = dateFormat.format(Date())
    private var totalBreastfeed = 0
    private var totalLeftDuration = 0L
    private var totalRightDuration = 0L
    private var totalBreastFeedDuration = 0L
    private var totalBottlefeed = 0
    private var totalAmount = 0.0f
    private var totalBottlefeedDuration = 0L
    private var feed = Feed()
    private val timeSupporter = TimeDataSupporter()
    private val db = Firebase.firestore
    private val feeds = db.collection("feed")

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
        ui = FragmentFeedingSummaryBinding.inflate(inflater, container, false)
        val view = ui.root

        ui.feedingSDate.text = feedingDate

        ui.totalBreastfeed.text = getString(R.string.total_breastfeed, totalBreastfeed)
        ui.totalLeft.text = timeSupporter.timeStringFromLong(totalLeftDuration, true)
        ui.totalRight.text = timeSupporter.timeStringFromLong(totalRightDuration, true)
        ui.totalBreastfeedTime.text = timeSupporter.timeStringFromLong(totalBreastFeedDuration, true)
        ui.totalBottlefeed.text = getString(R.string.total_bottlefeed, totalBottlefeed)
        ui.totalMilkAmount.text = getString(R.string.total_amount, totalAmount)
        ui.totalBottlefeedTime.text = timeSupporter.timeStringFromLong(totalBottlefeedDuration, true)

        ui.feedingSDateCon.setOnClickListener {
            Dialogs().openDatePicker(requireContext(), ui.feedingSDate)
        }
        ui.feedingSDate.addTextChangedListener {
            feedingDate = it.toString()
            getSummary()
        }

        getSummary()

        return view
    }

    private fun getSummary()
    {
        totalBreastfeed = 0
        totalLeftDuration = 0L
        totalRightDuration = 0L
        totalBreastFeedDuration = 0L
        totalBottlefeed = 0
        totalAmount = 0.0f
        totalBottlefeedDuration = 0L

        feeds
            .get()
            .addOnCompleteListener { result ->
                if(result.isSuccessful)
                {
                    if(!result.result.isEmpty)
                    {
                        for(document in result.result)
                        {
                            feed = document.toObject()
                            if(feed.date == feedingDate)
                            {
                                when (feed.type)
                                {
                                    "breastfeed"->{
                                        totalLeftDuration += feed.leftDuration
                                        totalRightDuration += feed.rightDuration
                                        totalBreastFeedDuration += feed.totalDuration
                                        totalBreastfeed += 1
                                    }
                                    "bottlefeed"->{
                                        totalBottlefeed += 1
                                        totalBottlefeedDuration += feed.totalDuration
                                        totalAmount += feed.amount
                                    }
                                }
                            }
                        }
                        ui.totalBreastfeed.text = getString(R.string.total_breastfeed, totalBreastfeed)
                        ui.totalLeft.text = timeSupporter.timeStringFromLong(totalLeftDuration, true)
                        ui.totalRight.text = timeSupporter.timeStringFromLong(totalRightDuration, true)
                        ui.totalBreastfeedTime.text = timeSupporter.timeStringFromLong(totalBreastFeedDuration, true)
                        ui.totalBottlefeed.text = getString(R.string.total_bottlefeed, totalBottlefeed)
                        ui.totalMilkAmount.text = getString(R.string.total_amount, totalAmount)
                        ui.totalBottlefeedTime.text = timeSupporter.timeStringFromLong(totalBottlefeedDuration, true)
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
         * @return A new instance of fragment FeedingSummary.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedingSummary().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}