package com.example.comprapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.*
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.nio.ByteBuffer

class Scanner{
    private var options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_EAN_13).build()

    private fun getInstance(): BarcodeScanner{
        return BarcodeScanning.getClient(options)
    }

    fun setOptions(option: Int){
        options = BarcodeScannerOptions.Builder().setBarcodeFormats(option).build()
    }

    fun analyzeBarcode(image: ImageProxy, rotation: Int): String {
        Log.d("ComprApp","Se ha entrado a analizar el código de barras")
        val input = InputImage.fromBitmap(imageProxyToBitmap(image), rotation)
        val barcode = getInstance()
        var barcodes = barcode.process(input)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    Log.d("ComprApp", "Encontrado código: " + barcode.rawValue.toString())
                }
            }

        while(!barcodes.isComplete){}

        var result = barcodes.result

        return if(result.size == 1){
            result[0].displayValue.toString()
        } else{
            //Raise Exception
            ""
        }


    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap{
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}