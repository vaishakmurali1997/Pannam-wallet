package com.example.vaishakmurali.panamwallet

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_payment_history.*


class PaymentHistoryActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_history)

        // Initialization
        val mode = ArrayList<String>()
        val names = ArrayList<String>()
        val amount = ArrayList<Int>()

        // Fetching value from database.
        var myRef = FirebaseDatabase.getInstance().reference
                .child("Users")
                .child("UserID")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Logs")
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(p0: DataSnapshot?) {
                        for (data in p0!!.children){
                            mode.add(data.child("modes").value.toString())
                            names.add(data.child("name").value.toString())
                            amount.add(data.child("amount").value.toString().toInt())
                        }

                        history.adapter = CustomAdapter(this@PaymentHistoryActivity,mode,names,amount)
                    }
                })
    }

    //creating adapter class to display history
    class CustomAdapter(context: Context ,val mode:ArrayList<String>,val names:ArrayList<String>,val amount:ArrayList<Int>) : BaseAdapter() {
        private val mcontext:Context
        init {
            mcontext = context
        }

        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

            val layouts = LayoutInflater.from(mcontext).inflate(R.layout.layout_payment_history_custom,parent,false)
            val modeView = layouts.findViewById<TextView>(R.id.mode)
            val nameView = layouts.findViewById<TextView>(R.id.displayName)
            val amountView = layouts.findViewById<TextView>(R.id.amountDisplay)
            modeView.text = mode[position]
            nameView.text = names[position]
            amountView.text = "₹"+ amount[position].toString()
            return layouts
        }

        override fun getItem(position: Int): Any {
            return "TEST"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return names.size
        }


    }
}
