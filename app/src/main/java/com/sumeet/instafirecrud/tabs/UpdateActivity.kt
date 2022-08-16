package com.sumeet.instafirecrud.tabs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sumeet.instafirecrud.MainActivity
import com.sumeet.instafirecrud.MainActivity.Companion.EXTRA_POST_ID
import com.sumeet.instafirecrud.R
import com.sumeet.instafirecrud.daos.PostDao
import com.sumeet.instafirecrud.models.Post
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_update.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UpdateActivity : AppCompatActivity() {

    companion object {
        private const val PICK_PHOTO_CODE = 4568
        private const val TAG = "UpdateActivity"
    }
    private var photoUri: Uri? = null
    private lateinit var storageRef: StorageReference
    private var postId:String? = null
    private var flag = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        storageRef = FirebaseStorage.getInstance().reference

        postId = intent.getStringExtra(EXTRA_POST_ID)!!

        GlobalScope.launch(Dispatchers.Unconfined) {
            val postDao = PostDao()
            val post = postDao.getPostById(postId!!).await().toObject(Post::class.java)!!
            etDescriptionUpdate.setText(post.description)
            Glide.with(applicationContext).load(post.imageUrlPost).apply(
                RequestOptions().transform(
                    CenterCrop(), RoundedCorners(20)
                )
            ).into(ivUploadUpdate)
            proBarUpdate.visibility = View.GONE
        }


        btnChooseImageUpdate.setOnClickListener {
            val imagePickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerIntent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg","image/png","image/jpg")
            imagePickerIntent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes)
            imagePickerIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            if(imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }

        btnUpdate.setOnClickListener {
            btnUpdate.isEnabled = false
            if(flag == 1) {
                getImageUrl()
                proBarUpdate.visibility = View.VISIBLE
            }
            else {
                getNewDataWithoutImageChange()
                proBarUpdate.visibility = View.VISIBLE
            }
        }
    }

    private fun getNewDataWithoutImageChange() {
        val newTime = System.currentTimeMillis()
        val newDescription = etDescriptionUpdate.text.toString()
        val map = mutableMapOf<String,Any>()
        map["createdAt"] = newTime
        if(newDescription.isNotEmpty()) {
            map["description"] = newDescription
        }
        val changed = "Edited"
        map["textEdit"] = changed
        updateData(map)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when(requestCode) {

            PICK_PHOTO_CODE -> {
                if(resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        launchImageCrop(it)
                    }
                }
                else  {
                    Toast.makeText(this,"Failed", Toast.LENGTH_SHORT).show()
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if(resultCode == Activity.RESULT_OK) {
                    result.uri?.let { uri ->
                        photoUri = uri
                        ivUploadUpdate.setImageURI(photoUri)
                        val postDao = PostDao()
                        flag = 1
                        GlobalScope.launch {
                            val post = postDao.getPostById(postId!!).await().toObject(Post::class.java)!!
                            val fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(post!!.imageUrlPost)
                            fileRef.delete()
                        }
                    }
                }
                else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(this,"Failed to Crop", Toast.LENGTH_SHORT).show()
                }
            }


        }

        /**if(requestCode == PICK_PHOTO_CODE && resultCode == Activity.RESULT_OK) {
        photoUri = data?.data
        ivUploadUpdate.setImageURI(photoUri)
        val postDao = PostDao()
        flag = 1
        GlobalScope.launch {
        val post = postDao.getPostById(postId!!).await().toObject(Post::class.java)!!
        val fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(post!!.imageUrlPost)
        fileRef.delete()
        }
        }
        else {
        Toast.makeText(this,"Image picker action has been canceled",Toast.LENGTH_SHORT).show()
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

    private fun getImageUrl() {
        val photoUploadUri = photoUri as Uri
        val imageFileName = "images/${System.currentTimeMillis()}-photo.jpg"
        val photoRef = storageRef.child(imageFileName).putFile(photoUploadUri)
        photoRef.addOnSuccessListener {
            val image = storageRef.child(imageFileName).downloadUrl
            image.addOnSuccessListener {
                val imageUrl = it.toString()
                getNewData(imageUrl)
            }.addOnFailureListener {
                Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getNewData(newUrl: String) {
        val newTime = System.currentTimeMillis()
        val newDescription = etDescriptionUpdate.text.toString()
        val map = mutableMapOf<String,Any>()
        map["createdAt"] = newTime
        if(newDescription.isNotEmpty()) {
            map["description"] = newDescription
        }
        map["imageUrlPost"] = newUrl
        val changed = "Edited"
        map["textEdit"] = changed
        updateData(map)
    }

    private fun updateData(newData: MutableMap<String, Any>) {
        GlobalScope.launch(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val postsCollections = db.collection("posts")
            postsCollections.document(postId!!).set(
                newData,
                SetOptions.merge()
            ).await()
        }
        proBarUpdate.visibility = View.GONE
        Toast.makeText(this,"Updated Successfully",Toast.LENGTH_SHORT).show()
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
}