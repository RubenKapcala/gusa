package com.kapgusa.fiesta.controlador

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.modelo.Dispositivo
import com.kapgusa.fiesta.modelo.Gustos
import com.kapgusa.fiesta.modelo.Jugador
import com.kapgusa.fiesta.modelo.Transformable
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable
import java.util.*


object MiBluetooth {

    //Declaración de constantes
    const val REQUEST_ENABLE_BLUETOOTH = 1 //Respuesta a activar bluetooth
    const val REQUEST_BLUETOOTH_SCAN_23 = 2 //Respuesta a escanear dispositivos API superior a 23
    private const val APP_NAME = "BTGame" //Nombre se enlaza con el UUID
    private val MY_UUID = UUID.fromString("bf34a98b-1971-4d0d-a010-592c9c009860")
    private const val separador = "€¬" //Elemento separador en los mensajes enviados por Bluetooth

    //Estados en los que puede estar la conexión
    enum class Estado{STATE_LISTENING, STATE_CONNECTING, STATE_CONNECTED, STATE_CONNECTION_FAILED}

    data class Mensaje(
        val datos: List<String>,
        val dispositivo: Int,
        val tipo: TipoDatoTransmitido
    ): Serializable, Transformable{
        //Define el objeto que se está enviando en el mensaje
        enum class TipoDatoTransmitido{POSICION, DISPOSITIVO, MENSAJE, METER_JUGADOR, QUITAR_JUGADOR, DISPOSITIVOS}
    }

    //Variables referentes a las conexiones
    private var posicion = 0
    var conexionServidor: SendReceive? = null //Conexión con un servidor
    private var conexionesCliente: MutableList<SendReceive?> = mutableListOf() //Conexiones con los clientes
    var bluetoothAdapter: BluetoothAdapter? = null
    var eresServidor = true //Nos indica si estás trabajando como servidor o como cliente


    //Cuando se inicie este objeto obtiene el BluetoothAdapter
    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    //Comprueba si el dispositivo tiene opciones Bluetooth al no devolver null el init
    val esBluetooth: Boolean
        get() = bluetoothAdapter != null


    //Combrueba si el Bluetooth está activado. De no ser así intenta activarlo
    fun activarBluetooth(activity: Activity){
        if (!bluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH)
        }
    }


    //Pone el dispositivo visible
    fun visibilizar(activity: Activity) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        activity.startActivity(discoverableIntent) //Lanza el intent para hacer visible

        val server = ServerClass() //Crea un objeto para gestionar la conexión como servidor
        server.start() //Se queda a la espera de que se conecten
    }


    //Implementa la funcionalidad de los eventos que ocurren mientras se buscan dispositivos
    interface BuscarDispositivosInterface{
        fun alEmpezar()
        fun alEncontrar(device: BluetoothDevice)
        fun alTerminar()
        fun siYaEstaBuscando()
    }


    //Permite buscar dispositivos Bluetooth visibles cercanos
    fun buscarDispisitivos(activity: Activity, funciones: BuscarDispositivosInterface) {
        if (!bluetoothAdapter?.isDiscovering!!){ //Comprueba que no este descubriendo
            comprobarPermisos(activity) //Comprueba que tenga permisos
            val intentFilter = IntentFilter() //Filtramos el intent
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND)

            //Gestiona las respuestas que recibimos
            val broadcastReceiverBusqueda = object: BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) { //Filtra por acción
                        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                            funciones.alEmpezar() //Se implementa el crear el objeto
                        }
                        BluetoothDevice.ACTION_FOUND -> {
                            //Extraemos el dispositivo del intent
                            val device: BluetoothDevice =
                                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!

                            //Extraemos el tipo de dispositivo que es
                            val bluetoothClass: BluetoothClass = device.bluetoothClass

                            when (bluetoothClass.majorDeviceClass) { //Comprueba si es un móvil
                                BluetoothClass.Device.Major.PHONE -> {
                                    funciones.alEncontrar(device) //Se implementa el crear el objeto
                                }
                            }
                        }
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            funciones.alTerminar() //Se implementa el crear el objeto
                        }
                    }
                }
            }
            //Registra la activity en el broadcastReceiver
            activity.registerReceiver(broadcastReceiverBusqueda, intentFilter)

        }else{
            funciones.siYaEstaBuscando() //Se implementa el crear el objeto
        }
    }

    //Comprueba que tenga permisos y de no tenerlos los pide
    private fun comprobarPermisos(activity: Activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ //A partir de la API 23

            //Si no tenemos el permiso de búsqueda
            if (ContextCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED){

                //Pide el permiso
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_BLUETOOTH_SCAN_23
                )
            }

            //Inicia la búsqueda y comprueva si es exitosa
            if (!bluetoothAdapter!!.startDiscovery()){
                //De no serlo lo informa y sugiere comprobar que esté la ubicación activa
                Toast.makeText(
                        activity,
                        activity.getText(R.string.activarGps),
                        Toast.LENGTH_LONG
                ).show()
            }
        }else{ //Si en menor a la API 23
            bluetoothAdapter!!.startDiscovery() //Inicia la busqueda
        }
    }


    //Desconecta de todos los dispositivos
    fun desconectarDispositivos(){
        conexionServidor?.cancelarConexion() //Cancela la conexión con el servidor

        for (i in conexionesCliente){ //Cancela las conexiones con cada cliente
            i?.cancelarConexion()
        }
        conexionesCliente.clear() //Vacía la lista de conexiones con clientes
    }


    //Crea un hilo nuevo para conectarse como servidor
    class ServerClass : Thread() {
        private var serverSocket: BluetoothServerSocket? = null

        init {
            try {
                //Obtiene el serverSocket con el UUID
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            eresServidor = true //Para indicar que trabaja como servidor
        }

        override fun run() {
            var socket: BluetoothSocket? = null
            while (socket == null) {
                try {
                    //Informa que estamos a la escucha
                    EventBus.getDefault().post(Estado.STATE_LISTENING)
                    socket = serverSocket!!.accept() //Pone el servidor a la escucha
                } catch (e: IOException) {
                    e.printStackTrace()
                    //Informa que la conexión ha fallado
                    EventBus.getDefault().post(Estado.STATE_CONNECTION_FAILED)
                }
                if (socket != null) { //Si a establecido una conexión
                    //Informa que la conexión es exitosa
                    EventBus.getDefault().post(Estado.STATE_CONNECTED)
                    val sendService = SendReceive(socket) //Crea objeto que gestione los mensajes
                    conexionesCliente.add(sendService) //Lo añade a la lista de clientes
                    sendService.start() //Inicia la escucha de mensajes
                    serverSocket?.close() //Cierra el serverSocket
                    break
                }
            }
        }
    }


    //Crea un hilo nuevo para conectarse como cliente
    class ClientClass(device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null

        init {
            //Informa que estamos intentando conectar
            EventBus.getDefault().post(Estado.STATE_CONNECTING)
            bluetoothAdapter?.cancelDiscovery() //Cancelamos el descubrimiento de dispositivos
            try {
                //Obtiene el socket con el UUID
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            eresServidor = false //Para indicar que trabaja como cliente
        }

        override fun run() {
            try {
                socket!!.connect() //Intenta establecer el socket con el servidor
                EventBus.getDefault().post(Estado.STATE_CONNECTED)//Informa que esta conectado
                conexionServidor = SendReceive(socket!!) //Crea objeto que gestione los mensajes
                conexionServidor!!.start()//Inicia la escucha de mensajes
            } catch (e: IOException) {
                e.printStackTrace()
                //Informa que la conexión ha fallado
                EventBus.getDefault().post(Estado.STATE_CONNECTION_FAILED)
            }
        }
    }


    //Crea objetos para gestionar los mensajes de un socket
    class SendReceive(bluetoothSocket: BluetoothSocket) : Thread() {

        var posicion = 0
        private var socket = bluetoothSocket
        private var inputStream: InputStream
        private var outputStream: OutputStream
        private var cerrar = false //Permite finalizar el hilo

        init {
            var tempIn: InputStream? = null
            var tempOut: OutputStream? = null
            try {
                tempIn = bluetoothSocket.inputStream
                tempOut = bluetoothSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            inputStream = tempIn!!
            outputStream = tempOut!!
        }

        override fun run() {
            val buffer = ByteArray(1024) //Mensaje a leer
            var bytes: Int //Tamaño del mensaje
            while (true) {
                try {
                    if (cerrar){ //Sale del bucle para finalizar el hilo
                        break
                    }
                    bytes = inputStream.read(buffer) //Lee el mensaje y devuelve la longitud
                    val tempMsg = String(buffer, 0, bytes) //Lo convierte en String

                    recibirDatos(tempMsg)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        //Envía la cadena que le pasemos por el outputStream
        fun write(mensaje: String?) {
            val stringBuffer = StringBuffer()
            stringBuffer.append(posicion.toString()) //Pasa tipo de dato a Int y después a String
            stringBuffer.append(separador) //Añade un separador
            stringBuffer.append(mensaje) //Añade un separador
            val bytes = stringBuffer.toString().toByteArray()
            try {
                outputStream.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        //Cierra la conexión
        fun cancelarConexion(){
            try {
                cerrar = true //Sale del bucle
                inputStream.close() //Cierra la entrada de datos
                outputStream.close() //Cierra la salida de datos
                socket.close() //Cierra el socket
            } catch (e: Exception) { }
        }
    }

    private fun recibirDatos(recivido: String) {
        val mensajes = recivido.split(separador + separador) //Separa el string en cada mensaje

        for (mensaje in mensajes){
            val mensajeCompleto = mensaje.split(separador) //Separa el mensaje en partes

            var dispositivo = 0
            var tipo = Mensaje.TipoDatoTransmitido.DISPOSITIVO
            val datosMensaje: MutableList<String> = mutableListOf()

            for ((i, dato) in mensajeCompleto.withIndex()){
                when(i){
                    0 -> dispositivo = dato.toInt()
                    1 -> tipo = Mensaje.TipoDatoTransmitido.values()[dato.toInt()]
                    else -> datosMensaje.add(dato)
                }
            }
            val mensajeRecivido = Mensaje(datosMensaje, dispositivo, tipo)
            EventBus.getDefault().post(mensajeRecivido) //Informa que recibió ese objeto
        }
    }

    //Permite enviar un mensaje a los dispositivos conectados
    private fun enviarDatos(dato: Any, tipo: Mensaje.TipoDatoTransmitido){
        val stringBuffer = StringBuffer()
        stringBuffer.append(separador) //Añade un separador
        stringBuffer.append(tipo.ordinal.toString()) //Pasa tipo de dato a Int y después a String
        stringBuffer.append(separador) //Añade un separador
        when(tipo){
            Mensaje.TipoDatoTransmitido.METER_JUGADOR, Mensaje.TipoDatoTransmitido.QUITAR_JUGADOR ->{
                val jugador = dato as Jugador
                stringBuffer.append(jugador.nombre) //Añade el mensaje
                stringBuffer.append(separador) //Añade otro separados
                stringBuffer.append(if (jugador.isChico) "1" else "0") //Añade el mensaje
                stringBuffer.append(separador) //Añade otro separados
                stringBuffer.append(jugador.gustos.ordinal.toString()) //Añade el mensaje
                stringBuffer.append(separador) //Añade otro separados
                stringBuffer.append(jugador.posicion.toString()) //Añade el dispositivo que lo lanzó
                stringBuffer.append(separador) //Añade otro separados
            }
            Mensaje.TipoDatoTransmitido.DISPOSITIVOS ->{}
            else -> {}
        }

        if (eresServidor){ //Si funciona como servidor se lo envía a cada cliente
            for (i in conexionesCliente){
                i!!.write(stringBuffer.toString())
            }
        }else{ //Si funciona como cliente se lo envia al servidor
            conexionServidor!!.write(stringBuffer.toString())
        }
    }

    fun enviarDispositivo(dispositivo: Dispositivo){
        enviarDatos(dispositivo, Mensaje.TipoDatoTransmitido.DISPOSITIVO)
    }

    fun numerar(){
        for ((i, conexion) in conexionesCliente.withIndex()){
            val stringBuffer = StringBuffer()
            stringBuffer.append(separador) //Añade un separador
            stringBuffer.append(Mensaje.TipoDatoTransmitido.POSICION.ordinal.toString()) //Pasa tipo de dato a Int y después a String
            stringBuffer.append(separador) //Añade un separador
            stringBuffer.append((i + 1).toString()) //Añade el mensaje
            stringBuffer.append(separador) //Añade otro separados
            conexion!!.write(stringBuffer.toString())
        }
    }

    fun enviarMensaje(mensaje: String, dispositivo: Int){
        val stringBuffer = StringBuffer()
        stringBuffer.append(Mensaje.TipoDatoTransmitido.MENSAJE.ordinal.toString()) //Pasa tipo de dato a Int y después a String
        stringBuffer.append(separador) //Añade un separador
        stringBuffer.append(mensaje) //Añade el mensaje
        stringBuffer.append(separador) //Añade otro separados
        conexionesCliente[dispositivo - 1]!!.write(stringBuffer.toString())

    }

    fun meterJugador(jugador: Jugador){
        enviarDatos(jugador, Mensaje.TipoDatoTransmitido.METER_JUGADOR)
    }

    fun quitarJugador(jugador: Jugador){
        enviarDatos(jugador, Mensaje.TipoDatoTransmitido.QUITAR_JUGADOR)
    }

}