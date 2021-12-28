package com.kapgusa.fiesta.vistas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kapgusa.fiesta.R
import com.kapgusa.fiesta.databinding.ActivityMainBinding
import com.kapgusa.fiesta.modelo.bbdd.DbHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var contadorVueltas = 0
    private val mHandler = Handler(Looper.getMainLooper())
    private var cargaInutilActiva = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Thread {
            DbHelper(this)
            cargaInutil.run()
        }.start()

        binding.botonNoPulsar.setOnClickListener{
            continuar()
        }

    }

    private val cargaInutil: Runnable = object : Runnable {
        override fun run() {

            var tiempoReloj = 0

            when (contadorVueltas) {
                1 -> {
                    tiempoReloj = 3000
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil1)
                    binding.botonNoPulsar.visibility = View.VISIBLE

                }
                2 -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress = 2
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil2)
                    tiempoReloj = 2200

                }
                3 -> {
                    binding.progressBar.progress = 5
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil3)
                    tiempoReloj = 2500
                }
                4 -> {
                    binding.progressBar.progress = 12
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil4)
                    tiempoReloj = 2100
                }
                5 -> {
                    binding.progressBar.progress = 34
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil5)
                    tiempoReloj = 2400
                }
                6 -> {
                    binding.progressBar.progress = 40
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil6)
                    tiempoReloj = 2700
                }
                7 -> {
                    binding.progressBar.progress = 45
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil7)
                    tiempoReloj = 3800
                }
                8 -> {
                    binding.progressBar.progress = 60
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil8)
                    tiempoReloj = 3000
                }
                9 -> {
                    binding.progressBar.progress = 80
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil9)
                    tiempoReloj = 4000
                }
                10 -> {
                    binding.progressBar.progress = 90
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil10)
                    tiempoReloj = 2700
                }
                11 -> {
                    binding.progressBar.progress = 98
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil11)
                    tiempoReloj = 2500
                }
                12 -> {
                    binding.progressBar.progress = 100
                    binding.textoCargandoInutil.setText(R.string.cargandoInutil12)
                    tiempoReloj = 1500
                }
                13 -> continuar()
            }
            mHandler.postDelayed(this, tiempoReloj.toLong())
            contadorVueltas++
        }
    }

    private fun continuar() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }


    //Recibe los cambios de estado de la conexión bluetooth
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventEstado(estado: DbHelper.EstadoCargaBBDD) {
        binding.textoCargandoInutil.text = estado.texto
        if (estado.carga == 0){
            binding.botonNoPulsar.visibility = View.VISIBLE
            cargaInutilActiva = true
        }else{
            binding.progressBar.progress = estado.carga
        }
    }

    //Se registra en EventBus
    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        if (cargaInutilActiva){
            cargaInutil.run()
        }
    }

    //Cancela el registro en EventBus
    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
        mHandler.removeCallbacks(cargaInutil)
    }
}
