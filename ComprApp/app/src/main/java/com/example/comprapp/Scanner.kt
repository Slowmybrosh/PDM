package com.example.comprapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.os.Parcelable
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.android.parcel.Parcelize
import java.nio.ByteBuffer

class Scanner{
    private var options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_EAN_13).build()

    private fun getBarcodeInstance(): BarcodeScanner{
        return BarcodeScanning.getClient(options)
    }

    private fun getRecognizerInstance(): com.google.mlkit.vision.text.TextRecognizer {
        return TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    fun setOptions(option: Int){
        options = BarcodeScannerOptions.Builder().setBarcodeFormats(option).build()
    }

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

    fun analyzeText(image: ImageProxy, rotation: Int): List<RecognizedText> {
        val bitmap = imageRotateBitmap(imageProxyToBitmap(image),rotation)
        val input = InputImage.fromBitmap(bitmap,0)
        val recognizer = getRecognizerInstance()
        val numbers = recognizer.process(input)
            .addOnSuccessListener {
                Log.d("ComprApp", "Se ha encontrado texto en la imagen")
            }


        while(!numbers.isComplete){}
        val resultsList = mutableListOf<RecognizedText>()
        val regex = """\d+((\,|\.)\d{2,3})?""".toRegex()
        for(result in numbers.result.textBlocks){
            if(regex.containsMatchIn(result.text)){
                var item = RecognizedText(regex.find(result.text)!!.value,result.cornerPoints)
                resultsList.add(item)
            }
        }

        return resultsList

    }

    fun imageProxyToBitmap(image: ImageProxy): Bitmap{
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun imageRotateBitmap(image: Bitmap, degrees: Int): Bitmap{
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(image,0,0,image.width,image.height,matrix,false)
    }
}

@Parcelize
data class Text(val name: String, val P1: Point, val P2: Point, val P3: Point, val P4: Point) : Parcelable