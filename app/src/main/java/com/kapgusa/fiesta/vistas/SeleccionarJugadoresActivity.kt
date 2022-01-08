package com.kapgusa.fiesta.vistas

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.controlador.MiBluetooth
import com.kapgusa.fiesta.controlador.Musica
import com.kapgusa.fiesta.databinding.ActivitySeleccionarJugadoresBinding
import com.kapgusa.fiesta.modelo.Dispositivo
import com.kapgusa.fiesta.modelo.Gustos
import com.kapgusa.fiesta.modelo.Jugador
import com.kapgusa.fiesta.modelo.bbdd.DbHelper

class SeleccionarJugadoresActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeleccionarJugadoresBinding
    private lateinit var db: DbHelper

    //Inicializa las variables
    private lateinit var listaTusJugadores: MutableList<Jugador>
    private val listaDispositivo: MutableList<Dispositivo> = mutableListOf()
    private var esChico = false
    private var gustos = Gustos.CHICAS
    private var modificandoJugador: Jugador? = null
    private lateinit var botonesRv: BotonesRv


    interface BotonesRv{
        fun btnAniadir(jugador: Jugador)
        fun btnModificar(jugador: Jugador)
        fun btnSacarJugador(jugador: Jugador)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarJugadoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DbHelper(this)

        cargarValores()

        ajustarViewIniciales()

        funcionalidadBotones()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun funcionalidadBotones() {
        binding.btnInfoMultidispositivoSeleccionarJugadores.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                Musica.sonidoBoton()
                binding.lytInfoSeleccionarJugadores.visibility = View.VISIBLE
            }
            if (event.action == MotionEvent.ACTION_UP) {
                binding.lytInfoSeleccionarJugadores.visibility = View.INVISIBLE
            }
            true
        }
        binding.btnCrearJugadorSeleccionarJugadores.setOnClickListener {
            Musica.sonidoBoton()
            abrirCreador()
        }
        binding.btnCancelarJugadorSeleccionarJugadores.setOnClickListener {
            Musica.sonidoBoton()
            cerrarCreador()
        }
        binding.btnChicaSexoSeleccionarJugadores.setOnClickListener {
            Musica.sonidoBoton()
            actualizarSexo(false)
        }
        binding.btnChicoSexoSeleccionarJugadores.setOnClickListener {
            Musica.sonidoBoton()
            actualizarSexo(true)
        }
        binding.btnChicaGustosSeleccionarJugadores.setOnClickListener {
            Musica.sonidoBoton()
            actualizarGustos(Gustos.CHICAS)
        }
        binding.btnChicoGustosSeleccionarJugadores.setOnClickListener {
            Musica.sonidoBoton()
            actualizarGustos(Gustos.CHICOS)
        }
        binding.btnChicaChicoGustosSeleccionarJugadores.setOnClickListener {
            Musica.sonidoBoton()
            actualizarGustos(Gustos.AMBOS)
        }
        binding.btnAniadirSeleccionarJugadores.setOnClickListener {
            //Mostrar si es valido
            if (ComprobarDisponibilidad()) {
                Musica.sonidoBoton()
                if (modificandoJugador == null){
                    val jugador = db.insertarJugador(binding.etNombreSeleccionarJugadores.text.toString(), esChico, gustos)
                    listaTusJugadores.add(jugador)
                }else{
                    val jugador = db.modificarJugador(binding.etNombreSeleccionarJugadores.text.toString(), esChico, gustos, modificandoJugador!!.id!!)
                    listaTusJugadores.remove(modificandoJugador)
                    listaTusJugadores.add(jugador)
                    modificandoJugador = null
                    binding.btnBorrarJugadorSeleccionarJugadores.visibility = View.GONE
                }
                binding.rvTusJugadoresSeleccionarJugadores.adapter = AdapterJugadoresSeleccionarJugadores(listaTusJugadores, botonesRv)
                cerrarCreador()

            }else{
                Musica.sonidoBotonMal()
            }
        }
        binding.btnBorrarJugadorSeleccionarJugadores.setOnClickListener {
            Musica.sonidoBoton()
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.enSerio)
            builder.setMessage(R.string.deseaBorrar)
                    .setPositiveButton(R.string.atras) { _, _ -> }
                    .setNegativeButton(R.string.aceptar) { _, _ ->

                        db.borrarJugador(modificandoJugador!!.id!!)
                        listaTusJugadores.remove(modificandoJugador)
                        binding.rvTusJugadoresSeleccionarJugadores.adapter = AdapterJugadoresSeleccionarJugadores(listaTusJugadores, botonesRv)
                        cerrarCreador()
                    }.setCancelable(false).show()


        }
        binding.btnMultidispositivoSeleccionarJugadores.setOnClickListener {
            listaTusJugadores
            listaDispositivo
        }
    }

    private fun abrirCreador() {
        if (modificandoJugador == null){
            binding.etNombreSeleccionarJugadores.setText("")
        }else{
            binding.etNombreSeleccionarJugadores.setText(modificandoJugador!!.nombre)
            binding.btnBorrarJugadorSeleccionarJugadores.visibility = View.VISIBLE
        }
        binding.lytCrearJugadorSeleccionarJugadores.visibility = View.VISIBLE

    }

    private fun cerrarCreador() {
        binding.etNombreSeleccionarJugadores.setText("")
        binding.lytCrearJugadorSeleccionarJugadores.visibility = View.GONE
        binding.btnBorrarJugadorSeleccionarJugadores.visibility = View.GONE
    }

    private fun actualizarGustos(gustos: Gustos) {
        this.gustos = gustos
        binding.lytTaparChicaGustosSeleccionarJugadores.visibility = View.VISIBLE
        binding.lytTaparChicoGustosSeleccionarJugadores.visibility = View.VISIBLE
        binding.lytTaparChicaChicoGustosSeleccionarJugadores.visibility = View.VISIBLE
        when(gustos){
            Gustos.CHICOS ->binding.lytTaparChicoGustosSeleccionarJugadores.visibility = View.INVISIBLE
            Gustos.CHICAS ->binding.lytTaparChicaGustosSeleccionarJugadores.visibility = View.INVISIBLE
            Gustos.AMBOS ->binding.lytTaparChicaChicoGustosSeleccionarJugadores.visibility = View.INVISIBLE
        }
    }

    private fun actualizarSexo(esChico: Boolean) {
        this.esChico = esChico
        binding.lytTaparChicoSexoSeleccionarJugadores.visibility = View.VISIBLE
        binding.lytTaparChicaSexoSeleccionarJugadores.visibility = View.VISIBLE
        when(esChico){
            true ->binding.lytTaparChicoSexoSeleccionarJugadores.visibility = View.INVISIBLE
            false ->binding.lytTaparChicaSexoSeleccionarJugadores.visibility = View.INVISIBLE
        }
    }


    private fun ComprobarDisponibilidad(): Boolean {

        if (binding.etNombreSeleccionarJugadores.text.toString().replace(" ", "").isEmpty()){
            Toast.makeText(this, R.string.nombreVacio, Toast.LENGTH_LONG).show()
            return false
        }
        //Comprobamos si el nombre esta repetido
        for (jugador in listaTusJugadores) {
            val nombre = binding.etNombreSeleccionarJugadores.text.toString().replace(" ", "")
            if (nombre.compareTo(jugador.nombre.replace(" ", "")) == 0) {
                if (modificandoJugador == null || modificandoJugador!!.nombre != nombre){
                    Toast.makeText(this, R.string.nombreRepetido, Toast.LENGTH_LONG).show()
                    return false
                }
            }
        }

        return true
    }


    private fun cargarValores() {
        listaTusJugadores = db.getJugadores()
        listaDispositivo.add(Dispositivo(mutableListOf()))

        botonesRv = object : BotonesRv{
            override fun btnAniadir(jugador: Jugador) {
                if (MiBluetooth.eresServidor){
                    jugador.preparado = true
                    listaDispositivo[0].jugadores.add(jugador)
                    ajustarRvs()
                }else{

                }
            }

            override fun btnModificar(jugador: Jugador) {
                actualizarSexo(jugador.isChico)
                actualizarGustos(jugador.gustos)
                modificandoJugador = jugador
                abrirCreador()

            }

            override fun btnSacarJugador(jugador: Jugador) {
                if (MiBluetooth.eresServidor){
                    jugador.preparado = false
                    listaDispositivo[0].jugadores.remove(jugador)
                    ajustarRvs()
                }else{

                }
            }
        }
    }

    private fun ajustarViewIniciales() {
        binding.rvJugadoresPreparadosSeleccionarJugadores.setHasFixedSize(true)
        binding.rvJugadoresPreparadosSeleccionarJugadores.layoutManager = LinearLayoutManager(this)

        binding.rvTusJugadoresSeleccionarJugadores.setHasFixedSize(true)
        binding.rvTusJugadoresSeleccionarJugadores.layoutManager = LinearLayoutManager(this)

        ajustarRvs()
    }

    private fun ajustarRvs() {
        var nDispositivos = 0
        for (dispositivo in listaDispositivo){
            nDispositivos += dispositivo.jugadores.size
        }
        binding.textJugadoresPreparadosSeleccionarJugadores.text = getString(R.string.jugadoresPreparados) + " " + nDispositivos
        binding.rvTusJugadoresSeleccionarJugadores.adapter = AdapterJugadoresSeleccionarJugadores(listaTusJugadores, botonesRv)
        binding.rvJugadoresPreparadosSeleccionarJugadores.adapter = AdapterDispositivosSeleccionarJugadores(this, listaDispositivo, botonesRv)

    }

}