package com.kapgusa.fiesta.vistas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.controlador.MiBluetooth
import com.kapgusa.fiesta.controlador.Musica
import com.kapgusa.fiesta.databinding.ActivityMenuBinding
import com.kapgusa.fiesta.modelo.bbdd.DbHelper
import kotlin.system.exitProcess

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        mostrarMensajeControlDeEdad()

        funcionBotones()

    }

    private fun mostrarMensajeControlDeEdad() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.alerta)
        builder.setMessage(R.string.alertaMensaje)
                .setPositiveButton(R.string.continuar) { _, _ -> Musica.sonidoBoton() }
            .setNegativeButton(R.string.salir) { _, _ ->
                Musica.sonidoBoton()
                finishAffinity()
                exitProcess(0)
            }.setCancelable(false).show()
    }

    private fun funcionBotones() {
        binding.btnNuevaPatidaMenu.setOnClickListener{
            activarBotones(false)
            Musica.sonidoBoton()
            val botonNuevoJuego = Intent(this, ElegirMapaActivity::class.java)
            startActivity(botonNuevoJuego)
        }

        binding.btnOpcionesMenu.setOnClickListener{
            activarBotones(false)
            Musica.sonidoBoton()
            val botonOpciones = Intent(this, OpcionesActivity::class.java)
            startActivity(botonOpciones)
        }

        binding.btnBuscarPartidaMenu.setOnClickListener{
            Musica.sonidoBoton()
            if (MiBluetooth.esBluetooth){
                activarBotones(false)
                val botonTutorial = Intent(this, BuscarPartidaActivity::class.java)
                startActivity(botonTutorial)
            } else{
                Toast.makeText(this, getText(R.string.sin_bluetooth), Toast.LENGTH_LONG).show()
            }

        }

        binding.btnPersonalizarMenu.setOnClickListener{
            Musica.sonidoBoton()
            binding.lytPersonalizarMenu.visibility = View.VISIBLE
        }

        binding.btnAtrasPreferenciasMenu.setOnClickListener{
            Musica.sonidoBoton()
            binding.lytPersonalizarMenu.visibility = View.GONE
        }

        binding.btnCreadorRetosMenu.setOnClickListener{
            activarBotones(false)
            Musica.sonidoBoton()
            val botonPersonalizar = Intent(this, CrearRetosActivity::class.java)
            startActivity(botonPersonalizar)
        }

        binding.btnCreadorMapasMenu.setOnClickListener{
            activarBotones(false)
            Musica.sonidoBoton()
            val botonPersonalizar = Intent(this, CrearMapasActivity::class.java)
            startActivity(botonPersonalizar)
        }

    }


    private fun activarBotones(activar: Boolean) {
        binding.btnNuevaPatidaMenu.isEnabled = activar
        binding.btnOpcionesMenu.isEnabled = activar
        binding.btnBuscarPartidaMenu.isEnabled = activar
        binding.btnCreadorMapasMenu.isEnabled = activar
        binding.btnCreadorRetosMenu.isEnabled = activar
    }

    override fun onBackPressed() {
        Musica.sonidoBoton()
        if (binding.lytPersonalizarMenu.visibility == View.VISIBLE) {
            binding.lytPersonalizarMenu.visibility = View.GONE
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.enSerio)
            builder.setMessage(R.string.noHayAlcohol)
                    .setPositiveButton(R.string.salir) { _, _ ->
                        Musica.sonidoBoton()
                        finishAffinity()
                        exitProcess(0)
                    }
                    .setNegativeButton(R.string.quedaMas) { _, _ ->
                        Musica.sonidoBoton()
                    }.setCancelable(false).show()
        }
    }

    override fun onResume() {
        activarBotones(true)
        super.onResume()
    }
}