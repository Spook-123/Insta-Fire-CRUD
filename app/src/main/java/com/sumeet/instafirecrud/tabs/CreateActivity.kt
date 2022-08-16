package com.sumeet.instafirecrud.tabs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sumeet.instafirecrud.MainActivity
import com.sumeet.instafirecrud.R
import com.sumeet.instafirecrud.daos.PostDao
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_create.*

class CreateActivity : AppCompatActivity() {


    companion object {
        private const val PICK_PHOTO_CODE = 1234
        private const val TAG = "CreateActivity"
    }
    private var photoUri: Uri? = null
    private lateinit var storageReference: StorageReference
    private lateinit var postDao: PostDao
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)


        proBar.visibility = View.GONE
        postDao = PostDao()

        auth = Firebase.auth
        storageReference = FirebaseStorage.getInstance().reference


        btnChooseImage.setOnClickListener {
            val imagePickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerIntent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg","image/png","image/jpg")
            imagePickerIntent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes)
            imagePickerIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            if(imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }

        btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }
    }

    private fun handleSubmitButtonClick() {
        if(photoUri == null) {
            Toast.makeText(this,"No Photo Selected", Toast.LENGTH_SHORT).show()
            return
        }
        if(etDescription.text.isBlank()) {
            Toast.makeText(this,"Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if(auth.currentUser == null) {
            Toast.makeText(this,"Invalid Sign In", Toast.LENGTH_SHORT).show()
            return
        }
        btnSubmit.isEnabled = false
        proBar.visibility = View.VISIBLE
        val photoUploadUri = photoUri as Uri
        Toast.makeText(this,"Uploading please wait!!", Toast.LENGTH_LONG).show()
        //Upload photo to Firebase
        val imageFileName = "images/${System.currentTimeMillis()}-photo.jpg"
        val photoRef = storageReference.child(imageFileName).putFile(photoUploadUri)
        photoRef.addOnSuccessListener{
            val imageUrl = storageReference.child(imageFileName).downloadUrl

            imageUrl.addOnSuccessListener {
                Toast.makeText(this,"Uploaded Successfully", Toast.LENGTH_SHORT).show()
                proBar.visibility = View.GONE
                Log.i(TAG,"Image Url -> $it")
                val text = etDescription.text.toString()
                val image = it.toString()
                postDao.addPost(text,image)
                val mainIntent = Intent(this, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this,"Failed to Upload", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this,"Failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when(requestCode) {

            PICK_PHOTO_CODE -> {
                if(resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        launchImageCrop(it)
                    }
                }
                else {
                    Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if(resultCode == Activity.RESULT_OK) {
                    result.uri?.let { uri ->
                        photoUri = uri
                        ivUpload.setImageURI(photoUri)
                    }
                }
                else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(this,"Failed to Crop",Toast.LENGTH_SHORT).show()

                }
            }



        }
        /**if(requestCode == PICK_PHOTO_CODE && resultCode == Activity.RESULT_OK) {
        photoUri = data?.data
        ivUpload.setImageURI(photoUri)
        }
        else {
        Toast.makeText(this,"Image Picker Actions Has Been Canceled",Toast.LENGTH_SHORT).show()
        }**/
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1920,1600)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
    }
}