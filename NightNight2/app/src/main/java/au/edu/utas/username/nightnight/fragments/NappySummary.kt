package au.edu.utas.username.nightnight.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import au.edu.utas.username.nightnight.R
import au.edu.utas.username.nightnight.adapter.Dialogs
import au.edu.utas.username.nightnight.databinding.FragmentNappySummaryBinding
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
 * Use the [NappySummary.newInstance] factory method to
 * create an instance of this fragment.
 */
class NappySummary : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var ui: FragmentNappySummaryBinding
    //get Firebase
    private val db = Firebase.firestore
    private val nappies = db.collection("nappy")
    private var nappy = au.edu.utas.username.nightnight.classes.Nappy()
    private var totalWet = 0
    private var totalDry = 0
    private var totalMixed = 0
    private var total = 0
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
        ui = FragmentNappySummaryBinding.inflate(inflater, container, false)
        val view = ui.root

        ui.totalNappies.text = getString(R.string.total_nappies, total)
        ui.totalWet.text = totalWet.toString()
        ui.totalDry.text = totalDry.toString()
        ui.totalMixed.text = totalMixed.toString()

        ui.nappyDate.text = nappyDate

        ui.sNappyDate.setOnClickListener {
            Dialogs().openDatePicker(requireContext(), ui.nappyDate)
        }
        ui.nappyDate.addTextChangedListener {
            nappyDate = it.toString()
            getSummary()
        }

       getSummary()
        return view
    }

    private fun getSummary()
    {
        totalWet = 0
        totalDry = 0
        totalMixed = 0
        total = 0
        nappies.get()
            .addOnCompleteListener { result ->
                if(result.isSuccessful)
                {
                    if(!result.result.isEmpty)
                    {
                        for(document in result.result) {
                            nappy = document.toObject()
                            nappy.id = document.id
                            if (nappy.date == nappyDate) {
                                when (nappy.type) {
                                    "pee" -> {
                                        totalWet += 1
                                    }
                                    "poo" -> {
                                        totalDry += 1
                                    }
                                    "both" -> {
                                        totalMixed += 1
                                    }
                                }
                            }
                        }
                        total = totalWet + totalDry + totalMixed
                        if(total != 0)
                        {
                            ui.totalNappies.text = getString(R.string.total_nappies, total)
                            ui.totalWet.text = totalWet.toString()
                            ui.totalDry.text = totalDry.toString()
                            ui.totalMixed.text = (totalMixed.toString())
                        }
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
         * @return A new instance of fragment NappySummary.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NappySummary().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}