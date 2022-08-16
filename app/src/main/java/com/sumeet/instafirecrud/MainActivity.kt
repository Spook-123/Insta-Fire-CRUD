package com.sumeet.instafirecrud

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.sumeet.instafirecrud.adapter.OnItemClicked
import com.sumeet.instafirecrud.adapter.PostAdapter
import com.sumeet.instafirecrud.daos.PostDao
import com.sumeet.instafirecrud.models.Post
import com.sumeet.instafirecrud.tabs.CreateActivity
import com.sumeet.instafirecrud.tabs.ProfileActivity
import com.sumeet.instafirecrud.tabs.UpdateActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

open class MainActivity : AppCompatActivity(), OnItemClicked {

    companion object {
        const val EXTRA_USER_NAME = "EXTRA_USER_NAME"
        const val EXTRA_POST_ID = "EXTRA_POST_ID"
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PostAdapter
    private lateinit var postDao: PostDao
    private lateinit var post:MutableList<Post>
    private val signInUser = Firebase.auth.currentUser!!.displayName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth


        supportActionBar?.title = "All Posts"

        fabCreate.setOnClickListener {
            val createIntent = Intent(this, CreateActivity::class.java)
            startActivity(createIntent)
        }

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        post = mutableListOf()
        postDao = PostDao()
        val postsCollection = postDao.postsCollection
        var query = postsCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        val username = intent.getStringExtra(EXTRA_USER_NAME)
        if(username != null) {
            supportActionBar?.title = username
            query = query.whereEqualTo("createdBy.username",username)
        }
        val rvOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query,Post::class.java).build()
        adapter = PostAdapter(rvOptions,this)
        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_profile) {
            val profileIntent = Intent(this, ProfileActivity::class.java)
            profileIntent.putExtra(EXTRA_USER_NAME,signInUser)
            startActivity(profileIntent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }

    override fun onDeleteClicked(postId: String) {
        GlobalScope.launch(Dispatchers.Unconfined) {
            val postElement = postDao.getPostById(postId).await().toObject(Post::class.java)

            if(auth.currentUser!!.uid == postElement!!.createdBy.uid) {
                Toast.makeText(applicationContext,"Deleted", Toast.LENGTH_SHORT).show()
                postDao.deletePost(postId)
            }
            else {
                Toast.makeText(applicationContext,"Access Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onUpdateClicked(postId: String) {
        GlobalScope.launch(Dispatchers.Unconfined) {
            val postElement = postDao.getPostById(postId).await().toObject(Post::class.java)
            if(auth.currentUser!!.uid == postElement!!.createdBy.uid) {
                val intent = Intent(applicationContext, UpdateActivity::class.java)
                intent.putExtra(EXTRA_POST_ID,postId)
                startActivity(intent)
            }
            else {
                Toast.makeText(applicationContext,"Access Denied",Toast.LENGTH_SHORT).show()
            }
        }
    }


}