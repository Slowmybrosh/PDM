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
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.comprapp.databinding.ActivityCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Clase que contiene la cámara de la aplicación. Esta se usa para detectar códigos de barras y etiquetas de precio
 *
 * @property viewBinding contiene la vista para acceder rápidamente a los elementos de la misma
 * @property action acción de la cámara, puede ser abierta para detectar un código de barras o una etiqueta con el precio
 * @property imageCapture captura de imágenes
 * @property cameraExecutor hebra que controla la cámara
 */

class Camera : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCameraBinding
    private lateinit var action: CameraAction
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        action = intent.getSerializableExtra("action") as CameraAction
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

    /**
     * Obtener foto
     *
     * @param action contexto en el que se ha abierto la cámara: Barcode o Price
     */
    private fun takePhoto(action: CameraAction) {
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
                        CameraAction.BARCODE -> {
                            val value = scanner.analyzeBarcode(image,image.imageInfo.rotationDegrees)
                            image.close()
                            if(value != null){
                                data.data = Uri.parse(value)
                                setResult(RESULT_OK,data)
                                finish()
                            }
                        }
                        CameraAction.PRICE -> {
                            val recognizedText = scanner.analyzeText(image,image.imageInfo.rotationDegrees)
                            image.close()
                            if (recognizedText != null) {

                                val array: ArrayList<String> = ArrayList(recognizedText.asList())
                                data.putStringArrayListExtra("prices",array)
                                setResult(RESULT_OK,data)
                                finish()
                            }
                        }
                    }
                }
            }
        )
    }

    /**
     * Abrir la cámara para hacer una foto
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider) }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try{
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            }
            catch(exc: Exception){
                Log.e("ComprApp", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Comprobar si se han obtenido todos los permisos
     */
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