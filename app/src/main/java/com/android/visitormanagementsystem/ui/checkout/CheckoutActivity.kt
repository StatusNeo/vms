package com.android.visitormanagementsystem.ui.checkout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.android.visitormanagementsystem.CheckoutBinding
import com.android.visitormanagementsystem.ui.visitorList.VisitorListActivity
import com.android.visitormanagementsystem.ui.visitorList.VisitorListUiModel
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.getCurrentTime

class CheckoutActivity : AppCompatActivity() {
    var checkoutViewState = ProgressBarViewState()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(CheckoutBinding.inflate(layoutInflater).apply{
            val gson = Gson()
            val strObj = intent.getStringExtra("Clicked_Visitor")
            val uiModel: VisitorListUiModel = gson.fromJson(strObj, VisitorListUiModel::class.java)
            data = uiModel
            executePendingBindings()
            viewState = checkoutViewState
          /*  backBtn.setOnClickListener {
                val intent = Intent(this@CheckoutActivity, VisitorListActivity::class.java)
                startActivity(intent)
                this@CheckoutActivity.finish()
            }*/

            btnCheckout.setOnClickListener{
                val visitor = hashMapOf(
                    "outTime" to getCurrentTime()
                )
                val db = Firebase.firestore
                checkoutViewState.progressbarEvent = true
                var query=db.collection("visitorslist").whereEqualTo("hostMobileNo",uiModel.hostMobileNo)
                    .get()
                query.addOnSuccessListener {
                    checkoutViewState.progressbarEvent = false
                    for (document in it){
                        db.collection("visitorslist").document(document.id).set(visitor, SetOptions.merge())
                        showToast("Visitor checked out")
                        val intent = Intent(this@CheckoutActivity, VisitorListActivity::class.java)
                        startActivity(intent)
                        this@CheckoutActivity.finish()
                    }
                } .addOnFailureListener { exception ->
                    checkoutViewState.progressbarEvent = false
                    Log.w("checkout error>>", "Error getting documents.", exception)
                }
            }
        }.root)
    }

    private fun showToast(msg:String){
        val toast: Toast = Toast.makeText(this@CheckoutActivity, msg, Toast.LENGTH_SHORT)
        toast.show()
    }
}