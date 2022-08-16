package com.sumeet.instafirecrud.tabs

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.sumeet.instafirecrud.MainActivity
import com.sumeet.instafirecrud.R
import kotlinx.android.synthetic.main.activity_test_sign_in.*

private const val TAG = "TestSignInActivity"
class TestSignInActivity : AppCompatActivity() {


    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_sign_in)

        auth = FirebaseAuth.getInstance()

        btnLogin.setOnClickListener {
            btnLogin.isEnabled = false
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            if(email.isBlank() || password.isBlank()) {
                Toast.makeText(this,"Email/Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //Firebase Authentication check
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                btnLogin.isEnabled  = true
                if(task.isSuccessful) {
                    Toast.makeText(this,"Success!", Toast.LENGTH_SHORT).show()
                    goPostsActivity()
                }
                else {
                    Log.e(TAG,"signInWithEmail failed",task.exception)
                    Toast.makeText(this,"Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    private fun goPostsActivity() {
        Log.i(TAG,"goPostsActivity")
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        //After Login if you will press back button then app will close directly
        finish()

    }
}