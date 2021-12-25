package com.kapgusa.fiesta.vistas

import android.content.pm.PackageManager
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
        binding.pene.adapter = JugadoresJuegoAdapter(this, DbHelper(this).obtenerMapas())


    }

}