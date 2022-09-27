package com.android.visitormanagementsystem.ui.aboutus

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.utils.getCurrentDate

class AboutUsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        var tvContent=findViewById<TextView>(R.id.tv_content)
        var tvBuild=findViewById<TextView>(R.id.tv_build)
        var tvDate=findViewById<TextView>(R.id.tv_date)
        var btn=findViewById<Button>(R.id.btn_ok)
        var backBtn=findViewById<ImageView>(R.id.backBtn)

        tvBuild.text = "Build Version: Build No. 1.0 Beta Release"
        tvDate.text= "Release Date: "+ getCurrentDate()
        btn
            .setOnClickListener {
                this@AboutUsActivity.finish()
            }
        backBtn.setOnClickListener{
            this@AboutUsActivity.finish()
        }
    }
}