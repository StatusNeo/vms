package com.android.visitormanagementsystem.ui.adminpanel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.databinding.ActivityAdminPanelBinding
import com.android.visitormanagementsystem.ui.adapters.GetAllHostAdapter
import com.android.visitormanagementsystem.ui.addhostprofile.AddHostActivity
import com.android.visitormanagementsystem.ui.adminreports.AdminReportsActivity
import com.android.visitormanagementsystem.ui.interfaces.OnHostClickInterface
import com.android.visitormanagementsystem.ui.interfaces.OnHostListInterface
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.showLogoutDialog
import com.android.visitormanagementsystem.utils.toast
import java.io.Serializable

class AdminPanelActivity:AppCompatActivity(), OnHostListInterface {

    lateinit var listAdapter: GetAllHostAdapter
    var hostViewState = ProgressBarViewState()
    lateinit var hostViewModel : GetHostViewModel
    lateinit var binding : ActivityAdminPanelBinding

    override fun onResume() {
        super.onResume()
        hostViewModel.getHostDetails()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hostViewModel = ViewModelProvider(this)[GetHostViewModel::class.java]
        setContentView(ActivityAdminPanelBinding.inflate(layoutInflater).apply {
            binding = this
            hostViewModel.setReportInterface(this@AdminPanelActivity)

            btnReports.setOnClickListener{
                val intent = Intent(this@AdminPanelActivity, AdminReportsActivity::class.java)
                intent.putExtra("Called_From","Admin")
                startActivity(intent)
            }
            btnAddHost.setOnClickListener{
                val intent = Intent(this@AdminPanelActivity, AddHostActivity::class.java)
                intent.putExtra("Called_From","New_User")
                startActivity(intent)
            }
            ivLogout.setOnClickListener{
                showLogoutDialog(this@AdminPanelActivity)
            }

            with(hostRecyclerView) {
                var items: ArrayList<GetAllHostUiModel> = ArrayList()
                layoutManager = LinearLayoutManager(context)
                adapter = GetAllHostAdapter(items, object : OnHostClickInterface {
                    override fun onHostClick(uiModel: GetAllHostUiModel) {
                        // Open Host Details screen
                        val intent = Intent(this@AdminPanelActivity, AddHostActivity::class.java)
                        intent.putExtra("Called_From","Update_User")
                        intent.putExtra("HostUiModel", uiModel as Serializable)
                        startActivity(intent)
                    }
                })
                listAdapter = adapter as GetAllHostAdapter
            }
        }.root)
    }

    override fun onBackPressed() {
        this@AdminPanelActivity.finish()
    }

    override fun getHostList(items: List<GetAllHostUiModel>) {
        if(items.isEmpty()) {
            toast(R.string.msg_no_data)
          /*  binding.noDataTv.visibility = View.VISIBLE
            binding.noDataView.visibility = View.VISIBLE*/
        }else{
          /*  binding.noDataTv.visibility = View.GONE
            binding.noDataView.visibility = View.GONE
            binding.homeReportsRecyclerView.visibility = View.VISIBLE*/
        }
        println("Host List " + items.size)
        listAdapter.setUserList(items)
    }
}