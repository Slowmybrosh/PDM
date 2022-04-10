package com.example.comprapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.comprapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val first = MainFragment()
        val second = SecondFragment()

        setCurrentFragment(first)
        viewBinding.footer.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> setCurrentFragment(first)
                R.id.settings -> setCurrentFragment(second)
                R.id.add -> setCurrentFragment(second)
            }
            true
        }

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

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            commit()
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
        private const val REQUEST_CAMERA_PRICE= 11
    }
}