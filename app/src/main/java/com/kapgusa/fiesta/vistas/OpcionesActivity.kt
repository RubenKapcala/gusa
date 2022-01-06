package com.kapgusa.fiesta.vistas

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.controlador.Musica
import com.kapgusa.fiesta.databinding.ActivityOpcionesBinding
import com.kapgusa.fiesta.modelo.bbdd.DbHelper

class OpcionesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpcionesBinding
    private val NIVEL_MAX_BEBER = 5
    private val NIVEL_MAX_PICANTE = 5

    //Inicializamos variables----------------------------------------------------------------
    private lateinit var db: DbHelper
    private var nivelPicante = 0
    private var nivelBeber = 0
    private var valorAModificar = 0
    private var musicaVol = 0f
    private var efectosVol = 0f
    private val nombreCancion = intArrayOf(R.string.momentoRelax, R.string.gusaOnFire, R.string.fiestaConGusa, R.string.gusaOriginal, R.string.porDefecto)
    private var vasalloActivo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpcionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DbHelper(this)

        CargarValoresGuardados()

        AjustarViewsIniciales()

        funcionalidadBotones()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun funcionalidadBotones() {

        binding.btnBeberMenosOpciones.setOnClickListener {
            Musica.sonidoBoton()
            if (nivelBeber != 1) {
                nivelBeber--
                db.setNivelbeber(nivelBeber)
                ajustarNivel(nivelBeber, binding.lytNivelBeberOpciones, R.drawable.icono_jarra)
            }
        }
        binding.btnBeberMasOpciones.setOnClickListener {
            Musica.sonidoBoton()
            if (nivelBeber != NIVEL_MAX_BEBER) {
                nivelBeber++
                db.setNivelbeber(nivelBeber)
                ajustarNivel(nivelBeber, binding.lytNivelBeberOpciones, R.drawable.icono_jarra)
            }
        }
        binding.btnMenosPicanteOpciones.setOnClickListener {
            Musica.sonidoBoton()
            if (nivelPicante != 1) {
                nivelPicante--
                db.setNivelbeber(nivelPicante)
                ajustarNivel(nivelPicante, binding.lytNivelPicanteOpciones, R.drawable.guindilla)
            }
        }
        binding.btnMasPicanteOpciones.setOnClickListener {
            Musica.sonidoBoton()
            if (nivelPicante != NIVEL_MAX_PICANTE) {
                nivelPicante++
                db.setNivelbeber(nivelPicante)
                ajustarNivel(nivelPicante, binding.lytNivelPicanteOpciones, R.drawable.guindilla)
            }
        }
        binding.btnJuegoOpciones.setOnClickListener {
            Musica.sonidoBoton()
            binding.btnSonidoOpciones.setBackgroundColor(-0x8e999a)
            binding.btnJuegoOpciones.setBackgroundColor(-0x234897)
            binding.lytJuegoOpciones.visibility = View.VISIBLE
        }
        binding.btnSonidoOpciones.setOnClickListener {
            Musica.sonidoBoton()
            binding.btnSonidoOpciones.setBackgroundColor(-0x234897)
            binding.btnJuegoOpciones.setBackgroundColor(-0x8e999a)
            binding.lytJuegoOpciones.visibility = View.GONE
        }
        binding.btnVasalloOpciones.setOnClickListener {
            Musica.sonidoBoton()
            vasalloActivo = !db.getVasallo()!!
            db.setVasallo(vasalloActivo)
            if (vasalloActivo) {
                binding.btnVasalloOpciones.background = resources.getDrawable(R.drawable.boton_sin_pulsar)
                binding.btnVasalloOpciones.setText(R.string.vasalloActivado)
            } else {
                binding.btnVasalloOpciones.background = resources.getDrawable(R.drawable.boton_pulsado)
                binding.btnVasalloOpciones.setText(R.string.vasalloDesactivado)
            }
        }
        binding.btnCancionAnteriorOpciones.setOnClickListener {
            Musica.sonidoBoton()
            binding.textCancionOpciones.text = getString(nombreCancion[Musica.anteriorCancion()])
        }
        binding.btnCancionSiguienteOpciones.setOnClickListener {
            Musica.sonidoBoton()
            binding.textCancionOpciones.text = getString(nombreCancion[Musica.siguienteCancion()])
        }
        binding.btnAtrasOpciones.setOnClickListener {
            Musica.sonidoBoton()
            finish()
        }
        binding.ivInfoVasalloOpciones.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.textInfoOpciones.setText(R.string.descripcionVasallo)
                binding.lytInfoOpciones.visibility = View.VISIBLE
                Musica.sonidoBoton()
            }
            if (event.action == MotionEvent.ACTION_UP) {
                binding.lytInfoOpciones.visibility = View.INVISIBLE
                Musica.sonidoBoton()
            }
            true
        }

        //region Listener de barras seleccionadoras
        //Barra de sonido (musica)--------------------------------------------------------
        binding.barraMusicaOpciones.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                Musica.cambiarVolumenMusica(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        //Barra de sonido (efectos)------------------------------------------------------
        binding.barraEfectosOpciones.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                efectosVol = binding.barraEfectosOpciones.progress.toFloat()
                Musica.cambiarVolumenEfectos(efectosVol)
                Musica.sonidoBoton()
            }
        })
        //endregion
    }


    private fun CargarValoresGuardados() {
        vasalloActivo = db.getVasallo()!!
        musicaVol = db.getVolumenMusica()!!
        efectosVol = db.getVolumeEfectos()!!
        nivelBeber = db.getNivelBeber()!!
        nivelPicante = db.getNivelPicante()!!
    }

    private fun AjustarViewsIniciales() {
        if (vasalloActivo) {
            binding.btnVasalloOpciones.background = resources.getDrawable(R.drawable.boton_sin_pulsar)
            binding.btnVasalloOpciones.setText(R.string.vasalloActivado)
        } else {
            binding.btnVasalloOpciones.background = resources.getDrawable(R.drawable.boton_pulsado)
            binding.btnVasalloOpciones.setText(R.string.vasalloDesactivado)
        }
        binding.barraMusicaOpciones.progress = musicaVol.toInt()
        binding.barraEfectosOpciones.progress = efectosVol.toInt()
        ajustarNivel(nivelBeber, binding.lytNivelBeberOpciones, R.drawable.icono_jarra)
        ajustarNivel(nivelPicante, binding.lytNivelPicanteOpciones, R.drawable.guindilla)
        binding.textCancionOpciones.setText(nombreCancion[Musica.musicaElegida])
    }

    private fun ajustarNivel(nivel: Int, layout: LinearLayout, icono: Int) {

        layout.removeAllViews()
        for (i in 1..nivel){
            val imagen = ImageView(this)
            imagen.setImageResource(icono)
            imagen.adjustViewBounds = true
            imagen.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT)
            layout.addView(imagen)
        }

    }

    override fun onResume() {
        Musica.cambiarCancion(0)
        super.onResume()
    }

}