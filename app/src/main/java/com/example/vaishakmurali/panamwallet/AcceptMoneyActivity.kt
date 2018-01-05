package com.example.vaishakmurali.panamwallet


import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.accept_money.*
import android.util.Base64



class AcceptMoneyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accept_money)

        // Receiving user's phone number from the database
        val myRef = FirebaseDatabase.getInstance().reference
        myRef.child("Users").child("UserID").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {

                        // encrypting the phone number and converting it into QR code
                        val phoneNumber = p0!!.child("phoneNumber").value.toString()
                        val encryptedNumber = Base64.encode(phoneNumber.toByteArray(),Base64.DEFAULT)
                        val myBitmap = net.glxn.qrgen.android.QRCode.from(String(encryptedNumber)).bitmap()
                        barCodeImage.setImageBitmap(myBitmap)
                    }
                })


    }
}
