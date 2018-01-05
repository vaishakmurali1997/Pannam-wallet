package com.example.vaishakmurali.panamwallet

// Importing necessary files

import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Base64
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_send_money.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class SendMoneyActivity : AppCompatActivity() {

    // Setting up permission code
    private val PERMISSION_REQUEST = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        val scannerView = ZXingScannerView(this@SendMoneyActivity)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_money)

        // To check if the user is transferring money using QR Code.
        if(intent.extras?.getString("QR_VALUE_RECEIVED") == "YES"){
            phoneTextField.setText(intent.extras.getString("QR_VALUE"))
        }

        // Action for send money button.
        sendButton.setOnClickListener {

            // Storing phone number.
            val phone = phoneTextField.text.toString()

            // Validation for phone number.
            if (phone.isEmpty() || phone.length !=10){
                Toast.makeText(this@SendMoneyActivity, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            }

            // Validation for amount Text field.
            else if(amountTextField.text.isEmpty()){
                Toast.makeText(this@SendMoneyActivity, "Please enter amount", Toast.LENGTH_SHORT).show()
            }
            else{
                val amount = amountTextField.text.toString().toInt()
                var myRef = FirebaseDatabase.getInstance().reference
                myRef.child("Users").child("UserID").child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {}
                            override fun onDataChange(p0: DataSnapshot?) {

                                // Action to be performed if the user tries to send money to himself.
                                val validPhone = p0!!.child("phoneNumber").value
                                if(validPhone == phone){
                                    Toast.makeText(this@SendMoneyActivity,"You can not send money to your own account",Toast.LENGTH_SHORT).show()
                                    phoneTextField.text.clear()
                                    amountTextField.text.clear()
                                    recreate()
                                }

                                // initialization for holding amount.
                                val newAmount: Int
                                val databaseAmountValue = (p0.child("amount").value).toString().toInt()
                                val receiverName = p0.child("name").value.toString()
                                when {

                                    // to check if the user has sufficient funds to send money.
                                    databaseAmountValue < amount -> {
                                        Toast.makeText(this@SendMoneyActivity,"Insufficient funds in your wallet", Toast.LENGTH_SHORT).show()
                                        val addMoneyActivityIntent = Intent(this@SendMoneyActivity,AddMoneyActivity::class.java)
                                        startActivity(addMoneyActivityIntent)
                                        finish()
                                    }

                                    // Action to be performed if user has sufficient funds.
                                    databaseAmountValue >= amount -> {
                                        newAmount = databaseAmountValue - amount
                                        myRef = FirebaseDatabase.getInstance().reference
                                                .child("Users")
                                                .child("UserID")
                                                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                                .child("amount")
                                        myRef.setValue(newAmount)
                                        myRef = FirebaseDatabase.getInstance().reference
                                        myRef.child("Users").child("UserID")
                                                .orderByChild("phoneNumber").equalTo(phone)
                                                .addListenerForSingleValueEvent(object :ValueEventListener{
                                                    override fun onCancelled(p0: DatabaseError?) {}
                                                    override fun onDataChange(p0: DataSnapshot?) {

                                                        // checking for valid user money transfer.
                                                        if(p0!!.hasChildren()){
                                                            var senderAmount = 0
                                                            var senderUid = ""
                                                            var senderName = ""
                                                            for (data in p0.children){
                                                                senderUid = data.key.toString()
                                                                senderName = data.child("name").value.toString()
                                                                senderAmount = data.child("amount").value.toString().toInt()
                                                                senderAmount += amount
                                                            }

                                                            // Reflecting the transferred money in sender's account
                                                            myRef = FirebaseDatabase.getInstance().reference
                                                                    .child("Users")
                                                                    .child("UserID")
                                                                    .child(""+senderUid)
                                                                    .child("amount")
                                                            myRef.setValue(senderAmount).addOnSuccessListener {
                                                                Toast.makeText(this@SendMoneyActivity,"Money transferred Successfully",Toast.LENGTH_SHORT).show()
                                                                phoneTextField.text.clear()
                                                                amountTextField.text.clear()
                                                                recreate()
                                                            }

                                                            // setting up logs for payment history ()
                                                            myRef = FirebaseDatabase.getInstance().reference
                                                                    .child("Users")
                                                                    .child("UserID")
                                                                    .child(""+senderUid)
                                                                    .child("Logs")
                                                                    .push()
                                                            myRef.setValue(Transactions(receiverName,amount,"Amount Received")).addOnSuccessListener {
                                                                phoneTextField.text.clear()
                                                                amountTextField.text.clear()
                                                                recreate()
                                                            }
                                                            myRef = FirebaseDatabase.getInstance().reference
                                                                    .child("Users")
                                                                    .child("UserID")
                                                                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                                                    .child("Logs")
                                                                    .push()
                                                            myRef.setValue(Transactions(senderName,amount,"Amount Sent")).addOnSuccessListener {
                                                                phoneTextField.text.clear()
                                                                amountTextField.text.clear()
                                                                recreate()
                                                            }

                                                        }else{

                                                            // Action to be performed if the entered mobile number doesn't exists.
                                                            Toast.makeText(this@SendMoneyActivity, "Invalid transaction. Please enter valid phone number", Toast.LENGTH_LONG).show()
                                                            myRef = FirebaseDatabase.getInstance().reference
                                                                    .child("Users")
                                                                    .child("UserID")
                                                                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                                                    .child("amount")
                                                            myRef.setValue(newAmount+amount)
                                                            phoneTextField.text.clear()
                                                            amountTextField.text.clear()
                                                            recreate()
                                                        }
                                                    }
                                                })
                                    }
                                    else -> Toast.makeText(this@SendMoneyActivity,"Sorry something went wrong",Toast.LENGTH_SHORT).show()

                                }
                            }
                        })
            }
        }

        //Action to be performed is user selects QR Code money transfer option
        qrButton.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),PERMISSION_REQUEST)
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)
            {
                // Setting up camera for QR code scan
                setContentView(scannerView)
                scannerView.setAutoFocus(true)
                scannerView.startCamera()
                scannerView.setResultHandler { p0: com.google.zxing.Result? ->

                    // Receiving encrypted data.
                    val requestCode = p0!!.text.toString()

                    // Decoding the encrypted data to get the sender's number
                    val qrValue = Base64.decode(requestCode.toByteArray(),Base64.DEFAULT)
                    val sendActivityIntent = Intent(this@SendMoneyActivity,SendMoneyActivity::class.java)
                    sendActivityIntent.putExtra("QR_VALUE",""+ String(qrValue))
                    sendActivityIntent.putExtra("QR_VALUE_RECEIVED","YES")
                    startActivity(sendActivityIntent)
                }
            }
            else{

                // Asking user to allow camera permissions required
                Toast.makeText(this@SendMoneyActivity,"Please allow the camera permission to enable QR CODE SCAN",Toast.LENGTH_LONG).show()
            }
        }
    }
    // Classes for logs of payments
    data class Transactions(val Name:String, val Amount:Int, val modes:String?)
}












