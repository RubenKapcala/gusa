package com.kapgusa.fiesta.vistas

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.controlador.Musica
import com.kapgusa.fiesta.databinding.ActivityCrearRetosBinding
import com.kapgusa.fiesta.modelo.Reto
import com.kapgusa.fiesta.modelo.bbdd.DbHelper
import java.util.*

class CrearRetosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearRetosBinding

    //Inicializamos lista de retos personalizados
    var listaRetosPersonalizados: List<Reto> = ArrayList<Reto>()

    //Inicializamos variables
    private var posicion = 0
    private var tipo = Reto.TipoReto.BEBER.ordinal
    private var presencial = false
    private var picante = false
    private var modificandoReto = true
    private lateinit var db: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearRetosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DbHelper(this)

        funcionalidadBotones()

        cargarPantalla()
    }

    fun funcionalidadBotones() {
        binding.btnBeberCrearRetos.setOnClickListener {
            tipo = Reto.TipoReto.BEBER.ordinal
            refrescarBotonesTipo(tipo)
            Musica.sonidoBoton()
        }
        binding.btnAleatorioCrearRetos.setOnClickListener {
            tipo = Reto.TipoReto.ALEATORIO.ordinal
            refrescarBotonesTipo(tipo)
            Musica.sonidoBoton()
        }
        binding.btnEstrellaCrearRetos.setOnClickListener {
            tipo = Reto.TipoReto.ESTRELLA.ordinal
            refrescarBotonesTipo(tipo)
            Musica.sonidoBoton()
        }
        binding.btnBaulCrearRetos.setOnClickListener {
            tipo = Reto.TipoReto.BAUL.ordinal
            refrescarBotonesTipo(tipo)
            Musica.sonidoBoton()
        }
        binding.btnPicanteCrearRetos.setOnClickListener {
            tipo = Reto.TipoReto.PICANTE.ordinal
            refrescarBotonesTipo(tipo)
            Musica.sonidoBoton()
        }
        binding.btnDerechaCrearRetos.setOnClickListener {
            if (listaRetosPersonalizados.size > posicion + 1) {
                posicion++
            } else {
                posicion = 0
            }
            Musica.sonidoBoton()
            ajustarVistaMapa()
        }
        binding.btnIzquierdarbCrearRetos.setOnClickListener {
            if (posicion > 0) {
                posicion--
            } else {
                posicion = listaRetosPersonalizados.size - 1
            }
            Musica.sonidoBoton()
            ajustarVistaMapa()
        }
        binding.btnCambiarPresencialCrearRetos.setOnClickListener {
            if (presencial) {
                presencial = false
                binding.btnCambiarPresencialCrearRetos.background = resources.getDrawable(R.drawable.boton_pulsado)
                binding.btnCambiarPresencialCrearRetos.setText(R.string.noPresencial)
            } else {
                presencial = true
                binding.btnCambiarPresencialCrearRetos.background = resources.getDrawable(R.drawable.boton_sin_pulsar)
                binding.btnCambiarPresencialCrearRetos.setText(R.string.presencial)
            }
            Musica.sonidoBoton()
        }
        binding.btnCambiarPicanteCrearRetos.setOnClickListener {
            if (picante) {
                picante = false
                binding.btnCambiarPicanteCrearRetos.background = resources.getDrawable(R.drawable.boton_pulsado)
                binding.btnCambiarPicanteCrearRetos.setText(R.string.noPicante)
            } else {
                picante = true
                binding.btnCambiarPicanteCrearRetos.background = resources.getDrawable(R.drawable.boton_sin_pulsar)
                binding.btnCambiarPicanteCrearRetos.setText(R.string.picante)
            }
            Musica.sonidoBoton()
        }
        binding.btnNuevoRetoCrearRetos.setOnClickListener {
            Musica.sonidoBoton()
            comenzarNuevoReto()
        }
        binding.btnAtrasCrearRetos.setOnClickListener {
            Musica.sonidoBoton()
            atras()
        }
        binding.btnBorrarReroCrearRetos.setOnClickListener {
            Musica.sonidoBoton()
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.en_serio)
            builder.setMessage(R.string.deseaBorrar)
                    .setPositiveButton(R.string.atras) { _, _ -> Musica.sonidoBoton() }
                    .setNegativeButton(R.string.aceptar) { _, _ ->
                        db.borrarReto(listaRetosPersonalizados[posicion].id)
                        posicion = 0
                        cargarPantalla()
                        Musica.sonidoBoton()
                    }.setCancelable(false).show()        }
        binding.btnGuardarCrearRetos.setOnClickListener {
            if (comprobarTodoOk()){
                Musica.sonidoBoton()
                procesarGuardado(montarReto())
            }else{
                Toast.makeText(this, getString(R.string.rellenarCampos), Toast.LENGTH_LONG).show()
                Musica.sonidoBotonMal()
            }
        }
    }

    private fun comprobarTodoOk(): Boolean {
        var todoOk = binding.textRetoCrearRetos.text.toString().replace(" ", "").compareTo("") != 0
                || binding.textPuntosConRetoInputCrearRetos.text.toString().replace(" ", "").compareTo("") != 0
                || binding.textPuntosConRetoInputCrearRetos.text.toString().replace(" ", "").compareTo("") != 0

        //Comprobamos si ha checado y lo guardamos si es necesario
        val nivel = when(true){
            binding.rbNivel1CrearRetos.isChecked -> 1
            binding.rbNivel2CrearRetos.isChecked -> 2
            binding.rbNivel3CrearRetos.isChecked -> 3
            binding.rbNivel4CrearRetos.isChecked -> 4
            binding.rbNivel5CrearRetos.isChecked -> 5
            else -> 0
        }
        when (tipo) {Reto.TipoReto.BEBER.ordinal,Reto.TipoReto.PICANTE.ordinal -> if (nivel == 0) { todoOk = false } }
        return todoOk
    }

    private fun montarReto(): Reto {
        val retoTemporal = Reto(listOf(binding.textRetoCrearRetos.text.toString()), Reto.TipoReto.values()[tipo],
                0, 0, binding.textPuntosSinRetoInputCrearRetos.text.toString().toInt(),
                binding.textPuntosConRetoInputCrearRetos.text.toString().toInt(), presencial, true)

        if (picante) {
            retoTemporal.nivelPicante = 1
        }

        //Comprobamos si ha checado y lo guardamos si es necesario
        val nivel = when(true){
            binding.rbNivel1CrearRetos.isChecked -> 1
            binding.rbNivel2CrearRetos.isChecked -> 2
            binding.rbNivel3CrearRetos.isChecked -> 3
            binding.rbNivel4CrearRetos.isChecked -> 4
            binding.rbNivel5CrearRetos.isChecked -> 5
            else -> 0
        }

        when (tipo) {
            Reto.TipoReto.BEBER.ordinal -> retoTemporal.nivelBeber = nivel
            Reto.TipoReto.PICANTE.ordinal -> retoTemporal.nivelPicante = nivel
        }
        return retoTemporal
    }

    private fun procesarGuardado(reto: Reto) {
        if (modificandoReto) {
            db.modificarReto(reto, listaRetosPersonalizados[posicion].id)
            Toast.makeText(this, getString(R.string.retoModificado), Toast.LENGTH_LONG).show()
        } else {
            db.insertarReto(reto)
            Toast.makeText(this, getString(R.string.retoCreado), Toast.LENGTH_LONG).show()
        }
        cargarPantalla()
    }


    private fun cargarPantalla() {
        listaRetosPersonalizados = db.getRetosPersonalizados()
        if (listaRetosPersonalizados.isEmpty()) {
            //Preparamos para añadir reto
            comenzarNuevoReto()
        } else {
            //Mostramos las vistas necesarias
            binding.btnBorrarReroCrearRetos.visibility = View.VISIBLE
            binding.btnNuevoRetoCrearRetos.visibility = View.VISIBLE
            binding.btnIzquierdarbCrearRetos.visibility = View.VISIBLE
            binding.btnDerechaCrearRetos.visibility = View.VISIBLE
            binding.btnGuardarCrearRetos.setText(R.string.modificar)

            //Ponemos modo modificar retos
            modificandoReto = true

            ajustarVistaMapa()

        }
    }

    private fun ajustarVistaMapa() {
        //Mostramos titulo
        binding.textTituloCrearRetos.text = getString(R.string.tusRetos) + "  " + (posicion + 1) + "/" + listaRetosPersonalizados.size

        //Mostramos datos del reto
        tipo = listaRetosPersonalizados[posicion].tipo.ordinal
        refrescarBotonesTipo(tipo)

        //Texto
        binding.textRetoCrearRetos.setText(listaRetosPersonalizados[posicion].texto[0])

        //Presencial
        if (listaRetosPersonalizados[posicion].presencial) {
            presencial = true
            binding.btnCambiarPresencialCrearRetos.setText(R.string.presencial)
            binding.btnCambiarPresencialCrearRetos.background = resources.getDrawable(R.drawable.boton_sin_pulsar)
        } else {
            presencial = false
            binding.btnCambiarPresencialCrearRetos.setText(R.string.noPresencial)
            binding.btnCambiarPresencialCrearRetos.background = resources.getDrawable(R.drawable.boton_pulsado)
        }

        //Puntos
        binding.textPuntosSinRetoInputCrearRetos.setText(java.lang.String.valueOf(listaRetosPersonalizados[posicion].monedasSiempre))
        binding.textPuntosConRetoInputCrearRetos.setText(java.lang.String.valueOf(listaRetosPersonalizados[posicion].monedasReto))

        //Picante
        if (listaRetosPersonalizados[posicion].nivelPicante > 0) {
            picante = true
            binding.btnCambiarPicanteCrearRetos.setText(R.string.picante)
            binding.btnCambiarPicanteCrearRetos.background = resources.getDrawable(R.drawable.boton_sin_pulsar)
        } else {
            picante = false
            binding.btnCambiarPicanteCrearRetos.setText(R.string.noPicante)
            binding.btnCambiarPicanteCrearRetos.background = resources.getDrawable(R.drawable.boton_pulsado)
        }
        binding.rgBeberCrearRetos.clearCheck()

        //Niveles

        val nivel = when(listaRetosPersonalizados[posicion].tipo.ordinal){
            Reto.TipoReto.BEBER.ordinal -> listaRetosPersonalizados[posicion].nivelBeber
            Reto.TipoReto.PICANTE.ordinal -> listaRetosPersonalizados[posicion].nivelPicante
            else -> 0
        }

        when (nivel) {
            1 -> binding.rgBeberCrearRetos.check(binding.rbNivel1CrearRetos.id)
            2 -> binding.rgBeberCrearRetos.check(binding.rbNivel2CrearRetos.id)
            3 -> binding.rgBeberCrearRetos.check(binding.rbNivel3CrearRetos.id)
            4 -> binding.rgBeberCrearRetos.check(binding.rbNivel4CrearRetos.id)
            5 -> binding.rgBeberCrearRetos.check(binding.rbNivel5CrearRetos.id)
        }
    }

    private fun refrescarBotonesTipo(tipo: Int) {
        binding.taparBeber.visibility = View.VISIBLE
        binding.taparAleatorio.visibility = View.VISIBLE
        binding.taparEstrella.visibility = View.VISIBLE
        binding.taparBaul.visibility = View.VISIBLE
        binding.taparPicante.visibility = View.VISIBLE
        binding.btnCambiarPicanteCrearRetos.visibility = View.VISIBLE
        binding.lytPicanteCrearRetos.visibility = View.VISIBLE
        binding.textPuntosConRetoCrearRetos.setText(R.string.puntosAlCumplirReto)
        binding.lytNivelRetosCrearRetos.visibility = View.INVISIBLE
        when (tipo) {
            Reto.TipoReto.BEBER.ordinal -> {
                binding.taparBeber.visibility = View.INVISIBLE
                binding.lytNivelRetosCrearRetos.visibility = View.VISIBLE
                binding.textNivelesCrearRetos.setText(R.string.nivelAlcohol)
            }
            Reto.TipoReto.ALEATORIO.ordinal -> binding.taparAleatorio.visibility = View.INVISIBLE
            Reto.TipoReto.ESTRELLA.ordinal -> binding.taparEstrella.visibility = View.INVISIBLE
            Reto.TipoReto.BAUL.ordinal -> {
                binding.taparBaul.visibility = View.INVISIBLE
                binding.textPuntosConRetoCrearRetos.setText(R.string.valorDelBaul)
            }
            Reto.TipoReto.PICANTE.ordinal -> {
                binding.taparPicante.visibility = View.INVISIBLE
                binding.lytNivelRetosCrearRetos.visibility = View.VISIBLE
                binding.textNivelesCrearRetos.setText(R.string.NivelPicante)
                binding.btnCambiarPicanteCrearRetos.visibility = View.INVISIBLE
                binding.lytPicanteCrearRetos.visibility = View.INVISIBLE
            }
        }
    }

    private fun comenzarNuevoReto() {
        binding.textTituloCrearRetos.setText(R.string.nuevoReto)
        binding.btnBorrarReroCrearRetos.visibility = View.INVISIBLE
        binding.btnNuevoRetoCrearRetos.visibility = View.INVISIBLE
        binding.btnIzquierdarbCrearRetos.visibility = View.GONE
        binding.btnDerechaCrearRetos.visibility = View.GONE
        binding.textRetoCrearRetos.setText("")
        binding.rgBeberCrearRetos.clearCheck()
        binding.btnCambiarPicanteCrearRetos.setText(R.string.noPicante)
        binding.btnCambiarPicanteCrearRetos.background = resources.getDrawable(R.drawable.boton_pulsado_reducido)
        picante = false
        binding.btnCambiarPresencialCrearRetos.setText(R.string.presencial)
        binding.btnCambiarPresencialCrearRetos.background = resources.getDrawable(R.drawable.boton_sin_pulsar_reducido)
        presencial = true
        binding.textPuntosConRetoInputCrearRetos.setText("")
        binding.textPuntosSinRetoInputCrearRetos.setText("")
        binding.btnGuardarCrearRetos.setText(R.string.guardar)
        modificandoReto = false
        tipo = Reto.TipoReto.BEBER.ordinal
        refrescarBotonesTipo(tipo)
    }

    override fun onBackPressed() {
        atras()
    }

    private fun atras() {
        if (!modificandoReto) {
            cargarPantalla()
            if (listaRetosPersonalizados.isEmpty()) {
                finish()
            }
        } else {
            finish()
        }
    }
}