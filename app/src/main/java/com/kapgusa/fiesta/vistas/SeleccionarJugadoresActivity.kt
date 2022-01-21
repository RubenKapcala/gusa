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
import com.kapgusa.fiesta.vistas.adaptadores.AdapterDispositivosSeleccionarJugadores
import com.kapgusa.fiesta.vistas.adaptadores.AdapterJugadoresSeleccionarJugadores
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SeleccionarJugadoresActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeleccionarJugadoresBinding
    private lateinit var db: DbHelper

    //Inicializa las variables
    private lateinit var listaTusJugadores: MutableList<Jugador>
    private val listaDispositivos: MutableList<Dispositivo> = mutableListOf()
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

        if (!MiBluetooth.eresServidor){
            MiBluetooth.enviarDispositivo(Dispositivo())
        }
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
        binding.lytCrearJugadorSeleccionarJugadores.setOnClickListener {
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
            if (comprobarDisponibilidadCrear()) {
                Musica.sonidoBoton()
                if (modificandoJugador == null){
                    val jugador = db.insertarJugador(binding.etNombreSeleccionarJugadores.text.toString(), esChico, gustos)
                    jugador.preparado = true
                    listaTusJugadores.add(jugador)
                    btnMeterJugador(jugador)
                }else{
                    val jugador = db.modificarJugador(binding.etNombreSeleccionarJugadores.text.toString(), esChico, gustos, modificandoJugador!!.id!!)
                    listaTusJugadores.remove(modificandoJugador)
                    listaTusJugadores.add(jugador)
                    modificandoJugador = null
                }
                binding.rvTusJugadoresSeleccionarJugadores.adapter = AdapterJugadoresSeleccionarJugadores(listaTusJugadores, botonesRv)
                cerrarCreador()
            }else{
                Musica.sonidoBotonMal()
            }
        }
        binding.btnEliminarJugadorSeleccionarJugadores.setOnClickListener {
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
            if (MiBluetooth.esBluetooth){
                MiBluetooth.visibilizar(this)
            } else{
                Toast.makeText(this, getText(R.string.sin_bluetooth), Toast.LENGTH_LONG).show()
            }
        }
        binding.btnComenzarSeleccionarJugadores.setOnClickListener {
            if (MiBluetooth.eresServidor){

            }else{

            }
        }
    }

    private fun abrirCreador() {
        if (modificandoJugador == null){
            binding.etNombreSeleccionarJugadores.setText("")
        }else{
            binding.etNombreSeleccionarJugadores.setText(modificandoJugador!!.nombre)
            binding.btnEliminarJugadorSeleccionarJugadores.visibility = View.VISIBLE
        }
        binding.lytCrearJugadorSeleccionarJugadores.visibility = View.VISIBLE

    }

    private fun cerrarCreador() {
        modificandoJugador = null
        binding.etNombreSeleccionarJugadores.setText("")
        binding.lytCrearJugadorSeleccionarJugadores.visibility = View.GONE
        binding.btnEliminarJugadorSeleccionarJugadores.visibility = View.GONE
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

    private fun comprobarDisponibilidadCrear(): Boolean {

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
        listaDispositivos.add(Dispositivo())

        botonesRv = object : BotonesRv{
            override fun btnAniadir(jugador: Jugador) {
                Musica.sonidoBoton()
                btnMeterJugador(jugador)
            }

            override fun btnModificar(jugador: Jugador) {
                Musica.sonidoBoton()
                actualizarSexo(jugador.isChico)
                actualizarGustos(jugador.gustos)
                modificandoJugador = jugador
                abrirCreador()

            }

            override fun btnSacarJugador(jugador: Jugador) {
                Musica.sonidoBoton()
                if (MiBluetooth.eresServidor){
                    jugador.preparado = false
                    listaDispositivos[0].jugadores.remove(jugador)
                    ajustarRvs()
                }else{

                }
            }
        }
    }

    private fun ajustarViewIniciales() {

        if (!MiBluetooth.eresServidor){
            binding.btnMultidispositivoSeleccionarJugadores.visibility = View.GONE
            binding.btnComenzarSeleccionarJugadores.visibility = View.GONE
            binding.btnInfoMultidispositivoSeleccionarJugadores.visibility = View.GONE
        }

        binding.rvJugadoresPreparadosSeleccionarJugadores.setHasFixedSize(true)
        binding.rvJugadoresPreparadosSeleccionarJugadores.layoutManager = LinearLayoutManager(this)

        binding.rvTusJugadoresSeleccionarJugadores.setHasFixedSize(true)
        binding.rvTusJugadoresSeleccionarJugadores.layoutManager = LinearLayoutManager(this)

        ajustarRvs()
    }

    private fun ajustarRvs() {
        var nDispositivos = 0
        for (dispositivo in listaDispositivos){
            nDispositivos += dispositivo.jugadores.size
        }
        binding.textJugadoresPreparadosSeleccionarJugadores.text = getString(R.string.jugadoresPreparados) + " " + nDispositivos
        binding.rvTusJugadoresSeleccionarJugadores.adapter = AdapterJugadoresSeleccionarJugadores(listaTusJugadores, botonesRv)
        binding.rvJugadoresPreparadosSeleccionarJugadores.adapter = AdapterDispositivosSeleccionarJugadores(this, listaDispositivos, botonesRv)
    }

    private fun nuevoDispositivo(){
        if (MiBluetooth.eresServidor){
            MiBluetooth.numerar()
            Thread.sleep(500)
            MiBluetooth.enviarDispositivo(Dispositivo())
            listaDispositivos.add(Dispositivo())
            binding.rvJugadoresPreparadosSeleccionarJugadores.adapter = AdapterDispositivosSeleccionarJugadores(this, listaDispositivos, botonesRv)
        }

    }

    private fun btnMeterJugador(jugador: Jugador){
        if (MiBluetooth.eresServidor){
            meterJugador(jugador, 0)
        }else{
            MiBluetooth.meterJugador(jugador)
        }
    }

    private fun meterJugador(jugador: Jugador, dispositivo: Int){
        if (MiBluetooth.eresServidor){
            if (comprobarDisponivilidadMeter(jugador)){
                jugador.preparado = true
                listaDispositivos[dispositivo].jugadores.add(jugador)
                ajustarRvs()
                MiBluetooth.meterJugador(jugador)
            }else{
                MiBluetooth.enviarMensaje(getString(R.string.nombreYaEnPartida), dispositivo)
            }
        }else{
            val index = buscarNombre(listaTusJugadores, jugador.nombre)
            if (index != -1){
                listaTusJugadores[index].preparado = true
            }
            listaDispositivos[dispositivo].jugadores.add(jugador)
            ajustarRvs()
        }
    }

    private fun comprobarDisponivilidadMeter(jugador: Jugador): Boolean{
        for (dis in listaDispositivos){
            if (buscarNombre(dis.jugadores, jugador.nombre) != -1){
                return false
            }
        }
        return true
    }

    private fun buscarNombre(lista: MutableList<Jugador>, nombre: String): Int{
        for ((i, jug) in lista.withIndex()){
            if (jug.nombre == nombre){
                return i
            }
        }
        return -1
    }

    private fun quitarJugador(jugador: Jugador, dispositivo: Int){

    }


    //Recibe los cambios de estado de la conexión bluetooth
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventEstado(estado: MiBluetooth.Estado) {

        when(estado){
            MiBluetooth.Estado.STATE_LISTENING -> Toast.makeText(this, "Listening", Toast.LENGTH_LONG).show()
            MiBluetooth.Estado.STATE_CONNECTION_FAILED -> Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show()
            MiBluetooth.Estado.STATE_CONNECTED -> Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    //Recibe la información que que un nuevo jugador se ha conectado
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventDatosBluetooth(mensaje: MiBluetooth.Mensaje) {

        when(mensaje.tipo){
            MiBluetooth.Mensaje.TipoDatoTransmitido.POSICION ->{
                MiBluetooth.conexionServidor?.posicion = mensaje.datos[0].toInt()
            }
            MiBluetooth.Mensaje.TipoDatoTransmitido.DISPOSITIVO ->{
                nuevoDispositivo()
            }
            MiBluetooth.Mensaje.TipoDatoTransmitido.MENSAJE ->{
                Toast.makeText(this, mensaje.datos[0], Toast.LENGTH_LONG).show()
            }
            MiBluetooth.Mensaje.TipoDatoTransmitido.METER_JUGADOR, MiBluetooth.Mensaje.TipoDatoTransmitido.QUITAR_JUGADOR->{
                val dispositivo: Int
                val nombre = mensaje.datos[0]
                val esChico = mensaje.datos[1].toInt() > 0
                val gusto = Gustos.values()[mensaje.datos[2].toInt()]
                val jugador = Jugador(nombre, esChico, gusto)
                if (MiBluetooth.eresServidor){
                    dispositivo = mensaje.dispositivo
                    jugador.posicion = dispositivo
                }else{
                    dispositivo = mensaje.datos[3].toInt()
                }
                if (mensaje.tipo == MiBluetooth.Mensaje.TipoDatoTransmitido.METER_JUGADOR){
                    meterJugador(jugador, dispositivo)
                }else{
                    quitarJugador(jugador, dispositivo)
                }

            }
            MiBluetooth.Mensaje.TipoDatoTransmitido.DISPOSITIVOS ->{}
        }

    }

    //Se registra en EventBus
    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    //Cancela el registro en EventBus
    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }
}