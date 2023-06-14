package au.edu.utas.username.nightnight

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import au.edu.utas.username.nightnight.adapter.Dialogs
import au.edu.utas.username.nightnight.adapter.ImageAdapter
import au.edu.utas.username.nightnight.classes.Nappy
import au.edu.utas.username.nightnight.databinding.ActivityAddNappyBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddNappy : AppCompatActivity() {

    private lateinit var ui: ActivityAddNappyBinding
    private var chosenImage: Uri? = null
    private var chosenBitmap: Bitmap? = null
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == android.app.Activity.RESULT_OK)
        {
            if(it.data != null)
            {
                chosenImage = it.data!!.data
                if(chosenImage != null)
                {
                    if(Build.VERSION.SDK_INT >= 28)
                    {
                        val source = ImageDecoder.createSource(this.contentResolver, chosenImage!!)
                        chosenBitmap = ImageDecoder.decodeBitmap(source)
                        ui.nImage.setImageBitmap(chosenBitmap)
                    }
                    else
                    {
                        chosenBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, chosenImage)
                        ui.nImage.setImageBitmap(chosenBitmap)
                    }
                }
            }
        }
    }

    private var nappy = Nappy()
    private var editedNappy: Nappy? = null
    val db = Firebase.firestore
    private val nappies = db.collection("nappy")
    private val imageAdapter = ImageAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddNappyBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val bundle = intent.extras
        //the activity can be twisted between 'adding' and 'editing' mode
        val isEditingNappy = (bundle?.getString("currentAction") == "editNappy") && (bundle.getString("nappyID") != "newNappy")
        val isAddingNappy = (bundle?.getString("currentAction") == "addNappy") && (bundle.getString("nappyID") == "newNappy")
        if(isEditingNappy)
        {
            nappies.document("${bundle?.getString("nappyID")}")
                .get()
                .addOnCompleteListener { result ->
                    if(result.isSuccessful)
                    {
                        if(result.result.exists())
                        {
                            val document = result.result
                            editedNappy = document.toObject<Nappy>()
                            editedNappy?.id = document.id

                            when(editedNappy?.type)
                            {
                                "pee"->{
                                    ui.nPee.setBackgroundResource(R.drawable.rectangle_grey_border_blue_solid)
                                    nappy.type = "pee"
                                }
                                "poo"->{
                                    ui.nPoo.setBackgroundResource(R.drawable.rectangle_grey_border_blue_solid)
                                    nappy.type = "poo"
                                }
                                "both"->{
                                    ui.nBoth.setBackgroundResource(R.drawable.rectangle_grey_border_blue_solid)
                                    nappy.type = "both"
                                }
                            }
                            ui.nTimePicker.text = editedNappy?.changingTime
                            val imageReference = imageAdapter.downloadImage("${editedNappy?.image}")
                            val oneMEGABYTE: Long = 1024 * 1024 * 16
                            imageReference.getBytes(oneMEGABYTE).addOnSuccessListener { bytes ->
                                ui.nImage.setImageBitmap(imageAdapter.byteArrayToBitmap(bytes))
                            }
                            ui.nNoteTxt.setText(editedNappy?.note)
                        }
                    }
                }
        }

        else if(isAddingNappy)
        {
            //set option 'Pee' is chosen
            nappy.type = "pee"
            ui.nPee.setBackgroundResource(R.drawable.rectangle_grey_border_blue_solid)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            ui.nTimePicker.text = timeFormat.format(Date())
        }

        ui.nBackButton.setOnClickListener {
            finish()
            if(isEditingNappy)
            {
                clearData()
            }
        }

        ui.nStartTimePicker.setOnClickListener {
            Dialogs().openTimePicker(this, ui.nTimePicker)
        }

        ui.nPee.setOnClickListener {
            nappy.type = "pee"
            setTypeButtons(ui.nPee, ui.nPoo, ui.nBoth)
        }

        ui.nPoo.setOnClickListener {
            nappy.type = "poo"
            setTypeButtons(ui.nPoo, ui.nPee, ui.nBoth)
        }

        ui.nBoth.setOnClickListener {
            nappy.type = "both"
            setTypeButtons(ui.nBoth, ui.nPee, ui.nPoo)
        }

        ui.nImage.setOnClickListener {
            chooseImage()
        }

        ui.nUploadButton.setOnClickListener {
            chooseImage()
        }

        ui.nRemoveButton.setOnClickListener {
            ui.nImage.setImageResource(R.drawable.poop)
        }

        ui.nClearButton.setOnClickListener {
            if(isAddingNappy) {
                clearData()
            }
            else if(isEditingNappy){
                clearData()
                val builder = AlertDialog.Builder(this)
                val displayer = LayoutInflater.from(this).inflate(R.layout.confirm_delete, null)
                builder.setView(displayer)
                builder.setCancelable(true)
                val dialog = builder.create()
                dialog.show()
                val yesButton = displayer.findViewById<Button>(R.id.yesButton)
                val noButton = displayer.findViewById<Button>(R.id.noButton)
                yesButton.setOnClickListener {
                    val db = Firebase.firestore
                    db.collection("nappy").document("${bundle?.getString("nappyID")}")
                        .delete()
                        .addOnSuccessListener {
                            dialog.dismiss()
                            finish()
                        }
                        .addOnFailureListener {
                            dialog.dismiss()
                            val builder2 = AlertDialog.Builder(this)
                            val displayer2 = LayoutInflater.from(this).inflate(R.layout.dialog_error, null)
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

        ui.nSaveButton.setOnClickListener {
            if(isAddingNappy)
            {
                val dataFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val reverseDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                nappy.id = "${dataFormat.format(Date())} ${ui.nTimePicker.text}"
                nappy.date =  reverseDateFormat.format(Date())
                nappy.dateID = nappy.id
            }
            else
            {
                nappy.id = editedNappy?.id
                nappy.date = editedNappy?.date
                nappy.dateID = editedNappy?.id?.substring(0, 10) + " ${ui.nTimePicker.text}"
            }
            nappy.changingTime = ui.nTimePicker.text.toString()
            nappy.image = "nappyPhoto/${nappy.id}${nappy.type}.png"
            nappy.note = ui.nNoteTxt.text.toString()

            val imageAdapter = ImageAdapter()
            val uploadImageTask = imageAdapter.uploadImage(ui.nImage, "nappyPhoto/", "${nappy.id}", "${nappy.type}")
            val loadingDialog = Dialogs().showDialog(this, R.layout.loading, false)
            loadingDialog.show()
            uploadImageTask.addOnCompleteListener{
                nappies.document("${nappy.id}")
                    .set(nappy)
                    .addOnSuccessListener {
                        //TODO: go to the nappy list when data is stored
                        loadingDialog.dismiss()
                        clearData()
                        if(isAddingNappy) {
                            val intent = Intent(this, ViewLists::class.java)
                            intent.putExtra("currentList", "nappy")
                            startActivity(intent)
                        }
                        finish()
                    }
                    .addOnFailureListener{
                        loadingDialog.dismiss()
                        val dialog = Dialogs().showDialog(this, R.layout.dialog_error, true)
                        dialog.show()
                    }
            }
            uploadImageTask.addOnFailureListener{
                loadingDialog.dismiss()
                val dialog = Dialogs().showDialog(this, R.layout.dialog_error, true)
                dialog.show()
            }

        }

    }

    //set UIs when a nappy condition is chosen
    private fun setTypeButtons (chosen: MaterialToolbar, second: MaterialToolbar, third: MaterialToolbar)
    {
        chosen.setBackgroundResource(R.drawable.rectangle_grey_border_blue_solid)
        second.setBackgroundResource(R.drawable.rectangle_grey_border)
        third.setBackgroundResource(R.drawable.rectangle_grey_border)
    }

    private fun clearData()
    {
        nappy.clearData()
        ui.nTimePicker.setText(R.string.time_stamp)
        setTypeButtons(ui.nPee, ui.nPoo, ui.nBoth)
        ui.nImage.setImageResource(R.drawable.poop)
        ui.nNoteTxt.setText(R.string.note_content)
    }

    //check if a permission to access the device gallery is granted or not
    private fun chooseImage(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
        else
        {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcher.launch(galleryIntent)
        }
    }

    //if not, request permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                launcher.launch(galleryIntent)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}