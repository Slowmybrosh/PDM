package com.example.outshows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * @author Alejandro Olivares
 *
 * Esta clase contiene la actividad principal
 *
 */

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * Establece el fragmento que se muestra en el contenedor
     *
     * @param fragment fragmento a mostrar
     */
    private fun setFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {

        }
    }
}