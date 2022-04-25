package com.example.comprapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.comprapp.databinding.ActivityMainBinding

/**
 * Clase que contiene la actividad principal
 * @property viewBinding contiene la vista para acceder rápidamente a los elementos de la misma
 */

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val add = MainFragment(MainFragmentAction.ADD)
        val history = HistoryFragment(add)

        setCurrentFragment(add)

        viewBinding.footer.setOnItemSelectedListener {
            when(it.itemId){
                R.id.settings -> setCurrentFragment(history) //TODO cambiar icono y nombre
                R.id.add -> setCurrentFragment(add)
            }
            true
        }
    }

    /**
     * Establece el fragmento que se muestra en el contenedor
     *
     * @param fragment fragmento a mostrar
     */
    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            commit()
        }
}