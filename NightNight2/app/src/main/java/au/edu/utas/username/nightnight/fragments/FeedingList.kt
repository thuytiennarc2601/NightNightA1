package au.edu.utas.username.nightnight.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.username.nightnight.*
import au.edu.utas.username.nightnight.adapter.Dialogs
import au.edu.utas.username.nightnight.adapter.TimeDataSupporter
import au.edu.utas.username.nightnight.databinding.*
import com.google.firebase.firestore.Query
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
 * Use the [FeedingList.newInstance] factory method to
 * create an instance of this fragment.
 */
private val feedingList = mutableListOf<au.edu.utas.username.nightnight.classes.Feed>()
class FeedingList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var ui: FragmentFeedingListBinding

    val timeSupporter = TimeDataSupporter()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var feedingDate = dateFormat.format(Date())

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
        ui = FragmentFeedingListBinding.inflate(inflater, container, false)
        val view = ui.root

        ui.feedingDate.text = feedingDate

        //vertical list
        ui.feedingListHolder.layoutManager = LinearLayoutManager(requireContext())

        //getFeedingFromFirebase()

        ui.feedingDateContainer.setOnClickListener {
            Dialogs().openDatePicker(requireContext(), ui.feedingDate)
        }

        ui.feedingDate.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                feedingDate = p0.toString()
                getFeedingFromFirebase()
            }

        })

        ui.feedingSummary.setOnClickListener {
            val intent = Intent(requireContext(), ViewSummaries::class.java)
            intent.putExtra("currenList", "feeding")
            startActivity(intent)
        }

        return view
    }

    inner class FeedingHolder (var ui: MealListItemBinding): RecyclerView.ViewHolder(ui.root){}

    inner class FeedingAdapter (private var feeds: MutableList<au.edu.utas.username.nightnight.classes.Feed>) : RecyclerView.Adapter<FeedingHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedingHolder {
            val ui = MealListItemBinding.inflate(layoutInflater, parent, false)   //inflate a new row from the my_list_item.xml
            return FeedingHolder(ui)
        }

        override fun getItemCount(): Int {
            return feeds.size
        }

        override fun onBindViewHolder(holder: FeedingHolder, position: Int) {
            val meal = feeds[position]

            if(meal.type == "breastfeed")
            {
                holder.ui.mealIcon.setImageResource(R.drawable.breastfeeding)
                holder.ui.mealTime.text = meal.startTime
                holder.ui.mealSides.visibility = View.VISIBLE
                holder.ui.mealSides.text = getString(R.string.sides, meal.startSide, meal.endSide)
                holder.ui.mealDetails.text = getString(R.string.breasfeed_duration, timeSupporter.timeStringFromLong(meal.totalDuration, true), timeSupporter.timeStringFromLong(meal.leftDuration, true), timeSupporter.timeStringFromLong(meal.rightDuration, true))
                holder.ui.mealNote.text = meal.note
                holder.ui.mealEditButton.setOnClickListener {
                    val intent = Intent(requireContext(), AddBreastFeeding::class.java)
                    val bundle = Bundle()
                    bundle.putString("currentAction", "editBreast")
                    bundle.putString("feedID", meal.id)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
                holder.ui.mealDeleteButton.setOnClickListener {
                    val builder = AlertDialog.Builder(requireContext())
                    val displayer = LayoutInflater.from(requireContext()).inflate(R.layout.confirm_delete, null)
                    builder.setView(displayer)
                    builder.setCancelable(true)
                    val dialog = builder.create()
                    dialog.show()
                    val yesButton = displayer.findViewById<Button>(R.id.yesButton)
                    val noButton = displayer.findViewById<Button>(R.id.noButton)
                    yesButton.setOnClickListener {
                        val db = Firebase.firestore
                        db.collection("feed").document("${meal.id}")
                            .delete()
                            .addOnSuccessListener {
                                dialog.dismiss()
                                getFeedingFromFirebase()
                            }
                            .addOnFailureListener {
                                dialog.dismiss()
                                val builder2 = AlertDialog.Builder(requireContext())
                                val displayer2 = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_error, null)
                                val message = displayer2.findViewById<TextView>(R.id.errorText)
                                message.setText(R.string.cannot_delete)
                                builder2.setView(displayer2)
                                builder2.setCancelable(true)
                                builder2.create().show()
                            }
                    }
                    noButton.setOnClickListener {
                        dialog.dismiss()
                    }
                }
            }

            else if(meal.type == "bottlefeed")
            {
                holder.ui.mealIcon.setImageResource(R.drawable.milk_bottle)
                holder.ui.mealTime.text = meal.startTime
                holder.ui.mealDetails.text = getString(R.string.bottle_details, timeSupporter.timeStringFromLong(meal.totalDuration, true), meal.amount)
                holder.ui.mealNote.text = meal.note
                holder.ui.mealEditButton.setOnClickListener{
                    val intent = Intent(requireContext(), AddBottleFeeding::class.java)
                    val bundle = Bundle()
                    bundle.putString("currentAction", "editBottle")
                    bundle.putString("feedID", meal.id)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
                holder.ui.mealDeleteButton.setOnClickListener {
                    val builder = AlertDialog.Builder(requireContext())
                    val displayer = LayoutInflater.from(requireContext()).inflate(R.layout.confirm_delete, null)
                    builder.setView(displayer)
                    builder.setCancelable(true)
                    val dialog = builder.create()
                    dialog.show()
                    val yesButton = displayer.findViewById<Button>(R.id.yesButton)
                    val noButton = displayer.findViewById<Button>(R.id.noButton)
                    yesButton.setOnClickListener {
                        val db = Firebase.firestore
                        db.collection("feed").document("${meal.id}")
                            .delete()
                            .addOnSuccessListener {
                                dialog.dismiss()
                                getFeedingFromFirebase()
                            }
                            .addOnFailureListener {
                                dialog.dismiss()
                                val builder2 = AlertDialog.Builder(requireContext())
                                val displayer2 = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_error, null)
                                val message = displayer2.findViewById<TextView>(R.id.errorText)
                                message.setText(R.string.cannot_delete)
                                builder2.setView(displayer2)
                                builder2.setCancelable(true)
                                builder2.create().show()
                            }
                    }
                    noButton.setOnClickListener {
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getFeedingFromFirebase()
    }

    private fun getFeedingFromFirebase() {
        feedingList.clear()
        ui.feedingListHolder.adapter = FeedingAdapter(feeds = feedingList)
        val db = Firebase.firestore
        db.collection("feed")
            .orderBy("dateID", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result)
                {
                    val item = document.toObject<au.edu.utas.username.nightnight.classes.Feed>()
                    item.id = document.id
                    if(item.date == feedingDate)
                    {
                        feedingList.add(item)
                    }
                }

                (ui.feedingListHolder.adapter as FeedingList.FeedingAdapter).notifyItemRangeInserted(0, feedingList.size)
                if(feedingList.size != 0)
                {
                    ui.feedingNoRecord.visibility = View.GONE
                    ui.feedingListHolder.visibility = View.VISIBLE
                }
                else
                {
                    ui.feedingListHolder.visibility = View.GONE
                    ui.feedingNoRecord.visibility = View.VISIBLE
                }
                //Log.d(FIREBASE_TAG, "fragment nappy: ${nappyList.size}")
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedingList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedingList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}