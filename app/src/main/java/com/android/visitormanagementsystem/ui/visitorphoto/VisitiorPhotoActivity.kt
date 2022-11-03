package com.android.visitormanagementsystem.ui.visitorphoto

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
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.android.visitormanagementsystem.BuildConfig
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.VisitorPhotoBinding
import com.android.visitormanagementsystem.ui.registervisitor.RegisterVisitorActivity
import com.android.visitormanagementsystem.ui.visitorlanding.VisitorLandingActivity
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class VisitiorPhotoActivity:AppCompatActivity() {
    var progressViewState = ProgressBarViewState()
    private val db = Firebase.firestore

    var isPhotoUploaded: Boolean = false

    // lateinit var imageUri:Uri
    private var imageUri: Uri? = null
    var useMobileNo: String = ""

    //var storageRef = storage.reference
    var imageUrl: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(VisitorPhotoBinding.inflate(layoutInflater).apply{
            viewStateProgress = progressViewState

            useMobileNo = intent.getStringExtra("mobile").toString()
            println("Mobile 22  $useMobileNo")

            btnUploadPhoto.setOnClickListener {
                if (!progressViewState.progressbarEvent) {
                    if (!isPhotoUploaded) {
                        if (ActivityCompat.checkSelfPermission(
                                this@VisitiorPhotoActivity, android.Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this@VisitiorPhotoActivity,
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
                    } else {
                        val intent =
                            Intent(this@VisitiorPhotoActivity, RegisterVisitorActivity::class.java)
                        intent.putExtra("mobile", useMobileNo)
                        intent.putExtra("imageUrl", imageUrl)
                        startActivity(intent)
                        finish()
                    }
                }
                }

            tvChangePhoto.setOnClickListener {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra("selectContent", 4)
                    openCVResultLauncher.launch(intent)
                } else {
                    var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    openLegacyResultLauncher.launch(intent)
                }
            }

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
            progressViewState.progressbarEvent = true
            try {
                if(result.resultCode == Activity.RESULT_OK) {
                    var bmp: Bitmap = result.data?.extras?.get("data") as Bitmap
                    imageUri = saveImageInQ(bmp)
                    var ivPhoto = findViewById<CircleImageView>(R.id.ivPhotoVisitor)
                    ivPhoto.setImageBitmap(bmp)
                    uploadImage()


                }else{
                    progressViewState.progressbarEvent = false

                }
            } catch(e: Exception) {
                progressViewState.progressbarEvent = false
            }
        }

    var openLegacyResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            progressViewState.progressbarEvent = true

            try {
                if(result.resultCode == Activity.RESULT_OK) {
                    var extras: Bundle? = result.data?.extras
                    var bmp: Bitmap = result.data?.extras?.get("data") as Bitmap
                    imageUri = saveTheImageLegacyStyle(this, bmp)
                    var ivPhoto = findViewById<CircleImageView>(R.id.ivPhoto)
                    ivPhoto.setImageBitmap(bmp)
                    uploadImage()
                }else{
                    progressViewState.progressbarEvent = false

                }
            } catch(e: Exception) {
                progressViewState.progressbarEvent = false
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
                    progressViewState.progressbarEvent = false
                    var tvPhoto = findViewById<TextView>(R.id.tv_change_photo)
                    tvPhoto.visibility=View.VISIBLE;
                    var btn_upload_photo = findViewById<Button>(R.id.btn_upload_photo)
                    btn_upload_photo.text = "Next"
                    Log.d("uri>>>", uri.toString())
                })
            }.addOnFailureListener {
                progressViewState.progressbarEvent = false
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@VisitiorPhotoActivity, VisitorLandingActivity::class.java)
        startActivity(intent)
        this@VisitiorPhotoActivity.finish()
    }
}