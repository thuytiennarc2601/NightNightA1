package au.edu.utas.username.nightnight

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import au.edu.utas.username.nightnight.adapter.Dialogs
import au.edu.utas.username.nightnight.adapter.ImageAdapter
import au.edu.utas.username.nightnight.classes.Baby
import au.edu.utas.username.nightnight.databinding.ActivityBabyProfileBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class BabyProfile : AppCompatActivity() {
    private lateinit var ui: ActivityBabyProfileBinding
    private var chosenImage:Uri? = null
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
                        ui.babyAvatar.setImageBitmap(chosenBitmap)
                    }
                    else
                    {
                        chosenBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, chosenImage)
                        ui.babyAvatar.setImageBitmap(chosenBitmap)
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityBabyProfileBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //get baby details from the database
        var baby = Baby()
        val db = Firebase.firestore
        val babyDetails = db.collection("baby")
        babyDetails
            .get()
            .addOnSuccessListener { result ->
                for(document in result)
                {
                    baby = document.toObject()
                    baby.id = document.id

                }

                val imageAdapter = ImageAdapter()
                val imageReference = imageAdapter.downloadImage("${baby.image}")
                val oneMEGABYTE: Long = 1024 * 1024 * 16
                imageReference.getBytes(oneMEGABYTE).addOnSuccessListener { bytes ->
                    ui.babyAvatar.setImageBitmap(imageAdapter.byteArrayToBitmap(bytes))
                }
                ui.babyNameField.setText(baby.babyName)

                ui.dob.text = baby.dateOfBirth

                if (baby.gender == "boy")
                {
                    setIconWhenBoyIsSelected()
                }
                else
                {
                    setIconWhenGirlIsSelected()
                }


                ui.boyIcon.setOnClickListener{
                    baby.gender = "boy"
                    setIconWhenBoyIsSelected()
                }

                ui.girlIcon.setOnClickListener{
                    baby.gender = "girl"
                    setIconWhenGirlIsSelected()
                }

                ui.buttonUpload.setOnClickListener{
                    chooseImage()
                }

                ui.dateContainer.setOnClickListener{
                    Dialogs().openDatePicker(this, ui.dob)
                }

                ui.saveBabyInfoBtn.setOnClickListener{
                    baby.babyName = ui.babyNameField.text.toString()
                    baby.image = "babyPhoto/${baby.id}${baby.babyName}.png"
                    baby.dateOfBirth = ui.dob.text.toString()
                    val uploadBabyPhotoTask = imageAdapter.uploadImage(ui.babyAvatar, "babyPhoto/", "${baby.id}", "${baby.babyName}")
                    val loadingDialog = Dialogs().showDialog(this, R.layout.loading, false)
                    loadingDialog.show()
                    uploadBabyPhotoTask.addOnCompleteListener{
                        babyDetails.document(baby.id!!)
                            .set(baby)
                            .addOnSuccessListener {
                                //Log.d(FIREBASE_TAG, "Successfully updated the baby's info ${baby.id}")
                                //go to main screen when data is stored
                                val mainScreen = Intent(this, MainActivity::class.java)
                                startActivity(mainScreen)
                                finish()
                                loadingDialog.dismiss()
                            }
                            .addOnFailureListener{
                                loadingDialog.dismiss()
                                val dialog = Dialogs().showDialog(this, R.layout.dialog_error, true)
                                dialog.show()
                            }
                    }
                    uploadBabyPhotoTask.addOnFailureListener{
                        loadingDialog.dismiss()
                        val dialog = Dialogs().showDialog(this, R.layout.dialog_error, true)
                        dialog.show()
                    }
                }
            }
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

    private fun setIconWhenGirlIsSelected()
    {
        ui.girlIcon.setBackgroundResource(R.drawable.rectangle_white_border)
        ui.girlIcon.setPadding(5)
        ui.boyIcon.setBackgroundColor(getColor(R.color.grey_primary))
        ui.boyIcon.setPadding(0)
    }

    private fun setIconWhenBoyIsSelected()
    {
        ui.boyIcon.setBackgroundResource(R.drawable.rectangle_white_border)
        ui.boyIcon.setPadding(5)
        ui.girlIcon.setBackgroundColor(getColor(R.color.grey_primary))
        ui.girlIcon.setPadding(0)
    }

}