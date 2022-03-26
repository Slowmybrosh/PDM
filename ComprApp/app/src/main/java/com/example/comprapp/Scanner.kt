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
    private var value: String? = null

    private fun getInstance(): BarcodeScanner{
        return BarcodeScanning.getClient(options)
    }

    fun setOptions(option: Int){
        options = BarcodeScannerOptions.Builder().setBarcodeFormats(option).build()
    }

    fun analyzeBarcode(image: ImageProxy, rotation: Int): String? {
        Log.d("CamerApp","Se ha entrado a analizar el código de barras")
        val input = InputImage.fromBitmap(imageProxyToBitmap(image), rotation)
        val barcode = getInstance()
        val result = barcode.process(input).addOnSuccessListener { barcodes ->
            for (barcode in barcodes) {
                value = barcode.displayValue
            }
        }

        return value
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap{
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}