package com.example.vaishakmurali.panamwallet

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.Image
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.Snackbar
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_home.*
import java.lang.reflect.Type

class HomeActivity : AppCompatActivity() {

    // All initialization goes here
    private var mGoogleApiClient: GoogleApiClient? = null
    private var progress: ProgressDialog? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        progress = ProgressDialog(this@HomeActivity)
        progress!!.setCanceledOnTouchOutside(false)

        // Action if the user is not logged is
        if (FirebaseAuth.getInstance().currentUser == null){
            myProgressDialog("Please Wait","Loading your profile")
            val intentForLoginActivity = Intent(this@HomeActivity, LoginActivity::class.java)
            intentForLoginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentForLoginActivity)
            finish()
            progress!!.dismiss()
        }

        // Making amount balance uneditable by user
        amountBalance.isEnabled = false

        // Displaying amount balance
        availableBalanceFunction(amountBalance)

        // Action for add money button
        addMoneyBtn.setOnClickListener {
            myProgressDialog(null,"Redirecting")
            val intentForAddMoneyActivity = Intent(this@HomeActivity, AddMoneyActivity::class.java)
            startActivity(intentForAddMoneyActivity)
            progress!!.dismiss()
        }

        // Action for send money button
        sendMoneyBtn.setOnClickListener{
            myProgressDialog(null,"Redirecting")
            val sendMoneyActivityIntent = Intent(this@HomeActivity, SendMoneyActivity::class.java)
            startActivity(sendMoneyActivityIntent)
            progress!!.dismiss()
        }

        // Action for payment history button
        paymentHistoryBtn.setOnClickListener {
            myProgressDialog(null,"Redirecting")
            val paymentHistoryActivityIntent = Intent(this,PaymentHistoryActivity::class.java)
            startActivity(paymentHistoryActivityIntent)
            progress!!.dismiss()
        }


        acceptMoneyBtn.setOnClickListener {
            myProgressDialog(null,"Redirecting")
            val acceptMoneyActivityIntent = Intent(this@HomeActivity,AcceptMoneyActivity::class.java)
            startActivity(acceptMoneyActivityIntent)
            progress!!.dismiss()
        }

        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()


        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, GoogleApiClient.OnConnectionFailedListener {
                    Toast.makeText(applicationContext, "Network Error.", Toast.LENGTH_SHORT).show()
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        // Action for sign out button
        signOutBtn.setOnClickListener {
            myProgressDialog("Please Wait","Signing you out")
            mAuth!!.signOut()
            Auth.GoogleSignInApi.signOut(mGoogleApiClient)
            val i = Intent(this@HomeActivity, LoginActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
            progress!!.dismiss()
        }



    }

    // Creating function for progress dialog
    private fun myProgressDialog(title: String?, message: String){
        progress!!.setTitle(title)
        progress!!.setMessage(message)
        progress!!.show()
    }
    @SuppressLint("SetTextI18n")
    private fun availableBalanceFunction(view: EditText){

        // Setting up firebase reference
        var myRef = FirebaseDatabase.getInstance().reference

        // assigning default view value.
        view.setText("null")
        myRef.child("Users").child("UserID")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addValueEventListener(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}

                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(p0: DataSnapshot?) {

                        // Setting the value of amount to the view

                        //val phoneNumber = p0!!.child("phoneNumber").value
                        if(!p0!!.hasChild("phoneNumber")){

                            // Alert DialogBox to ask user for phone number
                            val input = EditText(this@HomeActivity)
                            input.inputType = EditorInfo.TYPE_CLASS_NUMBER
                            val phoneValue = EditText(this@HomeActivity).text
                            input.text = phoneValue
                            AlertDialog.Builder(this@HomeActivity)
                                    .setTitle("Please fill your details")
                                    .setMessage("Please enter your phone number so as to perform payment transaction.")
                                    .setView(input)
                                    .setPositiveButton("Add Phone Number", { _, _ ->

                                        // checking if the user has entered valid phone number
                                        if(input.text.isEmpty() || input.text.length != 10){
                                            Toast.makeText(this@HomeActivity,"Please add a valid phone number.",Toast.LENGTH_SHORT).show()
                                            recreate()
                                        }else{

                                            // checking
                                            myRef.child("Users").child("UserID")
                                                    .orderByChild("phoneNumber").equalTo(input.text.toString())
                                                    .addListenerForSingleValueEvent(object: ValueEventListener{
                                                        override fun onCancelled(p0: DatabaseError?) {}

                                                        override fun onDataChange(p0: DataSnapshot?) {
                                                            // Checking if the phone number is already registered with us.
                                                            if (p0!!.hasChildren()){
                                                                Toast.makeText(this@HomeActivity,"This number has already been registered with us.",Toast.LENGTH_SHORT).show()
                                                                recreate()
                                                            }else{
                                                                // Saving the phone number in database
                                                                myRef = FirebaseDatabase.getInstance().reference
                                                                        .child("Users")
                                                                        .child("UserID")
                                                                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                                                        .child("phoneNumber")
                                                                myRef.setValue(input.text.toString())
                                                                Toast.makeText(this@HomeActivity, "Phone number added successfully", Toast.LENGTH_SHORT).show()

                                                            }
                                                        }
                                                    })

                                            }
                        })
                                    .setCancelable(false)
                                    .create()
                                    .show()
                        }
                            // Displaying amount balance
                            val data = p0.child("amount").value
                            view.setText("₹" + data)
                            progress!!.dismiss()


            }

        })
        // Displaying cash back ads
        shopListView.adapter = shopAdapter(this@HomeActivity)
    }

    // Class adapter for cash back ads
    class shopAdapter(context: Context):BaseAdapter(){
        private val myContext:Context
        init {
            myContext = context
        }

        val images = arrayListOf<Int>(R.drawable.moracoos,R.drawable.aloha,R.drawable.cocoa,R.drawable.enrich,R.drawable.monster,R.drawable.my_coffee_bar)
        val companyNames = myContext.resources.getStringArray(R.array.companyName)
        val compnayDescription = myContext.resources.getStringArray(R.array.description)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val shopView = LayoutInflater.from(myContext).inflate(R.layout.shop_layout,parent,false)
            val image = shopView.findViewById<ImageView>(R.id.Image)
            image.setImageResource(images[position])
            val names = shopView.findViewById<TextView>(R.id.companyName)
            names.setText(companyNames[position])
            val description = shopView.findViewById<TextView>(R.id.companyDescription)
            description.setText(compnayDescription[position])
           return shopView
        }

        override fun getItem(position: Int): Any {
            return "TEST"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return images.size
        }

    }
}
