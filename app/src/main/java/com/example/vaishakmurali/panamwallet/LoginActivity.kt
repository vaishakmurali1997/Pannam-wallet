package com.example.vaishakmurali.panamwallet

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    // Initialization goes here
    private var mAuth: FirebaseAuth? = null
    private var progress: ProgressDialog? = null
    private var RC_SIGN_IN = 1
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null
    private val TAG = "Error"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progress = ProgressDialog(this@LoginActivity)
        progress!!.setCanceledOnTouchOutside(false)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this) { Toast.makeText(applicationContext, "Network Error.", Toast.LENGTH_SHORT).show() }
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        mAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {

                // Calling userDetails function
                userDetails()
            }

            // Action to for google sign in button
            mGoogleBtn.setOnClickListener {

                // Calling signIn function
                signIn()
            }


        }
    }

    // Function for sign in operations
    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthStateListener!!)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {

                myProgressDialog("Please Wait","We are loading your profile")
                // Google Sign In was successful, authenticate with Firebase
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)

            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(applicationContext, result.toString(), Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful)

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful) {
                        Log.w(TAG, "signInWithCredential", task.exception)
                        Toast.makeText(applicationContext, task.exception!!.toString() + "", Toast.LENGTH_SHORT).show()

                    } else {
                    }
                }
    }

    // Creating a function for checking & creating user details
    private fun userDetails() {
        content.visibility = View.INVISIBLE
        val database = FirebaseDatabase.getInstance()
        var myRef = database.reference

        // Checking if the user has previously registered with us using this account
        myRef.child("Users").child("UserID").child(FirebaseAuth.getInstance().currentUser!!.uid).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {

                // Action to be performed if the user used this account before
                //if (p0!!.child(FirebaseAuth.getInstance().currentUser!!.uid).exists()){
                if (p0!!.hasChildren()){
                    progress!!.dismiss()
                    val i = Intent(this@LoginActivity, HomeActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                    finish()
                }else{

                    // Action to be performed if the user is registering for the first time using this account
                    val data = AmountDetails(FirebaseAuth.getInstance().currentUser!!.displayName!!,0)
                    myRef = database.reference.child("Users").child("UserID").child(FirebaseAuth.getInstance().currentUser!!.uid)
                    myRef.setValue(data)
                    val intentForHomeActivity2 = Intent(this@LoginActivity, HomeActivity::class.java)
                    intentForHomeActivity2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intentForHomeActivity2)
                    finish()
                }
            }
        })
    }

    // Creating function for progress dialog
    private fun myProgressDialog(title: String, message: String){
        progress!!.setTitle(title)
        progress!!.setMessage(message)
        progress!!.show()
    }

    // Creating data class for amount details to be transferred to database
    data class AmountDetails(val name: String?, val amount: Int?)
}
