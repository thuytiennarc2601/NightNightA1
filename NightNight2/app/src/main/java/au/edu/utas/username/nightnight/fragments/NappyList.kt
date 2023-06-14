package au.edu.utas.username.nightnight.fragments

import android.annotation.SuppressLint
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
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.username.nightnight.AddNappy
import au.edu.utas.username.nightnight.R
import au.edu.utas.username.nightnight.ViewLists
import au.edu.utas.username.nightnight.ViewSummaries
import au.edu.utas.username.nightnight.adapter.Dialogs
import au.edu.utas.username.nightnight.adapter.ImageAdapter
import au.edu.utas.username.nightnight.databinding.FragmentNappyListBinding
import au.edu.utas.username.nightnight.databinding.NappyListItemBinding
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
 * Use the [NappyList] factory method to
 * create an instance of this fragment.
 */
private val nappyList = mutableListOf<au.edu.utas.username.nightnight.classes.Nappy>()
class NappyList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var ui: FragmentNappyListBinding

    private val imageAdapter = ImageAdapter()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var nappyDate = dateFormat.format(Date())
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
        ui = FragmentNappyListBinding.inflate(inflater, container, false)
        val view = ui.root

        ui.nappyDate.text = nappyDate

        //vertical list
        ui.nappyListHolder.layoutManager = LinearLayoutManager(requireContext())

        // Retrieve the argument value
        //getNappiesFromFirebase()

        ui.nappyDateContainer.setOnClickListener {
            Dialogs().openDatePicker(requireContext(), ui.nappyDate)
        }

        ui.nappyDate.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                nappyDate = p0.toString()
                getNappiesFromFirebase()
            }

        })

        ui.nappySummary.setOnClickListener {
            val intent = Intent(requireContext(), ViewSummaries::class.java)
            intent.putExtra("currentList", "nappy")
            startActivity(intent)
        }

        return view
    }

    inner class NappyHolder (var ui: NappyListItemBinding): RecyclerView.ViewHolder(ui.root){}

    inner class NappyAdapter (private var nappies: MutableList<au.edu.utas.username.nightnight.classes.Nappy>) : RecyclerView.Adapter<NappyHolder>()
    {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NappyHolder {
            val ui = NappyListItemBinding.inflate(layoutInflater, parent, false)   //inflate a new row from the my_list_item.xml
            return NappyHolder(ui)
        }

        override fun getItemCount(): Int {
            return nappies.size
        }

        @SuppressLint("MissingInflatedId")
        override fun onBindViewHolder(holder: NappyHolder, position: Int) {
            val nappy = nappies[position]
            holder.ui.nappyTime.text = nappy.changingTime
            holder.ui.nappyNote.text = nappy.note
            when(nappy.type) {
                "pee" -> { holder.ui.nappyTypeIcon.setImageResource(R.drawable.pee) }
                "poo" -> { holder.ui.nappyTypeIcon.setImageResource(R.drawable.poo) }
                "both" -> { holder.ui.nappyTypeIcon.setImageResource(R.drawable.both) }
            }
            val imageReference = imageAdapter.downloadImage("${nappy.image}")
            val oneMEGABYTE: Long = 1024 * 1024 * 16
            holder.ui.nappyPhoto.setOnClickListener {
                val loading = Dialogs().showDialog(requireContext(), R.layout.loading, false)
                loading.show()
                imageReference.getBytes(oneMEGABYTE).addOnSuccessListener { bytes ->
                    val builder = AlertDialog.Builder(requireContext())
                    val displayer = LayoutInflater.from(requireContext()).inflate(R.layout.image_displayer, null)
                    val imageHolder = displayer.findViewById<ImageView>(R.id.imageHolder)
                    imageHolder.setImageBitmap(imageAdapter.byteArrayToBitmap(bytes))
                    builder.setView(displayer)
                    builder.setCancelable(true)
                    loading.dismiss()
                    builder.create().show()
                }
            }

            holder.ui.nappyEditButton.setOnClickListener {
                val intent = Intent(requireContext(), AddNappy::class.java)
                val bundle = Bundle()
                bundle.putString("currentAction", "editNappy")
                bundle.putString("nappyID", nappy.id)
                intent.putExtras(bundle)
                startActivity(intent)
            }

            holder.ui.nappyDeleteButton.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                val box = LayoutInflater.from(requireContext()).inflate(R.layout.confirm_delete, null)
                val yesButton = box.findViewById<Button>(R.id.yesButton)
                val noButton = box.findViewById<Button>(R.id.noButton)
                builder.setView(box)
                builder.setCancelable(true)
                val dialog = builder.create()
                yesButton.setOnClickListener {
                    val db = Firebase.firestore
                    db.collection("nappy").document("${nappy.id}")
                        .delete()
                        .addOnSuccessListener {
                            dialog.dismiss()
                            getNappiesFromFirebase()
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
        getNappiesFromFirebase()
    }

    private fun getNappiesFromFirebase()
    {
        nappyList.clear()
        ui.nappyListHolder.adapter = NappyAdapter(nappies = nappyList)
        val db = Firebase.firestore
        db.collection("nappy")
            .orderBy("dateID", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result)
                {
                    val item = document.toObject<au.edu.utas.username.nightnight.classes.Nappy>()
                    item.id = document.id
                    if(item.date == nappyDate)
                    {
                        nappyList.add(item)
                    }
                }

                (ui.nappyListHolder.adapter as NappyAdapter).notifyItemRangeInserted(0, nappyList.size)
                if(nappyList.size != 0)
                {
                    ui.nappyNoRecord.visibility = View.GONE
                    ui.nappyListHolder.visibility = View.VISIBLE
                }
                else
                {
                    ui.nappyListHolder.visibility = View.GONE
                    ui.nappyNoRecord.visibility = View.VISIBLE
                }
            }
    }
}