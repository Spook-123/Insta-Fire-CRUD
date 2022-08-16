package com.sumeet.instafirecrud.tabs

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sumeet.instafirecrud.MainActivity
import com.sumeet.instafirecrud.R
import com.sumeet.instafirecrud.daos.UserDao
import com.sumeet.instafirecrud.models.User
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_GOOGLE_SIGN_IN = 4566
    }
    private lateinit var googleSignInClient:GoogleSignInClient
    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        auth = Firebase.auth

        btnTestSignIn.setOnClickListener {
            val testIntent = Intent(this,TestSignInActivity::class.java)
            startActivity(testIntent)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

        btnSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)

        }
    }

    override fun onStart() {
        super.onStart()
        //Check if the user is signed in and update th UI accordingly
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        }catch (e: ApiException) {
            //Google SignIn Failed,update UI Accordingly
            Log.w(TAG,"Google sign in failed",e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        btnSignIn.visibility = View.GONE
        proBar.visibility = View.VISIBLE
        btnTestSignIn.isVisible = false
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                val user = auth.currentUser
                updateUI(user)
            }
            else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                updateUI(null)
            }
        }
    }
    private fun updateUI(currentUser: FirebaseUser?) {
        if(currentUser == null) {
            btnSignIn.visibility = View.VISIBLE
            proBar.visibility = View.GONE
            return
        }
        val user = User(currentUser.uid,currentUser.displayName.toString(),currentUser.photoUrl.toString())
        val userDao = UserDao()
        userDao.addUser(user)
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        finish()

    }
}