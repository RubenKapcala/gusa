package com.kapgusa.fiesta.vistas

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.controlador.MiBluetooth
import com.kapgusa.fiesta.databinding.ActivityBuscarPartidaBinding
import com.kapgusa.fiesta.vistas.adaptadores.AdapterDispositivos
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BuscarPartidaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuscarPartidaBinding //Binding con los elementos gráficos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuscarPartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Prepara el reciclerView
        binding.rvPartidas.setHasFixedSize(true)
        binding.rvPartidas.layoutManager = LinearLayoutManager(this)

        funcionalidadBotones()
        buscarDispositivos()

    }

    //Da la funcionalidad a los botones
    private fun funcionalidadBotones() {
        binding.btnAtras.setOnClickListener { finish()} //Cierra esta activity
        binding.btnReintentar.setOnClickListener { buscarDispositivos() } //Realiza una búsqueda
    }

    //Se comunica con el objeto MiBluetooth para que inicie una nueva búsqueda y se ocupa de mostrar al usuario los resultados
    private fun buscarDispositivos(){

        val dispositivosEncontrados = ArrayList<BluetoothDevice>() //Inicializa una lista de dispositivos vacía

        MiBluetooth.buscarDispisitivos(this, object: MiBluetooth.BuscarDispositivosInterface{

            override fun alEmpezar() {
                dispositivosEncontrados.clear() //Limpia la lista de dispositivos
                binding.rvPartidas.adapter?.notifyDataSetChanged() //Recarga la vista
                binding.pbBuscarDispositivos.visibility = View.VISIBLE //Pone la progressBar visible
            }

            override fun alEncontrar(device: BluetoothDevice) {
                //Si el dispositivo encontrado no pertenece a la lista lo añade y carga la vista
                if (!dispositivosEncontrados.contains(device)){
                    dispositivosEncontrados.add(device)
                    binding.rvPartidas.adapter = AdapterDispositivos(dispositivosEncontrados)
                }
            }

            override fun alTerminar() {
                binding.pbBuscarDispositivos.visibility = View.GONE //Pone la progressBar invisible
            }

            override fun siYaEstaBuscando() {
                //Muestra un mensaje informando que ya se está realizando la búsqueda
                Toast.makeText(this@BuscarPartidaActivity, R.string.yaBuscando, Toast.LENGTH_LONG).show()
            }

        })

    }

    //Recibe los cambios de estado de la conexión bluetooth
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventEstado(estado: MiBluetooth.Estado) {

        //Muestra un mensaje para informar que se encuentra en ese nuevo estado
        when(estado){
            MiBluetooth.Estado.STATE_CONNECTING -> Toast.makeText(
                this,
                "Connecting",
                Toast.LENGTH_LONG
            ).show()
            MiBluetooth.Estado.STATE_CONNECTION_FAILED -> Toast.makeText(
                this,
                "Connection Failed",
                Toast.LENGTH_LONG
            ).show()
            MiBluetooth.Estado.STATE_CONNECTED -> {
                Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show()
                MiBluetooth.eresServidor = false
                val intent = Intent(this, SeleccionarJugadoresActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> {}
        }
    }

    //Gestiona la respuesta del usuario tras pedirle los permisos necesarios para la búsqueda
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MiBluetooth.REQUEST_BLUETOOTH_SCAN_23 -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getText(R.string.permisoAceptado), Toast.LENGTH_SHORT).show()
                    buscarDispositivos()
                } else {
                    Toast.makeText(this, getText(R.string.permisosNecesasios), Toast.LENGTH_SHORT).show()
                }
            }
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
        MiBluetooth.bluetoothAdapter?.cancelDiscovery()
        EventBus.getDefault().unregister(this)
    }
}