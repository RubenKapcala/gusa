package com.kapgusa.fiesta.vistas

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.databinding.ActivityMenuBinding
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
        builder.setMessage(R.string.alerta_mensaje)
                .setPositiveButton(R.string.continuar) { _, _ -> /*Musica.SonidoBoton()*/ }
            .setNegativeButton(R.string.salir) { _, _ ->
                //Musica.SonidoBoton()
                finishAffinity()
                exitProcess(0)
            }.setCancelable(false).show()
    }

    private fun funcionBotones() {
        binding.btnNuevaPatidaMenu.setOnClickListener{
            activarBotones(false)
            val botonNuevoJuego = Intent(this, SeleccionarJugadoresActivity::class.java)
            startActivity(botonNuevoJuego)
        }

        binding.btnOpcionesMenu.setOnClickListener{
            activarBotones(false)
            val botonOpciones = Intent(this, OpcionesActivity::class.java)
            startActivity(botonOpciones)
        }

        binding.btnTutorialMenu.setOnClickListener{
            activarBotones(false)
            val botonTutorial = Intent(this, TutorialActivity::class.java)
            startActivity(botonTutorial)
        }

        binding.btnPersonalizarMenu.setOnClickListener{
            binding.lytPersonalizarMenu.visibility = View.VISIBLE
        }

        binding.btnAtrasPreferenciasMenu.setOnClickListener{
            binding.lytPersonalizarMenu.visibility = View.GONE
        }

        binding.btnCreadorRetosMenu.setOnClickListener{
            activarBotones(false)
            val botonPersonalizar = Intent(this, CrearRetosActivity::class.java)
            startActivity(botonPersonalizar)
        }

        binding.btnCreadorMapasMenu.setOnClickListener{
            activarBotones(false)
            val botonPersonalizar = Intent(this, CrearMapasActivity::class.java)
            startActivity(botonPersonalizar)
        }

    }


    private fun activarBotones(activar: Boolean) {
        binding.btnNuevaPatidaMenu.isEnabled = activar
        binding.btnOpcionesMenu.isEnabled = activar
        binding.btnTutorialMenu.isEnabled = activar
        binding.btnCreadorMapasMenu.isEnabled = activar
        binding.btnCreadorRetosMenu.isEnabled = activar
    }

    override fun onBackPressed() {
        if (binding.lytPersonalizarMenu.visibility == View.VISIBLE) {
            binding.lytPersonalizarMenu.visibility = View.GONE
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.en_serio)
            builder.setMessage(R.string.no_hay_alcohol)
                    .setPositiveButton(R.string.salir) { _, _ ->
                        finishAffinity()
                        exitProcess(0)
                    }
                    .setNegativeButton(R.string.queda_mas) { _, _ -> }.setCancelable(false).show()
        }
    }

    override fun onPause() {
        super.onPause()
        //Musica.PausarMusica()
    }

    override fun onResume() {
        //Musica.PlayMusica()
        activarBotones(true)
        super.onResume()
    }
}