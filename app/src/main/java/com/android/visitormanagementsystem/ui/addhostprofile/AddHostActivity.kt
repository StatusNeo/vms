package com.android.visitormanagementsystem.ui.addhostprofile

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
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.android.visitormanagementsystem.AddHostBinding
import com.android.visitormanagementsystem.BuildConfig
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHostActivity : AppCompatActivity() {
    var addHostViewState = ProgressBarViewState()
    var role: String = "Employee"
    // lateinit var imageUri:Uri
    private var imageUri: Uri? = null
    //var storageRef = storage.reference
    var imageUrl: String = ""
    var isPhotoUploaded: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(AddHostBinding.inflate(layoutInflater).apply {
            viewState = addHostViewState
            //  etUserName.requestFocus()
            btnSubmit.setOnClickListener {

               if (!isPhotoUploaded){
                   toast(R.string.error_photo_upload_host)
               }else
                if(etHostName.text.isNullOrBlank()) {
                    etHostName.requestFocus()
                    etHostName.error = "Please enter Host Full name"
                } else if(etHostMobile.text.isNullOrBlank()) {
                    etHostMobile.requestFocus()
                    etHostMobile.error = "Please enter Host mobile no."
                } else if(etHostMobile.text.toString()
                        .isNotEmpty() && etHostMobile.text?.length != 10
                ) {
                    etHostMobile.requestFocus()
                    etHostMobile.error = "Mobile number should be 10 digit's"
                } else if(!etHostMobile.text.toString()
                        .matches(Constants.MOBILE_NUMBER_REGEX.toRegex())
                ) {
                    etHostMobile.requestFocus()
                    etHostMobile.error = "Invalid Mobile Number"
                } else if(etEmailId.text.toString().isNotEmpty() && !etEmailId.text.toString()
                        .matches(Constants.EMAIL_REGEX.toRegex())
                ) {
                    etEmailId.error = "Please enter valid Email"
                } else if(radioGroup.checkedRadioButtonId == -1) {
                    toast(R.string.text_role)
                } else {
                    addNewHost(
                        etHostName.text.toString(),
                        etHostMobile.text.toString(),
                        etEmailId.text.toString() ?: "",
                        etHostDesignation.text.toString() ?: "",
                        role
                    )
                }
            }
            layoutPhoto.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(
                        this@AddHostActivity, android.Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@AddHostActivity,
                        arrayOf(android.Manifest.permission.CAMERA),
                        123
                    )
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.putExtra("selectContent", 4)
                        openCVResultLauncher.launch(intent)
                    } else {
                        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        openLegacyResultLauncher.launch(intent)
                    }

                }
            }


            radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
                override fun onCheckedChanged(p0: RadioGroup?, checkedId: Int) {

                    when(checkedId) {
                        R.id.radioSecurity -> {
                            role = "Security"
                        }
                        R.id.radioHost -> {
                            role = "Employee"
                        }
                        else -> {
                            role = "Employee"
                        }
                    }
                }
            })
        }.root)
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
            addHostViewState.progressbarEvent = true
            try {
                if(result.resultCode == Activity.RESULT_OK) {
                    var bmp: Bitmap = result.data?.extras?.get("data") as Bitmap
                    imageUri = saveImageInQ(bmp)
                    var ivPhoto = findViewById<ShapeableImageView>(R.id.ivPhotoVisitor)
                    ivPhoto.setImageBitmap(bmp)
                    uploadImage()


                }
            } catch(e: Exception) {
                addHostViewState.progressbarEvent = false
            }
        }
    var openLegacyResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            addHostViewState.progressbarEvent = true

            try {
                if(result.resultCode == Activity.RESULT_OK) {
                    var extras: Bundle? = result.data?.extras
                    var bmp: Bitmap = result.data?.extras?.get("data") as Bitmap
                    imageUri = saveTheImageLegacyStyle(this, bmp)
                    var ivPhoto = findViewById<ShapeableImageView>(R.id.ivPhotoVisitor)
                    ivPhoto.setImageBitmap(bmp)
                    uploadImage()
                }
            } catch(e: Exception) {
                addHostViewState.progressbarEvent = false
            }
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
                    addHostViewState.progressbarEvent = false
                    var tvPhoto = findViewById<TextView>(R.id.tv_photo)
                    tvPhoto.text= "Change Photo";

                    Log.d("uri>>>", uri.toString())
                })
            }.addOnFailureListener {
                addHostViewState.progressbarEvent = false
            }
        }
    }

    private fun addNewHost(
        fullName: String, mobile: String, email: String, designation: String, role: String
    ) {
        var isMobileExisting = false
        var isUserNameExisting = false
        addHostViewState.progressbarEvent = true
        val myDB = FirebaseFirestore.getInstance().collection(Constants.EMPLOYEE_LIST)
        myDB.get().addOnSuccessListener { result ->

            result.query
            for(document in result) {
                val resultMobile = document.data[Constants.HOST_MOBILE].toString()
                if(!isMobileExisting) {
                    isMobileExisting = resultMobile == mobile
                }
            }
            if(!isMobileExisting) {
                val host = mapOf(
                    Constants.HOST_NAME to fullName,
                    Constants.HOST_MOBILE to mobile,
                    Constants.EMAIL to email,
                    Constants.DESIGNATION to designation,
                    Constants.TIMESTAMP to FieldValue.serverTimestamp(),
                    Constants.EMP_ROLE to role,
                    Constants.HOST_PHOTO to imageUrl
                )

                saveNewHost(host, myDB)
            } else {
                addHostViewState.progressbarEvent = false
                if(isMobileExisting) toast(R.string.host_already_exist)
                else if(isMobileExisting) toast(R.string.host_mobile_already_exist)
            }
        }.addOnFailureListener {
            addHostViewState.progressbarEvent = false
            toast(R.string.msg_something_went)
        }
    }

    private fun saveNewHost(
        host: Map<String, Any>,
        myDB: CollectionReference,
    ) {
        myDB.add(host).addOnSuccessListener {
            addHostViewState.progressbarEvent = false
            toast(R.string.toast_host_saved)
            this@AddHostActivity.finish()

        }.addOnFailureListener {
            addHostViewState.progressbarEvent = false
            toast(R.string.msg_something_went)
        }
    }
}