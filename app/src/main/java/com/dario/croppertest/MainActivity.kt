package com.dario.croppertest

import android.R.attr
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.SparseArray
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.dario.croppertest.databinding.ActivityMainBinding
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.theartofdev.edmodo.cropper.CropImage


@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private var cameraPermission:  Array<String>? = null
    private var storagePermission:  Array<String>? = null
    val STORAGE_REQUEST = 200
    val PHOTO_REQUEST_CODE = 300
    private var bitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        cameraPermission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        binding.btntest.setOnClickListener {
            takePicture()
        }
    }

    private fun takePicture() {
        if (!checkStoragePermission()) {
            requestStoragePermission()
        } else {
            startActivityForResult(Intent(this, CameraActivity::class.java), PHOTO_REQUEST_CODE)
            //CropImage.activity().start(this)
        }
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        requestPermissions(storagePermission!!, STORAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST) {
            if (grantResults.size > 0) {
                val writePermissionsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (writePermissionsAccepted) {
                    startActivityForResult(Intent(this, CameraActivity::class.java), PHOTO_REQUEST_CODE)
                    //CropImage.activity().start(this)
                } else {
                    Toast.makeText(this, "PERMISSION ERROR", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val imUri = result.uri
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imUri)
                bitmap?.let {
                    getTextFromImage(it)
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val result = data?.getStringExtra("uriFromFile")
                val uri = result!!.toUri()
                CropImage.activity(uri)
                    .setCropMenuCropButtonTitle("Recortar")
                    .start(this)
            } else {
                Toast.makeText(this, "Error taking photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTextFromImage(bitmap: Bitmap) {
        val recognizer = TextRecognizer.Builder(this).build()
        if (!recognizer.isOperational) {
            Toast.makeText(this, "recognizer not operational", Toast.LENGTH_SHORT).show()
        } else {
            val frame = Frame.Builder().setBitmap(bitmap).build()
            val parseArray: SparseArray<TextBlock> = recognizer.detect(frame)
            val stringBuilder = StringBuilder()
            for (i in 0..<parseArray.size()) {
                val block = parseArray.valueAt(i)
                stringBuilder.append(block.value)
                stringBuilder.append("\n")
            }
            Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}