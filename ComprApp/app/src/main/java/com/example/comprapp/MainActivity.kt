package com.example.comprapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.comprapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

//        val open_button = findViewById<Button>(R.id.button_camera)
//        open_button.setOnClickListener{
//            val intent = Intent(this, Camera::class.java)
//            intent.putExtra("action", Camera_action.BARCODE)
//            startActivityForResult(intent, REQUEST_CAMERA_BARCODE)
//        }

//        viewBinding.priceButton.setOnClickListener{
//            val intent = Intent(this, Camera::class.java)
//            intent.putExtra("action", Camera_action.PRICE)
//            startActivityForResult(intent, REQUEST_CAMERA_BARCODE)
//        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CAMERA_BARCODE){
            if(resultCode == RESULT_OK){

            } else if(resultCode == RESULT_CANCELED){

            }
        }
    }

    companion object{
        private const val REQUEST_CAMERA_BARCODE = 10
    }
}