package com.kapgusa.fiesta.vistas

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kapgusa.fiesta.databinding.ActivityMainBinding
import com.kapgusa.fiesta.modelo.bbdd.DbHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pene.setHasFixedSize(true)
        binding.pene.layoutManager = LinearLayoutManager(this)

        DatabaseThread().start()

        binding.botonNoPulsar.setOnClickListener{
            val mapas = DbHelper(this).obtenerMapas()
            binding.pene.adapter = JugadoresJuegoAdapter(this, mapas)
            binding.MAPA.setImageBitmap(BitmapFactory.decodeFile(mapas[0].direccionImagen))

        }

    }

    private inner class DatabaseThread(): Thread(){
        override fun run() {
            DbHelper(this@MainActivity)
            binding.textoCargandoInutil.text = "YAAAAAAA"

        }

    }

}
