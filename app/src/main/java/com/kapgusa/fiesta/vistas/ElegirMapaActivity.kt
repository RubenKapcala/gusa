package com.kapgusa.fiesta.vistas

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.controlador.Musica
import com.kapgusa.fiesta.databinding.ActivityElegirMapaBinding
import com.kapgusa.fiesta.modelo.Mapa
import com.kapgusa.fiesta.modelo.bbdd.DbHelper

class ElegirMapaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityElegirMapaBinding

    //Creo una lista co los mapas--------------------------------------------------------
    private lateinit var listaMapas: List<Mapa>
    private lateinit var listaBitmap: Array<Bitmap?>

    //Inicializo variables---------------------------------------------------------------
    private lateinit var db: DbHelper
    private var posicion = 0
    private var presencial = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElegirMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DbHelper(this)

        cargarValores()

        ajustarViewIniciales()

        funcionalidadBotones()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun funcionalidadBotones() {

        binding.btnAtrasElegirMapa.setOnClickListener {
            Musica.sonidoBoton()
            finish()
        }
        binding.btnContinuarElegirMapa.setOnClickListener {
            Musica.sonidoBoton()
            binding.btnContinuarElegirMapa.isEnabled = false
            val intent = Intent(this, SeleccionarJugadoresActivity::class.java)
            intent.putExtra("posicion", posicion)
            startActivity(intent)
        }
        binding.btnPresencialElegirMapa.setOnClickListener {
            Musica.sonidoBoton()
            if (!presencial) {
                presencial = true
                binding.btnPresencialElegirMapa.background = resources.getDrawable(R.drawable.boton_sin_pulsar_reducido)
                binding.btnPresencialElegirMapa.text = getText(R.string.partidaPresencial)
            } else {
                presencial = false
                binding.btnPresencialElegirMapa.background = resources.getDrawable(R.drawable.boton_pulsado_reducido)
                binding.btnPresencialElegirMapa.text = getText(R.string.partidaNoPresencial)
            }
        }
        binding.btnMapaAnteriorElegirMapa.setOnClickListener {
            Musica.sonidoBoton()
            if (posicion == 0) {
                posicion = listaMapas.size - 1
            } else {
                posicion--
            }
            cargarMapa()
        }
        binding.btnMapaSiguienteElegirMapa.setOnClickListener {
            Musica.sonidoBoton()
            if (posicion == listaMapas.size - 1) {
                posicion = 0
            } else {
                posicion++
            }
            cargarMapa()
        }
        binding.btnInfoPresencialElegirMapa.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.lytInfoElegirMapa.visibility = View.VISIBLE
                binding.textInfoElegirMapa.text = getText(R.string.descripcionPresencial)
                Musica.sonidoBoton()
            }
            if (event.action == MotionEvent.ACTION_UP) {
                binding.lytInfoElegirMapa.visibility = View.INVISIBLE
            }
            true
        }
        binding.ivCasillasElegirMapa.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.lytInfoElegirMapa.visibility = View.VISIBLE
                binding.textInfoElegirMapa.text = listaMapas[posicion].descripcion
                Musica.sonidoBoton()
            }
            if (event.action == MotionEvent.ACTION_UP) {
                binding.lytInfoElegirMapa.visibility = View.INVISIBLE
            }
            true
        }
    }

    private fun cargarValores() {
        listaMapas = db.getMapas()
        listaBitmap = arrayOfNulls(listaMapas.size)
    }

    private fun ajustarViewIniciales() {
        if (!presencial) {
            binding.btnPresencialElegirMapa.text = getText(R.string.partidaNoPresencial)
            binding.btnPresencialElegirMapa.background = resources.getDrawable(R.drawable.boton_pulsado_reducido)
        } else {
            binding.btnPresencialElegirMapa.text = getText(R.string.partidaPresencial)
            binding.btnPresencialElegirMapa.background = resources.getDrawable(R.drawable.boton_sin_pulsar_reducido)
        }

        cargarMapa()
    }

    private fun cargarMapa() {
        //Cargamos previsualizacion del mapa
        if (listaBitmap[posicion] == null){
            listaBitmap[posicion] = Mapa.crearImagenMapa(listaMapas[posicion], this)
        }
        binding.ivCasillasElegirMapa.setImageBitmap(listaBitmap[posicion])
        binding.textNombreMapaElegirMapa.text = listaMapas[posicion].nombre
    }


    override fun onResume() {
        binding.btnContinuarElegirMapa.isEnabled = true
        super.onResume()
    }

}