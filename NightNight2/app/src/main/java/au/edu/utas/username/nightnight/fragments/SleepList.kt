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
import au.edu.utas.username.nightnight.AddSleep
import au.edu.utas.username.nightnight.R
import au.edu.utas.username.nightnight.ViewSummaries
import au.edu.utas.username.nightnight.adapter.Dialogs
import au.edu.utas.username.nightnight.adapter.TimeDataSupporter
import au.edu.utas.username.nightnight.databinding.FragmentSleepListBinding
import au.edu.utas.username.nightnight.databinding.SleepListItemBinding
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
 * Use the [SleepList.newInstance] factory method to
 * create an instance of this fragment.
 */
private val sleepList = mutableListOf<au.edu.utas.username.nightnight.classes.Sleep>()
class SleepList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var ui: FragmentSleepListBinding

    val timeSupporter = TimeDataSupporter()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var sleepDate = dateFormat.format(Date())

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
        ui = FragmentSleepListBinding.inflate(inflater, container, false)
        val view = ui.root

        ui.sleepDate.text = sleepDate

        //vertical list
        ui.sleepListHolder.layoutManager = LinearLayoutManager(requireContext())

        ui.sleepDateContainer.setOnClickListener {
            Dialogs().openDatePicker(requireContext(), ui.sleepDate)
        }

        ui.sleepDate.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                sleepDate = p0.toString()
                getSleepsFromFirebase()
            }

        })

        ui.sleepSummary.setOnClickListener {
            val list = Intent(requireContext(), ViewSummaries::class.java)
            list.putExtra("currentList", "sleep")
            startActivity(list)
        }

        return view
    }

    inner class SleepHolder (var ui: SleepListItemBinding): RecyclerView.ViewHolder(ui.root){}

    inner class SleepAdapter (private var sleeps: MutableList<au.edu.utas.username.nightnight.classes.Sleep>) : RecyclerView.Adapter<SleepHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepHolder {
            val ui = SleepListItemBinding.inflate(layoutInflater, parent, false)   //inflate a new row from the my_list_item.xml
            return SleepHolder(ui)
        }

        override fun getItemCount(): Int {
            return sleeps.size
        }

        override fun onBindViewHolder(holder: SleepHolder, position: Int) {
            val sleep = sleeps[position]
            holder.ui.sleepTime.text = sleep.startTime
            holder.ui.sleepDuration.text = getString(R.string.sleep_duration, timeSupporter.timeStringFromLong(sleep.duration, true))
            holder.ui.sleepNote.text = sleep.note
            holder.ui.sleepEditButton.setOnClickListener {
                val intent = Intent(requireContext(), AddSleep::class.java)
                val bundle = Bundle()
                bundle.putString("currentAction", "editSleep")
                bundle.putString("sleepID", sleep.id)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            holder.ui.sleepDeleteButton.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                val box = LayoutInflater.from(requireContext()).inflate(R.layout.confirm_delete, null)
                val yesButton = box.findViewById<Button>(R.id.yesButton)
                val noButton = box.findViewById<Button>(R.id.noButton)
                builder.setView(box)
                builder.setCancelable(true)
                val dialog = builder.create()
                yesButton.setOnClickListener {
                    val db = Firebase.firestore
                    db.collection("sleep").document("${sleep.id}")
                        .delete()
                        .addOnSuccessListener {
                            dialog.dismiss()
                            getSleepsFromFirebase()
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
                dialog.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getSleepsFromFirebase()
    }

    private fun getSleepsFromFirebase() {
        sleepList.clear()
        ui.sleepListHolder.adapter = SleepAdapter(sleeps = sleepList)
        val db = Firebase.firestore
        db.collection("sleep")
            .orderBy("dateID", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result)
                {
                    val item = document.toObject<au.edu.utas.username.nightnight.classes.Sleep>()
                    item.id = document.id
                    if(item.date == sleepDate)
                    {
                        sleepList.add(item)
                    }
                }

                (ui.sleepListHolder.adapter as SleepList.SleepAdapter).notifyItemRangeInserted(0, sleepList.size)
                if(sleepList.size != 0)
                {
                    ui.sleepNoRecord.visibility = View.GONE
                    ui.sleepListHolder.visibility = View.VISIBLE
                }
                else
                {
                    ui.sleepListHolder.visibility = View.GONE
                    ui.sleepNoRecord.visibility = View.VISIBLE
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
         * @return A new instance of fragment SleepList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SleepList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}