package com.dario.croppertest

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.dario.croppertest.databinding.ActivityCameraBinding
import com.theartofdev.edmodo.cropper.CropImage
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.UpdateConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.parameter.Flash
import io.fotoapparat.result.transformer.scaled
import io.fotoapparat.selector.firstAvailable
import io.fotoapparat.selector.front
import io.fotoapparat.selector.off
import io.fotoapparat.selector.torch
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class CameraActivity : ComponentActivity()  {

    private lateinit var fotoapparat: Fotoapparat
    private var activeCamera: Camera = Camera.Back

    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initCamera()
        initListeners()
        fotoapparat.start()
        adjustViewsVisibility()
    }

    private fun initCamera() {
        fotoapparat = Fotoapparat(
            context = this,
            view = binding.cameraView,
            focusView = binding.focusView,
            logger = logcat(),
            lensPosition = activeCamera.lensPosition,
            cameraConfiguration = activeCamera.configuration,
            cameraErrorCallback = { Log.e("CAMERA_LOGGING_TAG", "Camera error: ", it) }
        )
    }

    private fun initListeners() {
        binding.flashCheckBox.setOnCheckedChangeListener(toggleFlash())
        binding.switchCameraImageView.setOnClickListener { changeCamera() }
        binding.photoButton.setOnClickListener {
            takePicture()
        }
    }

    private fun adjustViewsVisibility() {
        fotoapparat.getCapabilities().whenAvailable { capabilities ->
            capabilities?.let {
                binding.flashCheckBox.visibility = if (it.flashModes.contains(Flash.Torch)) View.VISIBLE else View.GONE
            } ?: Log.e("CAMERA_LOGGING_TAG", "Couldn't obtain capabilities.")
        }
        binding.switchCameraImageView.visibility = if (fotoapparat.isAvailable(front())) View.VISIBLE else View.GONE
    }

    private fun toggleFlash(): (CompoundButton, Boolean) -> Unit = { _, isChecked ->
        fotoapparat.updateConfiguration(
            UpdateConfiguration(
                flashMode = if (isChecked) {
                    firstAvailable(torch(), off())
                } else {
                    off()
                }
            )
        )
    }

    private fun changeCamera() {
        activeCamera = when (activeCamera) {
            Camera.Front -> Camera.Back
            Camera.Back -> Camera.Front
        }

        fotoapparat.switchTo(
            lensPosition = activeCamera.lensPosition,
            cameraConfiguration = activeCamera.configuration
        )

        adjustViewsVisibility()
        binding.flashCheckBox.isChecked = false
    }

    private fun takePicture() {
        val photoResult = fotoapparat.autoFocus().takePicture()
        val file = File(this.getExternalFilesDir("photos"), "${Calendar.getInstance().timeInMillis}.jpg")
        photoResult.saveToFile(file)
        photoResult.toBitmap(scaled(scaleFactor = 0.25f))
            .whenAvailable { photo ->
                photo?.let {

                    val oldExif = ExifInterface(file.path)
                    val exifOrientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION)
                    val os = BufferedOutputStream(FileOutputStream(file))
                    it.bitmap.compress(Bitmap.CompressFormat.JPEG, 65, os)
                    os.flush()
                    os.close()

                    if (exifOrientation != null) {
                        val newExif = ExifInterface(file.path)
                        newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation)
                        newExif.saveAttributes()
                    }
                    //CropImage.activity(Uri.fromFile(file)).start(this)
                    //finish()
                    val uri = Uri.fromFile(file)
                    setResult(RESULT_OK, Intent().putExtra("uriFromFile", uri.toString()))
                    finish()
                } ?: Log.e("CAMERA_LOGGING_TAG", "Couldn't capture photo.")
            }
    }
}