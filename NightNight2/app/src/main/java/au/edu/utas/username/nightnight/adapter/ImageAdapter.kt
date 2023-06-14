package au.edu.utas.username.nightnight.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


var storage = Firebase.storage

class ImageAdapter {

    private fun createImageName(string1: String, string2: String): String
    {
        return string1 + string2
    }

    //upload a chosen image
    fun uploadImage(imageView: ImageView, storageFolder: String, string1: String, string2: String): UploadTask
    {
        val bitmap = (imageView.drawable as BitmapDrawable).getBitmap()
        //get a storage reference
        val storageRef = storage.reference
        //get a name for the baby photo
        val path = storageFolder + createImageName(string1, string2) + ".png"
        //get a reference to a image folder in Firebase
        val ref = storageRef.child(path)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        return ref.putBytes(data)
    }

    //download image from database
    fun downloadImage(imageURL: String): StorageReference
    {
        //get a storage reference
        val storageRef = storage.reference

        //get an image reference
        //Log.d(FIREBASE_TAG, "Path:" + imageURL)
        return storageRef.child(imageURL)
    }

    fun byteArrayToBitmap(data: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }
}