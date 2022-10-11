package com.android.visitormanagementsystem.ui.registervisitor

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.android.visitormanagementsystem.BuildConfig
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.RegisterVisitorBinding
import com.android.visitormanagementsystem.ui.visitorList.VisitorListActivity
import com.android.visitormanagementsystem.utils.*
import com.google.firebase.firestore.FirebaseFirestore
import java.io.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.Exception

class RegisterVisitorActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    var isPhotoUploaded: Boolean = false
    private var imageUri: Uri? = null
    var imageUrl: String = ""
    private val rootDatabase = FirebaseDatabase.getInstance().getReference("images")
    var progressViewState = ProgressBarViewState()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(RegisterVisitorBinding.inflate(layoutInflater).apply {
            viewStateProgress = progressViewState

        }.root)
        var gender: String = "Male"

        var ivPhoto = findViewById<ShapeableImageView>(R.id.ivPhoto)
        var tvPhoto = findViewById<TextView>(R.id.tv_photo)

        var btnRegister = findViewById<Button>(R.id.btn_register)
        var btnSubmit = findViewById<Button>(R.id.btn_submit)
        var etBatchNo = findViewById<EditText>(R.id.et_batch_no)
        var et_mobile_number = findViewById<EditText>(R.id.et_mobile_number)
        var et_visitor_name = findViewById<EditText>(R.id.et_visitor_name)
        var et_host_name = findViewById<EditText>(R.id.et_host_name)
        var et_host_mobile = findViewById<EditText>(R.id.et_host_mobile)
        var et_email = findViewById<EditText>(R.id.etEmailId)
        var et_purpose = findViewById<EditText>(R.id.et_purpose)
        var et_no_person = findViewById<EditText>(R.id.et_no_person)
        var et_address = findViewById<EditText>(R.id.et_address)
        var scrollView = findViewById<NestedScrollView>(R.id.scrollView)
        // var btnBack = findViewById<ImageView>(R.id.backBtn)
        var radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        et_mobile_number.setText(intent.getStringExtra("mobile").toString())

        checkVisitorData(et_visitor_name, et_email, et_address, ivPhoto, radioGroup)
//        btnBack.setOnClickListener {
//            this@RegisterVisitorActivity.finish()
//        }
        ivPhoto.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.CAMERA), 123
                )
            } else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra("selectContent", 4)
                    openCVResultLauncher.launch(intent)
                } else {
                    var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    openLegacyResultLauncher.launch(intent)
                }

            }
        }

        tvPhoto.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.CAMERA), 123
                )
            } else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra("selectContent", 4)
                    openCVResultLauncher.launch(intent)
                } else {
                    var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    openLegacyResultLauncher.launch(intent)
                }
            }
        }

        btnRegister.setOnClickListener {
            var noOfPerson = 0
            if(et_no_person.text.isNotBlank()) {
                noOfPerson = et_no_person.text.toString().toInt()
            }

            if(et_mobile_number.text.isBlank()) {
                et_mobile_number.requestFocus()
            } else if(et_visitor_name.text.isBlank()) {
                et_visitor_name.requestFocus()
                et_visitor_name.error = "Please enter visitor name"
            } else if(!isPhotoUploaded) {
                showToast(R.string.error_photo_upload)
            } else if(et_email.text.isNotEmpty() && !et_email.text.matches(Constants.EMAIL_REGEX.toRegex())) {
                et_email.error = "Please enter valid Email";
            } else if(et_purpose.text.isBlank()) {
                et_purpose.requestFocus()
                et_purpose.error = "Please enter purpose of visit"
            } else if(et_no_person.text.isBlank()) {
                et_no_person.requestFocus()
                et_no_person.error = "Please enter no. of persons visiting"
            } else if(noOfPerson !in 0..5) {
                et_no_person.requestFocus()
                et_no_person.error = "No. of persons visiting should not be more than 5"
            } else if(et_address.text.isBlank()) {
                et_address.requestFocus()
                et_address.error = "Please enter your address"
            } else if(radioGroup.checkedRadioButtonId == -1) {
                toast(R.string.text_gender)
            } else if(et_host_name.text.isBlank()) {
                et_host_name.requestFocus()
                et_host_name.error = "Please enter host name"
            } else if(et_host_mobile.text.isBlank()) {
                et_host_mobile.requestFocus()
                et_host_mobile.error = "Please enter host mobile number"
            } else if(et_host_mobile.text.length != 10) {
                et_host_mobile.error = "Mobile number should be 10 digit's"
            } else if(!et_host_mobile.text.matches(Constants.MOBILE_NUMBER_REGEX.toRegex())) {
                et_host_mobile.error = "Invalid Mobile Number";
            } else {
                btnRegister.setBackgroundResource(R.drawable.grey_button)
                btnRegister.isEnabled = false
                btnRegister.isClickable = false
                etBatchNo.visibility = View.VISIBLE
                btnSubmit.visibility = View.VISIBLE
                scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }
        btnSubmit.setOnClickListener {
            if(etBatchNo.text.isBlank()) {
                etBatchNo.requestFocus()
                etBatchNo.error = "Please enter batch number"
            } else if(etBatchNo.text.length != 4) {
                etBatchNo.requestFocus()
                etBatchNo.error = "Batch number should be 4 digits"
            } else if(imageUrl.isBlank() || imageUrl.isEmpty()) {
                toast(R.string.error_photo_upload)
            } else {
                if(radioGroup.checkedRadioButtonId == 1) {
                    gender = "Male"
                } else {
                    gender = "Female"
                }
                progressViewState.progressbarEvent = true
                addNewVisitor(
                    et_visitor_name.text.toString(),
                    et_mobile_number.text.toString(),
                    et_email.text.toString(),
                    imageUrl,
                    false,
                   et_address.text.toString(),
                )
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)
                val formattedDate = current.format(formatter)
                val visits = hashMapOf(
                    "visitorName" to et_visitor_name.text.toString(),
                    "visitorEmail" to et_email.text.toString(),
                    "visitorMobileNo" to et_mobile_number.text.toString(),
                    "visitorImage" to imageUrl,
                    "visitDate" to formattedDate.toString(),
                    "inTime" to getCurrentTime(),
                    "batchNo" to etBatchNo.text.toString(),
                    "hostName" to et_host_name.text.toString(),
                    "hostMobileNo" to et_host_mobile.text.toString(),
                    "purpose" to et_purpose.text.toString(),
                    "noOfPersons" to et_no_person.text.toString(),
                    "gender" to gender,
                    Constants.TIMESTAMP to FieldValue.serverTimestamp(),
                    "outTime" to "NA"
                )

                    db.collection(Constants.VISITOR_LIST).add(visits).addOnSuccessListener {
                        progressViewState.progressbarEvent = false

                        val intent =
                            Intent(this@RegisterVisitorActivity, VisitorListActivity::class.java)
                        startActivity(intent)
                        this@RegisterVisitorActivity.finish()
                    }.addOnFailureListener { exception ->
                        progressViewState.progressbarEvent = false
                        Log.w("users error>>", "Error getting documents.", exception)
                    }
            }
        }
    }

    private fun checkVisitorData(
        et_visitor_name: EditText,
        et_email: EditText,
        et_address: EditText,
        iv_photo: ShapeableImageView,
        rg: RadioGroup
    ) {
        val myDB = FirebaseFirestore.getInstance().collection(Constants.VISITOR_DB)
        val query1: Query = myDB.whereEqualTo(Constants.VISITOR_MOBILE, intent.getStringExtra("mobile").toString())
        query1.get().addOnSuccessListener { result ->
                for(document in result) {
                    if(result.isEmpty) {
                        Log.w("users data>>", "Not found.")

                    } else {
                        et_visitor_name.setText(document.data["visitorName"].toString())
                        et_email.setText(document.data["visitorEmail"].toString())
                        et_address.setText(document.data["address"].toString())

//                        iv_photo.loadImage(document.data[Constants.VISITOR_IMAGE].toString())
//                        isPhotoUploaded=true

                        // var gender:String=document.data["gender"].toString()
                        //  var radioM = findViewById<RadioButton>(R.id.radioMale)

//                        if (gender == "Male"){
//                            radioM.isChecked=true
//                            rg.id=R.id.radioMale
//                        }
                    }
                }
            }.addOnFailureListener { exception ->
                Log.w("users error>>", "Error getting documents.", exception)
            }
    }

    private fun showToast(msg: Int) {
        val toast: Toast = Toast.makeText(this@RegisterVisitorActivity, msg, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun uploadImage() {
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)
        val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")
        imageUri?.let {
            storageReference.putFile(it).addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener(OnSuccessListener<Uri> { uri ->
                    imageUrl = uri.toString()
                    isPhotoUploaded = true
                    progressViewState.progressbarEvent = false
                    Log.d("uri>>>", uri.toString())
                })
            }.addOnFailureListener {
                progressViewState.progressbarEvent = false
            }
        }
    }

    private fun saveTheImageLegacyStyle(inContext: Context, inImage: Bitmap): Uri? {
        var imagesFolder: File = File(inContext.cacheDir, "images")
        var uri: Uri? = null
        imagesFolder.mkdir()
        var file: File = File(imagesFolder, "captured_image.jpg")
        var stream: FileOutputStream = FileOutputStream(file)
        try {
            inImage.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(
                inContext.applicationContext, BuildConfig.APPLICATION_ID + ".provider", file
            )
        } catch(e: FileNotFoundException) {
        }
        return uri


    }

    private fun saveImageInQ(bitmap: Bitmap): Uri? {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: Uri? = null
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        //use application context to get contentResolver
        val contentResolver = application.contentResolver

        contentResolver.also { resolver ->
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        }

        fos?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it) }

        contentValues.clear()
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
        imageUri?.let { contentResolver.update(it, contentValues, null, null) }

        return imageUri
    }

    private var openCVResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            progressViewState.progressbarEvent = true
            try {
                if(result.resultCode == Activity.RESULT_OK) {
                    var bmp: Bitmap = result.data?.extras?.get("data") as Bitmap
                    imageUri = saveImageInQ(bmp)
                    var ivPhoto = findViewById<ShapeableImageView>(R.id.ivPhoto)
                    ivPhoto.setImageBitmap(bmp)
                    uploadImage()

                }
            } catch(e: Exception) {
                progressViewState.progressbarEvent = false
            }
        }

    private var openLegacyResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            progressViewState.progressbarEvent = true

            try {
                if(result.resultCode == Activity.RESULT_OK) {
                    var extras: Bundle? = result.data?.extras
                    var bmp: Bitmap = result.data?.extras?.get("data") as Bitmap
                    imageUri = saveTheImageLegacyStyle(this, bmp)
                    var ivPhoto = findViewById<ShapeableImageView>(R.id.ivPhoto)
                    ivPhoto.setImageBitmap(bmp)
                    uploadImage()
                }
            } catch(e: Exception) {
                progressViewState.progressbarEvent = false
            }
        }

    private fun addNewVisitor(
        fullName: String, mobile: String, email: String, imageUrl: String,
        isBlacklisted: Boolean, address : String)
    {
        var isMobileExisting = false
        val myDB = FirebaseFirestore.getInstance().collection(Constants.VISITOR_DB)
        val query1: Query = myDB.whereEqualTo(Constants.VISITOR_MOBILE, mobile )
        query1.get().addOnSuccessListener { result ->
            result.query
            for(document in result) {
                isMobileExisting = !result.isEmpty
              /*  val resultMobile = document.data[Constants.VISITOR_MOBILE].toString()
                if(!isMobileExisting) {
                    isMobileExisting = resultMobile == mobile
                }*/
            }
            if(!isMobileExisting) {
                val visitor = mapOf(
                    "visitorName" to fullName,
                    "visitorEmail" to email,
                    "visitorMobileNo" to mobile,
                    "visitorImage" to imageUrl,
                    Constants.TIMESTAMP to FieldValue.serverTimestamp(),
                    "isBlacklisted" to isBlacklisted,
                    "address" to address,
                )
                myDB.add(visitor).addOnSuccessListener {
                    Log.w("visitor added >>", "successfully . ")

                }.addOnFailureListener {
                    Log.w("visitor add error>>", "Error . " + it.message.toString())
                }
            }
        }.addOnFailureListener {
            Log.w("visitor already exist error>>", "Error . " + it.message.toString())
        }
    }
}