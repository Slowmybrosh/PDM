package com.example.comprapp

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.comprapp.databinding.FragmentMainBinding
import org.json.JSONException

class DatabaseAdapter(private val context: Context?) {
    private var requestQueue = Volley.newRequestQueue(context)

    fun searchBarcode(barcode: String, viewBinding: FragmentMainBinding) : Pair<String,Bitmap>{
        val regex = "^[0-9]{13}$".toRegex()
        var url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
        if(regex.matches(barcode)){
            val request = JsonObjectRequest(Request.Method.GET, url, null,
                {response -> try{
                                val json_array = response.getJSONObject("product")

                                viewBinding.productName.text = json_array.getString("product_name")
                                viewBinding.productImageUrl.text = json_array.getString("image_front_small_url")
                            } catch(e: JSONException){
                                e.printStackTrace()
                            }
                }, { error -> error.printStackTrace() })
            requestQueue.add(request)

            val product_url_image = viewBinding.productImageUrl.text.toString()
            val image_request = ImageRequest(product_url_image,
                {
                    bitmap -> viewBinding.productImage.setImageBitmap(bitmap)
                },0,0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,{ error -> error.printStackTrace() })
            requestQueue.add(image_request)
        }
        return Pair(viewBinding.productName.text.toString(), viewBinding.productImage.drawable.toBitmap())
    }

    fun savePurchase(groceries: MutableList<PurchaseModel>){
        //TODO Guardar la lista completa en un JSON con el timestamp, añadir un timestamp al JSON al finalizar la compra (como ID)
    }

    fun getPurchase(timestamp: String): Pair<String, MutableList<PurchaseModel>>? {
        //TODO Recuperar la información de un JSON devolviendo el timestamp y la lista (para hacer el historial)
        return null
    }
}