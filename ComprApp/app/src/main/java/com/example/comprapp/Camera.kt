package com.example.comprapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
import com.example.comprapp.Scanner
import com.example.comprapp.databinding.ActivityCameraBinding
import com.example.comprapp.Camera_action

class Camera : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCameraBinding
    private lateinit var action: Camera_action
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var bitmap: Bitmap

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

        viewBinding.imageCaptureButton.setOnClickListener{ takePhoto(action) }

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
                    val scanner: Scanner = Scanner()
                    lateinit var value: String

                    value = if(action == Camera_action.BARCODE){
                        //Procesamos y extraemos el código de barras
                        scanner.analyzeBarcode(image,image.imageInfo.rotationDegrees)
                    } else {
                        ""
                    }
                    image.close()

                    val data: Intent = Intent()
                    if (value!!.isNotEmpty()){
                        data.data = Uri.parse(value)
                        setResult(RESULT_OK,data)
                    } else{
                        data.data = Uri.parse("-1")
                        setResult(RESULT_CANCELED,data)
                    }
                    finish()
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