package com.example.comprapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.comprapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val open_button = findViewById<Button>(R.id.button_camera)
        open_button.setOnClickListener{
            val intent = Intent(this, Camera::class.java)
            startActivity(intent)
        }
    }
}