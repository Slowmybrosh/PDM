package com.example.comprapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.nio.ByteBuffer

/**
 * Clase que escanea los códigos de barras detectados por la cámara y texto de las etiquetas
 *
 * @property options opciones del escaner de barras. Por defecto se establece en códigos del tipo EAN_13
 */
class Scanner{
    private var options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_EAN_13).build()

    /**
     * Obtener una instancia del escáner
     */
    private fun getBarcodeInstance(): BarcodeScanner{
        return BarcodeScanning.getClient(options)
    }

    /**
     * obtener una instancia para reconocer el texto
     */
    private fun getRecognizerInstance(): com.google.mlkit.vision.text.TextRecognizer {
        return TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Permite establecer un tipo concreto de código de barras
     *
     * @param option tipo de código de barras
     */
    fun setOptions(option: Int){
        options = BarcodeScannerOptions.Builder().setBarcodeFormats(option).build()
    }


    /**
     * Analizar la imagen que contiene el código de barras
     *
     * @param image imagen que tiene el código de barras a detectar
     * @param rotation rotación de la imagen. Se calcula automáticamente por la librería de la cámara
     *
     * @return devuelve un string con el código de barras detectado o null si no encuentra ninguno
     */
    fun analyzeBarcode(image: ImageProxy, rotation: Int): String? {
        val input = InputImage.fromBitmap(imageProxyToBitmap(image), rotation)
        val barcode = getBarcodeInstance()
        var barcodes = barcode.process(input)

        while(!barcodes.isComplete){}

        for (barcode in barcodes.result){
                return barcode.rawValue
        }

        return null
    }

    /**
     * Analiza el texto de una imagen para encontrar el precio de la etiqueta
     *
     * @param image imagen que contiene la etiqueta a detectar
     * @param rotation rotación de la imagen. Se calcula automáticamente por la librería de la cámara
     *
     * @return devuelve una lista con los posibles precios detectados
     */
    fun analyzeText(image: ImageProxy, rotation: Int): Array<String> {
        val bitmap = imageRotateBitmap(imageProxyToBitmap(image),rotation)
        val input = InputImage.fromBitmap(bitmap,0)
        val recognizer = getRecognizerInstance()
        val numbers = recognizer.process(input)
            .addOnSuccessListener {
                Log.d("ComprApp", "Se ha encontrado texto en la imagen")
            }


        while(!numbers.isComplete){}
        val resultsList = ArrayList<String>()
        val regex = """\d+((\,|\.)\d{2,3})?""".toRegex()
        for(result in numbers.result.textBlocks){
            if(regex.containsMatchIn(result.text)){
                resultsList.add(regex.find(result.text)!!.value)
            }
        }

        return resultsList.toTypedArray()

    }

    /**
     * Convierte un proxy de imagen en una imagen en Bitmap
     *
     * @return imagen en Bitmap
     */
    fun imageProxyToBitmap(image: ImageProxy): Bitmap{
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /**
     * Rota imagenes en bitmap
     *
     * @return devuelve las imagenes rotadas
     */
    private fun imageRotateBitmap(image: Bitmap, degrees: Int): Bitmap{
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(image,0,0,image.width,image.height,matrix,false)
    }
}