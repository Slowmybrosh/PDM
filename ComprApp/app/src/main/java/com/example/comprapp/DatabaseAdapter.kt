package com.example.comprapp

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.comprapp.databinding.FragmentMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

class DatabaseAdapter(private val context: Context?) {
    private var requestQueue = Volley.newRequestQueue(context)

    fun searchBarcode(barcode: String, viewBinding: FragmentMainBinding){
        val regex = "^[0-9]{13}$".toRegex()
        var url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
        if (regex.matches(barcode)) {
            val request = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    try {
                        val json_array = response.getJSONObject("product")

                        viewBinding.productName.text = json_array.getString("product_name")
                        viewBinding.productImageUrl.text = json_array.getString("image_front_small_url")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, { error -> Log.e("ComprApp", error.toString()) })
            requestQueue.add(request)
        }
    }

    fun savePurchase(groceries: MutableList<PurchaseModel>) {
        val jsonString = Gson().toJson(groceries)
        val file = File(context?.filesDir, DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
        file.writeText(jsonString)
    }

    fun getPurchase(file: File): MutableList<PurchaseModel> {
        val mutableListType = object : TypeToken<MutableList<PurchaseModel>>() {}.type
        return Gson().fromJson(file.readText(), mutableListType)
    }

    fun getLastPurchase(): MutableList<PurchaseModel>? {
        val dir = File(context?.filesDir!!.path)
        var lastModified: Long = -1
        var path = ""
        if (dir.walk().iterator().hasNext() && dir.walk().toList().last().isFile) {
            val lastFile = dir.walk().toList().last()
            val mutableListType = object : TypeToken<MutableList<PurchaseModel>>() {}.type
            return Gson().fromJson(lastFile.readText(), mutableListType)
        }
        return null

    }

    fun getAllPurchases() : MutableList<File>{
        var list = File(context?.filesDir!!.path).walk().toMutableList()
        list.removeAt(0)
        return list
    }

    fun removeFile(archivo: File) {
        archivo.delete()
    }


}