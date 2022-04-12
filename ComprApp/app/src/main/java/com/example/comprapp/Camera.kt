package com.example.comprapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.comprapp.databinding.ActivityCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Camera : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCameraBinding
    private lateinit var action: Camera_action
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        action = intent.getSerializableExtra("action") as Camera_action
        setContentView(viewBinding.root)

        if(allPermissionsGranted()){
            startCamera()
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        viewBinding.viewFinder.setOnTouchListener{ v, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                takePhoto(action)
            }
            true
        }


        cameraExecutor = Executors.newSingleThreadExecutor()
        imageCapture = ImageCapture.Builder().setJpegQuality(100).setFlashMode(FLASH_MODE_AUTO).build()
    }

    private fun takePhoto(action: Camera_action) {
        imageCapture.takePicture(cameraExecutor,
            object: ImageCapture.OnImageCapturedCallback(){ //Se llama cuando capturamos una imagen
                override fun onError(exception: ImageCaptureException) { // Si hay errores
                    Log.e("CamerApp","Error en la captura de la imagen", exception)
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val scanner = Scanner()
                    var data = Intent()

                    when (action) {
                        Camera_action.BARCODE -> {
                            val value = scanner.analyzeBarcode(image,image.imageInfo.rotationDegrees)
                            if(value != null){
                                data.data = Uri.parse(value)
                                setResult(RESULT_OK,data)
                                finish()
                            }
                        }
                        Camera_action.PRICE -> {
                            val recognizedText = scanner.analyzeText(image,image.imageInfo.rotationDegrees)
                            image.close()
                            if (recognizedText != null) {
                                val builder = AlertDialog.Builder(baseContext)
                                builder.setTitle("Selecciona el precio correcto").setItems(recognizedText) { dialog, which ->
                                    intent.putExtra("price", recognizedText[which])
                                    setResult(RESULT_OK, data)
                                    finish()
                                }.create().show()

                            }
                        }
                    }
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider) }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try{
                //Unbind por si acaso
                cameraProvider.unbindAll()

                //Vuelta a bindear
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            }
            catch(exc: Exception){
                Log.e("ComprApp", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            if(allPermissionsGranted()){
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE).toTypedArray()
    }
}