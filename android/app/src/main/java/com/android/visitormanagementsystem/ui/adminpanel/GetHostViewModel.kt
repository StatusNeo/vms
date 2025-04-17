package com.android.visitormanagementsystem.ui.adminpanel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.visitormanagementsystem.ui.interfaces.OnHostListInterface
import com.android.visitormanagementsystem.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class GetHostViewModel (application: Application) : AndroidViewModel(application) {
    private lateinit var onHostInterface: OnHostListInterface
    var initVisitorList: ArrayList<GetAllHostUiModel> = ArrayList()

    fun getHostDetails() {

        initVisitorList.clear()
        val myDB = FirebaseFirestore.getInstance().collection(Constants.EMPLOYEE_LIST)
        myDB.get().addOnSuccessListener { result ->
            if(result.isEmpty) {
                onHostInterface.getHostList(initVisitorList)
            } else {
                for(document in result) {

                    if(document.data[Constants.EMP_ROLE].toString() != "Admin") {
                        var visitorImage: String = ""
                        visitorImage = if(document.data["hostImage"].toString()
                                .isNullOrBlank() || document.data["hostImage"].toString()
                                .isNullOrEmpty()
                        ) {
                            "https://opt.toiimg.com/recuperator/img/toi/m-69257289/69257289.jpg"

                        } else {
                            document.data["hostImage"].toString()
                        }

                        /*val stamp = document.data[Constants.TIMESTAMP] as Timestamp
                        val date = stamp.toDate()*/

                        initVisitorList.add(
                            GetAllHostUiModel(
                                document.id,
                                document.data[Constants.HOST_NAME].toString(),
                                document.data[Constants.HOST_MOBILE].toString(),
                                document.data[Constants.EMAIL].toString(),
                                document.data[Constants.DESIGNATION].toString(),
                                document.data[Constants.EMP_ROLE].toString(),
                                visitorImage
                            )
                        )
                    }
                }
                initVisitorList.sortWith(
                    compareBy(String.CASE_INSENSITIVE_ORDER) { it.hostName.toString() })
                onHostInterface.getHostList(initVisitorList)
            }
        }.addOnFailureListener { exception ->
            onHostInterface.getHostList(initVisitorList)
            Timber.tag("HostDetails error>>").w(exception, "Error getting documents.")
        }
    }

    fun setReportInterface(onReportInterface: OnHostListInterface) {
        this.onHostInterface = onReportInterface
    }
}